package com.pro.Facture.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pro.Facture.Dto.*;
import com.pro.Facture.Entity.Client;
import com.pro.Facture.Entity.Commande;
import com.pro.Facture.Entity.Place;
import com.pro.Facture.Entity.Utilisateur;
import com.pro.Facture.repository.ClientRepository;
import com.pro.Facture.repository.CommandeRepository;
import com.pro.Facture.repository.PlaceRepository;
import com.pro.Facture.repository.UtilisateurRepository;
import com.pro.Facture.service.Pdf.CommandePdfService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final ClientRepository clientRepository;
    private final CommandePdfService commandePdfService;
    private final PlaceRepository placeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON mapper

    public CommandeService(CommandeRepository commandeRepository,
                           ClientRepository clientRepository,
                           CommandePdfService commandePdfService,
                           PlaceRepository placeRepository, UtilisateurRepository utilisateurRepository) {

        this.commandeRepository = commandeRepository;
        this.clientRepository = clientRepository;
        this.commandePdfService = commandePdfService;
        this.placeRepository = placeRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    // ----------------------------
    // CREATE FACTURE
    // ----------------------------
    public CommandeResponseDto createCommande(CommandeRequestDto dto) {

        // 🔹 Récupération du client
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client introuvable"));

        // 🔐 Récupération utilisateur connecté

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("Utilisateur non connecté");
        }

        // Récupérer l'email depuis le principal
        String email;
        if (auth.getPrincipal() instanceof UserDetails userDetails) {
            email = userDetails.getUsername(); // normalement c'est l'email
        } else if (auth.getPrincipal() instanceof String s) {
            email = s; // parfois JWT met juste l'email comme String
        } else {
            throw new RuntimeException("Impossible de récupérer l'utilisateur");
        }

        // Charger l'utilisateur depuis la base
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // 🔹 Calcul des totaux
        double totalBaseHT = 0.0;
        List<LigneCommandeResponseDto> lignes = new ArrayList<>();

        for (LigneCommandeDto l : dto.getLignes()) {
            double base = l.getHt();
            totalBaseHT += base;

            LigneCommandeResponseDto res = new LigneCommandeResponseDto();
            res.setDesign(l.getDesign());
            res.setBaseHT(base);
            lignes.add(res);
        }

        double totalRetenue = totalBaseHT * (dto.getRetenue() / 100);
        double totalMT = totalBaseHT - totalRetenue;
        double tauxTva = dto.getTauxTva() != null ? dto.getTauxTva() : 0.0;
        double totalTva = totalMT * (tauxTva / 100);
        double totalTTC = totalMT + totalTva;
        double totalNetAPayer = totalTTC - dto.getAvance();

        // 🔹 Création de la commande
        Commande commande = new Commande();
        commande.setClient(client);
        commande.setUtilisateur(user); // important
        commande.setDateFacture(dto.getDateFacture());
        commande.setDesign("FACTURE MULTI-LIGNES");
        commande.setHt(totalBaseHT);
        commande.setRetenue(totalRetenue);
        commande.setMt(totalMT);
        commande.setTva(tauxTva);
        commande.setMtTtc(totalTTC);
        commande.setAvance(dto.getAvance());
        commande.setNet(totalNetAPayer);

        // Stocker les lignes en JSON
        try {
            String lignesJson = objectMapper.writeValueAsString(dto.getLignes());
            commande.setLignesJson(lignesJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur conversion lignes en JSON");
        }

        // 💾 Sauvegarde
        Commande saved = commandeRepository.save(commande);
        saved.setRef(generateRef(saved.getId()));
        commandeRepository.save(saved);

        // 📦 Construction de la réponse
        CommandeResponseDto response = new CommandeResponseDto();
        response.setId(Math.toIntExact(saved.getId()));
        response.setRef(saved.getRef());
        response.setDateFacture(saved.getDateFacture());
        response.setClient(mapClient(client));
        response.setLignes(lignes);
        response.setTotalBaseHT(totalBaseHT);
        response.setTotalRetenue(totalRetenue);
        response.setTotalHTNet(totalMT);
        response.setTotalTva(totalTva);
        response.setTotalTTC(totalTTC);
        response.setTotalAvance(dto.getAvance());
        response.setTotalNetAPayer(totalNetAPayer);

        // Ajouter utilisateur dans la réponse
        response.setUtilisateur(mapUtilisateur(user));

        return response;
    }

    private String generateRef(Long id) {
        return String.format("%05d", id);
    }

    private ClientDto mapClient(Client client) {
        ClientDto c = new ClientDto();
        c.setId(client.getId());
        c.setNom(client.getNom());
        c.setNif(client.getNIF());
        c.setTelephone(client.getTelephone());
        c.setAdresse(client.getAdresse());
        return c;
    }

    private UtilisateurDto mapUtilisateur(Utilisateur user) {
        UtilisateurDto dto = new UtilisateurDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        return dto;
    }

    // ----------------------------
    // GET ALL COMMANDES
    // ----------------------------
    public List<CommandeResponseDto> getAll() {
        return commandeRepository.findAll()
                .stream()
                .map(this::mapCommandeToDto)
                .collect(Collectors.toList());
    }

    // ----------------------------
    // GET ONE COMMAND BY ID
    // ----------------------------
    public CommandeResponseDto getById(Long id) {
        Commande cmd = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        CommandeResponseDto dto = mapCommandeToDto(cmd);

        Place place = placeRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("Aucun Place trouvé"));

        byte[] pdfBytes = commandePdfService.genererPdf(dto, place);
        dto.setPdfBase64(Base64.getEncoder().encodeToString(pdfBytes));

        return dto;
    }


    // ----------------------------
    // DELETE COMMAND BY ID
    // ----------------------------
    public void deleteById(Long id) {
        if (!commandeRepository.existsById(id)) {
            throw new RuntimeException("Commande introuvable");
        }
        commandeRepository.deleteById(id);
    }

    private CommandeResponseDto mapCommandeToDto(Commande cmd) {
        CommandeResponseDto dto = new CommandeResponseDto();

        // ✅ ID réel
        dto.setId(Math.toIntExact(cmd.getId()));

        dto.setRef(cmd.getRef());
        dto.setDateFacture(cmd.getDateFacture());
        dto.setTotalBaseHT(cmd.getHt());
        dto.setTotalRetenue(cmd.getRetenue());
        dto.setTotalHTNet(cmd.getMt());
        dto.setTotalTva(cmd.getTva());
        dto.setTotalTTC(cmd.getMtTtc());
        dto.setTotalAvance(cmd.getAvance());
        dto.setTotalNetAPayer(cmd.getNet());

        if (cmd.getClient() != null) {
            dto.setClient(mapClient(cmd.getClient()));
        }

        // ✅ Charger les lignes JSON ICI AUSSI
        if (cmd.getLignesJson() != null && !cmd.getLignesJson().isEmpty()) {
            try {
                List<LigneCommandeDto> lignesDto =
                        Arrays.asList(objectMapper.readValue(
                                cmd.getLignesJson(),
                                LigneCommandeDto[].class
                        ));

                // conversion vers ResponseDto
                List<LigneCommandeResponseDto> lignes = lignesDto.stream().map(l -> {
                    LigneCommandeResponseDto r = new LigneCommandeResponseDto();
                    r.setDesign(l.getDesign());
                    r.setBaseHT(l.getHt());
                    return r;
                }).toList();

                dto.setLignes(lignes);
            } catch (Exception e) {
                dto.setLignes(new ArrayList<>());
            }
        } else {
            dto.setLignes(new ArrayList<>());
        }
        if (cmd.getUtilisateur() != null) {
            dto.setUtilisateur(mapUtilisateur(cmd.getUtilisateur()));
        }


        return dto;
    }

    // ----------------------------
    // GET 5 DERNIÈRES COMMANDES
    // ----------------------------
    public List<CommandeResponseDto> getLastFiveCommandes() {
        return commandeRepository.findTop3ByOrderByIdDesc()
                .stream()
                .map(this::mapCommandeToDto)
                .collect(Collectors.toList());
    }



    // ----------------------------
// AJOUT PAIEMENT (FACTURE MPE)
// ----------------------------
    public CommandeResponseDto ajouterPaiement(PaiementCommandeDto dto) {

        Commande commande = commandeRepository.findById(dto.getCommandeId())
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        if ("SOLDEE".equals(commande.getStatut())) {
            throw new RuntimeException("Cette facture est déjà soldée");
        }

        double ancienneAvance = commande.getAvance();
        double mtTtc = commande.getMtTtc();
        double montantPaye = dto.getMontantPaye();

        if (montantPaye <= 0) {
            throw new RuntimeException("Montant invalide");
        }

        double nouvelleAvance = ancienneAvance + montantPaye;
        double nouveauNet = mtTtc - nouvelleAvance;

        if (nouveauNet < 0) {
            throw new RuntimeException("Le montant payé dépasse le reste à payer");
        }

        commande.setAvance(nouvelleAvance);
        commande.setNet(nouveauNet);

        if (nouveauNet == 0) {
            commande.setStatut("PAYEE");
        } else {
            commande.setStatut("IMPAYEE");
        }

        Commande saved = commandeRepository.save(commande);

        // 🔁 DTO mis à jour
        CommandeResponseDto response = mapCommandeToDto(saved);

        // 🔁 PDF MIS À JOUR
        Place place = placeRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("Aucun Place trouvé"));

        byte[] pdfBytes = commandePdfService.genererPdf(response, place);
        response.setPdfBase64(Base64.getEncoder().encodeToString(pdfBytes));

        return response;
    }

}

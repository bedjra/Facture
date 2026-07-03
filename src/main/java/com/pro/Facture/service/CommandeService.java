package com.pro.Facture.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pro.Facture.Dto.*;
import com.pro.Facture.Entity.*;
import com.pro.Facture.repository.*;
import com.pro.Facture.service.Pdf.CommandePdfService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final ClientRepository clientRepository;
    private final PaiementCommandeRepository paiementCommandeRepository;
    private final CommandePdfService commandePdfService;
    private final PlaceRepository placeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CommandeService(CommandeRepository commandeRepository,
                           ClientRepository clientRepository,
                           PaiementCommandeRepository paiementCommandeRepository,
                           CommandePdfService commandePdfService,
                           PlaceRepository placeRepository,
                           UtilisateurRepository utilisateurRepository) {
        this.commandeRepository = commandeRepository;
        this.clientRepository = clientRepository;
        this.paiementCommandeRepository = paiementCommandeRepository;
        this.commandePdfService = commandePdfService;
        this.placeRepository = placeRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    // ----------------------------
    // CREATE COMMANDE
    // ----------------------------
    @Transactional
    public CommandeResponseDto createCommande(CommandeRequestDto dto) {

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client introuvable"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("Utilisateur non connecté");
        }

        String email;
        if (auth.getPrincipal() instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else if (auth.getPrincipal() instanceof String s) {
            email = s;
        } else {
            throw new RuntimeException("Impossible de récupérer l'utilisateur");
        }

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

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

        Commande commande = new Commande();
        commande.setClient(client);
        commande.setUtilisateur(user);
        commande.setDateFacture(dto.getDateFacture());
        commande.setDesign("FACTURE MULTI-LIGNES");
        commande.setHt(totalBaseHT);
        commande.setRetenue(totalRetenue);
        commande.setMt(totalMT);
        commande.setTva(tauxTva);
        commande.setMtTtc(totalTTC);
        commande.setAvance(0.0);
        commande.setNet(totalTTC);

        try {
            String lignesJson = objectMapper.writeValueAsString(dto.getLignes());
            commande.setLignesJson(lignesJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur conversion lignes en JSON");
        }

        Commande saved = commandeRepository.save(commande);
        saved.setRef(generateRef(saved.getId()));
        commandeRepository.save(saved);

        // 🔥 Si une avance est donnée à la création
        if (dto.getAvance() != null && dto.getAvance() > 0) {
            PaiementCommande paiement = new PaiementCommande();
            paiement.setCommande(saved);
            paiement.setMontant(dto.getAvance());
            // ✅ Date du premier paiement : celle du DTO si fournie, sinon aujourd'hui
            paiement.setDatePaiement(dto.getDatePaiement() != null ? dto.getDatePaiement() : LocalDate.now());
            paiementCommandeRepository.save(paiement);

            saved.setAvance(dto.getAvance());
            saved.setNet(totalTTC - dto.getAvance());
            saved.setStatut(saved.getNet() == 0 ? "PAYEE" : "IMPAYEE");
            commandeRepository.save(saved);
        }

        return mapCommandeToDto(saved);
    }

    // ----------------------------
    // AJOUTER UN PAIEMENT
    // ----------------------------
    @Transactional
    public CommandeResponseDto ajouterPaiement(PaiementCommandeDto dto) {

        Commande commande = commandeRepository.findById(dto.getCommandeId())
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        if ("PAYEE".equals(commande.getStatut())) {
            throw new RuntimeException("Cette facture est déjà soldée");
        }

        double montantPaye = dto.getMontantPaye();
        if (montantPaye <= 0) {
            throw new RuntimeException("Montant invalide");
        }

        double totalDejaPaye = paiementCommandeRepository.sumMontantByCommandeId(commande.getId());
        double nouveauTotal = totalDejaPaye + montantPaye;

        if (nouveauTotal > commande.getMtTtc()) {
            throw new RuntimeException("Le montant payé dépasse le reste à payer");
        }

        // ✅ Création du paiement avec la vraie date
        PaiementCommande paiement = new PaiementCommande();
        paiement.setMontant(montantPaye);
        paiement.setDatePaiement(dto.getDatePaiement() != null ? dto.getDatePaiement() : LocalDate.now());
        paiement.setCommande(commande);
        paiementCommandeRepository.save(paiement);

        double reste = commande.getMtTtc() - nouveauTotal;
        commande.setAvance(nouveauTotal);
        commande.setNet(reste);
        commande.setStatut(reste == 0 ? "PAYEE" : "IMPAYEE");

        Commande saved = commandeRepository.save(commande);

        CommandeResponseDto response = mapCommandeToDto(saved);

        Place place = placeRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("Aucun Place trouvé"));

        byte[] pdfBytes = commandePdfService.genererPdf(response, place);
        response.setPdfBase64(Base64.getEncoder().encodeToString(pdfBytes));

        return response;
    }

    // ----------------------------
    // MAP COMMANDE → DTO (avec historique paiements)
    // ----------------------------
    private CommandeResponseDto mapCommandeToDto(Commande cmd) {
        CommandeResponseDto dto = new CommandeResponseDto();

        dto.setId(Math.toIntExact(cmd.getId()));
        dto.setRef(cmd.getRef());
        dto.setStatut(cmd.getStatut());
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

        if (cmd.getUtilisateur() != null) {
            dto.setUtilisateur(mapUtilisateur(cmd.getUtilisateur()));
        }

        // ✅ Lignes JSON
        if (cmd.getLignesJson() != null && !cmd.getLignesJson().isEmpty()) {
            try {
                List<LigneCommandeDto> lignesDto = Arrays.asList(
                        objectMapper.readValue(cmd.getLignesJson(), LigneCommandeDto[].class)
                );
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

        // ✅ Historique des paiements
        List<PaiementCommande> paiements = paiementCommandeRepository.findByCommandeId(cmd.getId());
        List<PaiementCommandeResponseDto> historique = paiements.stream().map(p -> {
            PaiementCommandeResponseDto r = new PaiementCommandeResponseDto();
            r.setId(p.getId());
            r.setMontant(p.getMontant());
            r.setDatePaiement(p.getDatePaiement());
            return r;
        }).toList();
        dto.setHistoriquePaiements(historique);

        return dto;
    }

    // ----------------------------
    // GET ALL
    // ----------------------------
    public List<CommandeResponseDto> getAll() {
        return commandeRepository.findAll()
                .stream()
                .map(this::mapCommandeToDto)
                .collect(Collectors.toList());
    }

    // ----------------------------
    // GET BY ID (avec PDF)
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
    // DELETE
    // ----------------------------
    @Transactional
    public void deleteById(Long id) {
        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));
        paiementCommandeRepository.deleteByCommandeId(id);
        commandeRepository.delete(commande);
    }

    // ----------------------------
    // GET 3 DERNIÈRES
    // ----------------------------
    public List<CommandeResponseDto> getLastFiveCommandes() {
        return commandeRepository.findTop3ByOrderByIdDesc()
                .stream()
                .map(this::mapCommandeToDto)
                .collect(Collectors.toList());
    }



    // ----------------------------
// UPDATE COMMANDE
// ----------------------------
    @Transactional
    public CommandeResponseDto updateCommande(Long id, CommandeRequestDto dto) {

        Commande commande = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        if ("PAYEE".equals(commande.getStatut())) {
            throw new RuntimeException("Impossible de modifier une facture déjà soldée");
        }

        // --- Client ---
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
        commande.setClient(client);

        // --- Date ---
        commande.setDateFacture(dto.getDateFacture());

        // --- Recalcul des montants ---
        double totalBaseHT = 0.0;
        for (LigneCommandeDto l : dto.getLignes()) {
            totalBaseHT += l.getHt();
        }

        double totalRetenue = totalBaseHT * (dto.getRetenue() / 100);
        double totalMT      = totalBaseHT - totalRetenue;
        double tauxTva      = dto.getTauxTva() != null ? dto.getTauxTva() : 0.0;
        double totalTva     = totalMT * (tauxTva / 100);
        double totalTTC     = totalMT + totalTva;

        commande.setHt(totalBaseHT);
        commande.setRetenue(totalRetenue);
        commande.setMt(totalMT);
        commande.setTva(tauxTva);
        commande.setMtTtc(totalTTC);

        // --- Lignes JSON ---
        try {
            String lignesJson = objectMapper.writeValueAsString(dto.getLignes());
            commande.setLignesJson(lignesJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Erreur conversion lignes en JSON");
        }

        // --- Recalcul avance / net / statut ---
        double totalDejaPaye = paiementCommandeRepository.sumMontantByCommandeId(id);
        double reste = totalTTC - totalDejaPaye;
        commande.setAvance(totalDejaPaye);
        commande.setNet(Math.max(reste, 0));
        commande.setStatut(reste <= 0 ? "PAYEE" : "IMPAYEE");

        Commande saved = commandeRepository.save(commande);
        return mapCommandeToDto(saved);
    }

    // ----------------------------
    // HELPERS
    // ----------------------------
//    private String generateRef(Long id) {
//        return String.format("%05d", id);
//    }

    public String generateRef(Long id) {

        int numero = 1;

        while (commandeRepository.existsByReference(
                "FA-2026-" + String.format("%06d", numero))) {
            numero++;
        }

        return "FA-2026-" + String.format("%06d", numero);
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
        dto.setNom(user.getNom());           // ✅ ajout
        dto.setPrenom(user.getPrenom());     // ✅ ajout
        dto.setNumeroTelephone(user.getNumeroTelephone()); // ✅ ajout
        return dto;
    }


}
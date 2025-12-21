package com.pro.Facture.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pro.Facture.Dto.*;
import com.pro.Facture.Entity.Client;
import com.pro.Facture.Entity.Commande;
import com.pro.Facture.Entity.Place;
import com.pro.Facture.repository.ClientRepository;
import com.pro.Facture.repository.CommandeRepository;
import com.pro.Facture.repository.PlaceRepository;
import com.pro.Facture.service.Pdf.CommandePdfService;
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
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON mapper

    public CommandeService(CommandeRepository commandeRepository,
                           ClientRepository clientRepository,
                           CommandePdfService commandePdfService,
                           PlaceRepository placeRepository) {

        this.commandeRepository = commandeRepository;
        this.clientRepository = clientRepository;
        this.commandePdfService = commandePdfService;
        this.placeRepository = placeRepository;
    }

    // ----------------------------
    // CREATE FACTURE
    // ----------------------------
    public CommandeResponseDto createCommande(CommandeRequestDto dto) {

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client introuvable"));

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
        double totalTva = totalBaseHT * 0.18;
        double totalTTC = totalMT + totalTva;
        double totalNetAPayer = totalTTC - dto.getAvance();

        Commande commande = new Commande();
        commande.setClient(client);
        commande.setDateFacture(dto.getDateFacture());
        commande.setDesign("FACTURE MULTI-LIGNES");
        commande.setHt(totalBaseHT);
        commande.setRetenue(totalRetenue);
        commande.setMt(totalMT);
        commande.setTva(totalTva);
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

        Commande saved = commandeRepository.save(commande);
        saved.setRef(generateRef(saved.getId()));
        commandeRepository.save(saved);

        CommandeResponseDto response = new CommandeResponseDto();
        response.setId(Math.toIntExact(saved.getId())); // ✅
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


        return response;
    }

    private String generateRef(Long id) {
        return String.format("ref-%05d", id);
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

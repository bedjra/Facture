package com.pro.Facture.service;

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
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final ClientRepository clientRepository;
    private final CommandePdfService commandePdfService;
    private final PlaceRepository placeRepository;

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

        // Récupération client
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client introuvable"));

        double totalBaseHT = 0.0;
        List<LigneCommandeResponseDto> lignes = new ArrayList<>();

        // ----------------------------
        // Calcul des lignes
        // ----------------------------
        for (LigneCommandeDto l : dto.getLignes()) {

            double base = l.getHt();
            totalBaseHT += base;

            LigneCommandeResponseDto res = new LigneCommandeResponseDto();
            res.setDesign(l.getDesign());
            res.setBaseHT(base);

            lignes.add(res);
        }

        // ----------------------------
        // Calculs globaux (CORRECTS)
        // ----------------------------
        double totalRetenue = totalBaseHT * (dto.getRetenue() / 100);
        double totalMT = totalBaseHT - totalRetenue;      // MT
        double totalTva = totalBaseHT * 0.18;             // TVA sur BASE
        double totalTTC = totalMT + totalTva;             // MT + TVA
        double totalNetAPayer = totalTTC - dto.getAvance();

        // ----------------------------
        // Sauvegarde commande
        // ----------------------------
        Commande commande = new Commande();
        commande.setClient(client);
        commande.setDateFacture(dto.getDateFacture());

        commande.setDesign("FACTURE MULTI-LIGNES");
        commande.setHt(totalBaseHT);
        commande.setRetenue(totalRetenue);
        commande.setMt(totalMT);            // ✅ ICI
        commande.setTva(totalTva);
        commande.setMtTtc(totalTTC);
        commande.setAvance(dto.getAvance());
        commande.setNet(totalNetAPayer);

        // Save initial
        Commande saved = commandeRepository.save(commande);

        // Génération REF
        saved.setRef(generateRef(saved.getId()));
        commandeRepository.save(saved);

        // ----------------------------
        // Construction réponse
        // ----------------------------
        CommandeResponseDto response = new CommandeResponseDto();
        response.setRef(saved.getRef());
        response.setDateFacture(saved.getDateFacture());
        response.setClient(mapClient(client));
        response.setLignes(lignes);

        response.setTotalBaseHT(totalBaseHT);
        response.setTotalRetenue(totalRetenue);
        response.setTotalHTNet(totalMT);    // ✅ MT
        response.setTotalTva(totalTva);
        response.setTotalTTC(totalTTC);
        response.setTotalAvance(dto.getAvance());
        response.setTotalNetAPayer(totalNetAPayer);

        return response;
    }

    // ----------------------------
    // REFERENCE AUTO "00001"
    // ----------------------------
    private String generateRef(Long id) {
        return String.format("ref-%05d", id);
    }

    // ----------------------------
    // MAP CLIENT
    // ----------------------------
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
        // Récupérer la commande
        Commande cmd = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        // Mapper la commande vers le DTO
        CommandeResponseDto dto = mapCommandeToDto(cmd);

        // 🔹 Récupérer le Place depuis la base
        Place place = placeRepository.findById(1L) // ou une logique pour récupérer le bon Place
                .orElseThrow(() -> new RuntimeException("Place introuvable"));

        // Générer le PDF et le convertir en Base64
        byte[] pdfBytes = commandePdfService.genererPdf(dto, place);
        String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);
        dto.setPdfBase64(pdfBase64);

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

        dto.setRef(cmd.getRef());
        dto.setDateFacture(cmd.getDateFacture());
        dto.setTotalBaseHT(cmd.getHt());
        dto.setTotalRetenue(cmd.getRetenue());
        dto.setTotalHTNet(cmd.getMt());
        dto.setTotalTva(cmd.getTva());
        dto.setTotalTTC(cmd.getMtTtc());
        dto.setTotalAvance(cmd.getAvance());
        dto.setTotalNetAPayer(cmd.getNet());

        // client
        if (cmd.getClient() != null) {
            dto.setClient(mapClient(cmd.getClient()));
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

}

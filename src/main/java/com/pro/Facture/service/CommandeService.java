package com.pro.Facture.service;

import com.pro.Facture.Dto.ClientDto;
import com.pro.Facture.Dto.CommandeRequestDto;
import com.pro.Facture.Dto.CommandeResponseDto;
import com.pro.Facture.Dto.LigneCommandeResponseDto;
import com.pro.Facture.Entity.Client;
import com.pro.Facture.Entity.Commande;
import com.pro.Facture.repository.ClientRepository;
import com.pro.Facture.repository.CommandeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final ClientRepository clientRepository;

    public CommandeService(CommandeRepository commandeRepository, ClientRepository clientRepository) {
        this.commandeRepository = commandeRepository;
        this.clientRepository = clientRepository;
    }

    // ----------------------------
    // CREATE FACTURE
    // ----------------------------
    public CommandeResponseDto createCommande(CommandeRequestDto dto) {

        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client introuvable"));

        double totalBaseHT = 0.0;

        List<LigneCommandeResponseDto> lignes = new ArrayList<>();

        // Calcul des lignes (HT uniquement)
        for (var l : dto.getLignes()) {

            double base = l.getHt();
            totalBaseHT += base;

            LigneCommandeResponseDto res = new LigneCommandeResponseDto();
            res.setDesign(l.getDesign());
            res.setBaseHT(base);

            lignes.add(res);
        }

        // Calculs globaux
        double totalRetenue = totalBaseHT * (dto.getRetenue() / 100);
        double totalHTNet = totalBaseHT - totalRetenue;
        double totalTva = totalBaseHT * 0.18;
        double totalTTC = totalBaseHT + totalTva;
        double totalNetAPayer = totalTTC - dto.getAvance();

        // Sauvegarde commande
        Commande commande = new Commande();
        commande.setDesign("FACTURE MULTI-LIGNES");
        commande.setHt(totalBaseHT);
        commande.setRetenue(totalRetenue);
        commande.setMt(totalHTNet);
        commande.setTva(totalTva);
        commande.setMtTtc(totalTTC);
        commande.setAvance(dto.getAvance());
        commande.setNet(totalNetAPayer);

        Commande saved = commandeRepository.save(commande);
        saved.setRef(generateRef(saved.getId()));
        commandeRepository.save(saved);

        // Construction réponse
        CommandeResponseDto response = new CommandeResponseDto();
        response.setRef(saved.getRef());
        response.setDateFacture(dto.getDateFacture());
        response.setClient(mapClient(client));
        response.setLignes(lignes);

        response.setTotalBaseHT(totalBaseHT);
        response.setTotalRetenue(totalRetenue);
        response.setTotalHTNet(totalHTNet);
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
        return String.format("%05d", id);
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
        Commande cmd = commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable"));

        return mapCommandeToDto(cmd);
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

}

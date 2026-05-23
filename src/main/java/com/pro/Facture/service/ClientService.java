package com.pro.Facture.service;

import com.pro.Facture.Dto.ClientDto;
import com.pro.Facture.Dto.PaiementCommandeDto;
import com.pro.Facture.Entity.Client;
import com.pro.Facture.Entity.PaiementCommande;
import com.pro.Facture.repository.ClientRepository;
import com.pro.Facture.repository.CommandeRepository;
import com.pro.Facture.repository.PaiementCommandeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final CommandeRepository commandeRepository;
    private final PaiementCommandeRepository paiementCommandeRepository;

    // =========================
    // CONSTRUCTEUR
    // =========================

    public ClientService(ClientRepository clientRepository,
                         CommandeRepository commandeRepository, CommandeRepository commandeRepository1,
                         PaiementCommandeRepository paiementCommandeRepository) {

        this.clientRepository = clientRepository;
        this.commandeRepository = commandeRepository1;
        this.paiementCommandeRepository = paiementCommandeRepository;
    }

    // =========================
    // ENTITY -> DTO
    // =========================

    private ClientDto mapToDto(Client client) {

        return new ClientDto(
                client.getId(),
                client.getNom(),
                client.getNIF(),
                client.getTelephone(),
                client.getAdresse(),
                client.getStatutClient(),
                client.getDate()
        );
    }

    // =========================
    // DTO -> ENTITY
    // =========================

    private Client mapToEntity(ClientDto dto) {

        Client client = new Client();

        client.setId(dto.getId());
        client.setNom(dto.getNom());
        client.setNIF(dto.getNIF());
        client.setTelephone(dto.getTelephone());
        client.setAdresse(dto.getAdresse());
        client.setStatutClient(dto.getStatutClient());
        client.setDate(dto.getDate());

        return client;
    }

    // =========================
    // CREATE
    // =========================

    public ClientDto create(ClientDto dto) {

        Client client = mapToEntity(dto);

        Client saved = clientRepository.save(client);

        return mapToDto(saved);
    }

    // =========================
    // UPDATE
    // =========================

    public ClientDto update(Long id, ClientDto dto) {

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));

        client.setNom(dto.getNom());
        client.setNIF(dto.getNIF());
        client.setTelephone(dto.getTelephone());
        client.setAdresse(dto.getAdresse());
        client.setStatutClient(dto.getStatutClient());
        client.setDate(dto.getDate());

        Client updated = clientRepository.save(client);

        return mapToDto(updated);
    }

    // =========================
    // GET BY ID
    // =========================

    public ClientDto getById(Long id) {

        return clientRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
    }

    // =========================
    // GET ALL
    // =========================

    public List<ClientDto> getAll() {

        return clientRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // =========================
    // DELETE
    // =========================

    public void delete(Long id) {

        if (!clientRepository.existsById(id)) {
            throw new RuntimeException("Client introuvable");
        }

        clientRepository.deleteById(id);
    }

    // =========================
    // PAIEMENT -> DTO
    // =========================

    private PaiementCommandeDto mapPaiementToDto(PaiementCommande p) {

        return new PaiementCommandeDto(
                p.getId(),
                p.getMontant(),
                p.getDatePaiement(),
                p.getCommande().getId()
        );
    }

    // =========================
    // HISTORIQUE PAIEMENTS
    // =========================

    public Map<String, Object> getHistoriquePaiements(Long clientId) {

        clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));

        List<PaiementCommandeDto> paiements = paiementCommandeRepository.findByClientId(clientId)
                .stream()
                .map(this::mapPaiementToDto)
                .collect(Collectors.toList());

        double totalMontantPaye = paiements.stream()
                .mapToDouble(p -> p.getMontantPaye() != null ? p.getMontantPaye() : 0.0)
                .sum();

        double totalResteAPayer = commandeRepository.findByClientId(clientId)
                .stream()
                .mapToDouble(c -> c.getNet() != null ? c.getNet() : 0.0)
                .sum();

        return Map.of(
                "paiements", paiements,
                "totalMontantPaye", totalMontantPaye,
                "totalResteAPayer", totalResteAPayer
        );
    }
}
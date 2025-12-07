package com.pro.Facture.service;

import com.pro.Facture.Dto.ClientDto;
import com.pro.Facture.Entity.Client;
import com.pro.Facture.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    // Convertir Entity -> DTO
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

    // Convertir DTO -> Entity
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

    // CREATE
    public ClientDto create(ClientDto dto) {
        Client client = mapToEntity(dto);
        Client saved = clientRepository.save(client);
        return mapToDto(saved);
    }

    // UPDATE
    public ClientDto update(Long id, ClientDto dto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));

        client.setNom(dto.getNom());
        client.setNIF(dto.getNIF());
        client.setTelephone(dto.getTelephone());
        client.setAdresse(dto.getAdresse());
        client.setStatutClient(dto.getStatutClient());

        Client updated = clientRepository.save(client);
        return mapToDto(updated);
    }

    // GET by ID
    public ClientDto getById(Long id) {
        return clientRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Client introuvable"));
    }

    // GET ALL
    public List<ClientDto> getAll() {
        return clientRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // DELETE
    public void delete(Long id) {
        clientRepository.deleteById(id);
    }
}

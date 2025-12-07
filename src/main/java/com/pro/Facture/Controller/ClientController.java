package com.pro.Facture.Controller;

import com.pro.Facture.Dto.ClientDto;
import com.pro.Facture.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client")
@CrossOrigin(origins = "*")
@Tag(name = "Client", description = "Gestion des clients dans l'application")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @Operation(summary = "Créer un client")
    @PostMapping
    public ClientDto create(@RequestBody ClientDto dto) {
        return clientService.create(dto);
    }

    @Operation(summary = "Modifier un client")
    @PutMapping("/{id}")
    public ClientDto update(@PathVariable Long id, @RequestBody ClientDto dto) {
        return clientService.update(id, dto);
    }

    @Operation(summary = "Récupérer un client")
    @GetMapping("/{id}")
    public ClientDto getById(@PathVariable Long id) {
        return clientService.getById(id);
    }

    @Operation(summary = "Lister les clients")
    @GetMapping
    public List<ClientDto> getAll() {
        return clientService.getAll();
    }

    @Operation(summary = "Supprimer un client")
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        clientService.delete(id);
        return "Client supprimé avec succès";
    }
}

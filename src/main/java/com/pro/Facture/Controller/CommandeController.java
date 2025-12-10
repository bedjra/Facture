package com.pro.Facture.Controller;

import com.pro.Facture.Dto.CommandeRequestDto;
import com.pro.Facture.Dto.CommandeResponseDto;
import com.pro.Facture.service.CommandeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/commande")
public class CommandeController {

    private final CommandeService commandeService;

    public CommandeController(CommandeService commandeService) {
        this.commandeService = commandeService;
    }

    // ----------------------------
    // POST - CREER UNE FACTURE
    // ----------------------------
    @PostMapping
    public ResponseEntity<CommandeResponseDto> createCommande(@RequestBody CommandeRequestDto dto) {
        CommandeResponseDto response = commandeService.createCommande(dto);
        return ResponseEntity.ok(response);
    }

}

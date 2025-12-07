package com.pro.Facture.Controller;


import com.pro.Facture.Dto.PlaceDto;
import com.pro.Facture.Entity.Place;

import com.pro.Facture.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/place")
@Tag(name = "Place", description = "Gestion des informations de l'établissement")
@SecurityRequirement(name = "bearerAuth") // 🔥 JWT obligatoire
public class PlaceController {

    private final PlaceService placeService;

    public PlaceController(PlaceService placeService) {
        this.placeService = placeService;
    }

    @PostMapping
    @Operation(summary = "Créer une place")
    public Place create(@RequestBody PlaceDto dto) {
        return placeService.createPlace(dto);
    }

    @GetMapping
    @Operation(summary = "Liste des places")
    public List<Place> getAll() {
        return placeService.getAllPlaces();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détails d'une place")
    public Place getOne(@PathVariable Long id) {
        return placeService.getPlace(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une place")
    public Place update(@PathVariable Long id, @RequestBody PlaceDto dto) {
        return placeService.updatePlace(id, dto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une place")
    public void delete(@PathVariable Long id) {
        placeService.deletePlace(id);
    }
}

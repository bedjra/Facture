package com.pro.Facture.service;

import com.pro.Facture.Dto.RecuDto;
import com.pro.Facture.Entity.Place;
import com.pro.Facture.Entity.Recu;
import com.pro.Facture.repository.PlaceRepository;
import com.pro.Facture.repository.RecuRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecuService {

    private final RecuRepository recuRepository;

    private final PlaceRepository placeRepository;


    public RecuService(RecuRepository recuRepository, PlaceRepository placeRepository) {
        this.recuRepository = recuRepository;
        this.placeRepository = placeRepository;
    }

    // =========================
    // CREATE
    // =========================

    public RecuDto create(RecuDto dto) {
        Recu recu = mapToEntity(dto);

        // ✅ Récupérer la première place et l'associer
        Place place = placeRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("Aucune place configurée en base"));
        recu.setPlace(place);

        recu = recuRepository.save(recu);
        return mapToDto(recu);
    }


    // =========================
    // READ BY ID
    // =========================
    public RecuDto getById(Long id) {
        Recu recu = recuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reçu non trouvé"));
        return mapToDto(recu);
    }

    // =========================
    // READ ALL
    // =========================
    public List<RecuDto> getAll() {
        return recuRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // =========================
    // UPDATE
    // =========================
    public RecuDto update(Long id, RecuDto dto) {
        Recu recu = recuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reçu non trouvé"));

        recu.setNumeroPiece(dto.getNumeroPiece());
        recu.setDate(dto.getDate());
        recu.setBeneficiaire(dto.getBeneficiaire());
        recu.setMontantEncaisse(dto.getMontantEncaisse());
        recu.setMode(dto.getMode());
        recu.setMotif(dto.getMotif());

        recu = recuRepository.save(recu);
        return mapToDto(recu);
    }

    // =========================
    // DELETE
    // =========================
    public void delete(Long id) {
        Recu recu = recuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reçu non trouvé"));
        recuRepository.delete(recu);
    }

    // =========================
    // MAPPER
    // =========================
    private RecuDto mapToDto(Recu recu) {
        RecuDto dto = new RecuDto();
        dto.setId(recu.getId());
        dto.setNumeroPiece(recu.getNumeroPiece());
        dto.setDate(recu.getDate());
        dto.setBeneficiaire(recu.getBeneficiaire());
        dto.setMontantEncaisse(recu.getMontantEncaisse());
        dto.setMode(recu.getMode());
        dto.setMotif(recu.getMotif());
        return dto;
    }

    private Recu mapToEntity(RecuDto dto) {
        Recu recu = new Recu();
        recu.setNumeroPiece(dto.getNumeroPiece());
        recu.setDate(dto.getDate());
        recu.setBeneficiaire(dto.getBeneficiaire());
        recu.setMontantEncaisse(dto.getMontantEncaisse());
        recu.setMode(dto.getMode());
        recu.setMotif(dto.getMotif());
        return recu;
    }
}

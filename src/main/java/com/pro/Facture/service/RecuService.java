package com.pro.Facture.service;

import com.pro.Facture.Dto.RecuDto;
import com.pro.Facture.Dto.UtilisateurDto;
import com.pro.Facture.Entity.Place;
import com.pro.Facture.Entity.Recu;
import com.pro.Facture.Entity.Utilisateur;
import com.pro.Facture.repository.PlaceRepository;
import com.pro.Facture.repository.RecuRepository;
import com.pro.Facture.repository.UtilisateurRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecuService {

    private final RecuRepository recuRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PlaceRepository placeRepository;

    public RecuService(RecuRepository recuRepository,
                       UtilisateurRepository utilisateurRepository,
                       PlaceRepository placeRepository) {
        this.recuRepository = recuRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.placeRepository = placeRepository;
    }

    // =========================
    // CREATE
    // =========================
    public RecuDto create(RecuDto dto) {

        Recu recu = mapToEntity(dto);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        recu.setUtilisateur(user);

        Place place = placeRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new RuntimeException("Aucune place configurée"));
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
        recu.setNumBenef(dto.getNumBenef());           // ← nouveau
        recu.setMontantEncaisse(dto.getMontantEncaisse());
        recu.setMontantTotal(dto.getMontantTotal());
        recu.setReste(dto.getReste());
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
    // MAPPER ENTITY
    // =========================
    private Recu mapToEntity(RecuDto dto) {

        Recu recu = new Recu();

        recu.setNumeroPiece(dto.getNumeroPiece());
        recu.setDate(dto.getDate());
        recu.setBeneficiaire(dto.getBeneficiaire());
        recu.setNumBenef(dto.getNumBenef());           // ← nouveau
        recu.setMontantEncaisse(dto.getMontantEncaisse());
        recu.setMontantTotal(dto.getMontantTotal());
        recu.setReste(dto.getReste());
        recu.setMode(dto.getMode());
        recu.setMotif(dto.getMotif());

        return recu;
    }

    // =========================
    // MAPPER DTO
    // =========================
    private RecuDto mapToDto(Recu recu) {

        RecuDto dto = new RecuDto();

        dto.setId(recu.getId());

        String numeroAffichage = String.format(
                "%03d/CFACI/%d",
                recu.getId(),
                Year.now().getValue()
        );
        dto.setNumeroPieceAffichage(numeroAffichage);

        dto.setNumeroPiece(recu.getNumeroPiece());
        dto.setDate(recu.getDate());
        dto.setBeneficiaire(recu.getBeneficiaire());
        dto.setNumBenef(recu.getNumBenef());           // ← nouveau
        dto.setMontantEncaisse(recu.getMontantEncaisse());
        dto.setMontantTotal(recu.getMontantTotal());
        dto.setReste(recu.getReste());
        dto.setMode(recu.getMode());
        dto.setMotif(recu.getMotif());

        if (recu.getUtilisateur() != null) {
            dto.setUtilisateur(mapUtilisateur(recu.getUtilisateur()));
        }

        return dto;
    }

    // =========================
    // MAPPER UTILISATEUR
    // =========================

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
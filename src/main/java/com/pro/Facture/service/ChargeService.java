package com.pro.Facture.service;

import com.pro.Facture.Dto.ChargeDTO;
import com.pro.Facture.Entity.Charge;
import com.pro.Facture.repository.ChargeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChargeService {

    private final ChargeRepository chargeRepository;

    public ChargeService(ChargeRepository chargeRepository) {
        this.chargeRepository = chargeRepository;
    }

    // CREATE
    public ChargeDTO create(ChargeDTO dto) {
        Charge charge = mapToEntity(dto);
        return mapToDTO(chargeRepository.save(charge));
    }

    // READ ALL
    public List<ChargeDTO> getAll() {
        return chargeRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // READ BY ID
    public ChargeDTO getById(Long id) {
        Charge charge = chargeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charge non trouvée"));
        return mapToDTO(charge);
    }

    // UPDATE
    public ChargeDTO update(Long id, ChargeDTO dto) {
        Charge charge = chargeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Charge non trouvée"));

        charge.setDescription(dto.getDescription());
        charge.setMontant(dto.getMontant());
        charge.setDateCharge(dto.getDateCharge());

        return mapToDTO(chargeRepository.save(charge));
    }

    // DELETE
    public void delete(Long id) {
        chargeRepository.deleteById(id);
    }

    // ===== MAPPING =====

    private ChargeDTO mapToDTO(Charge charge) {
        return new ChargeDTO(
                charge.getId(),
                charge.getDescription(),
                charge.getMontant(),
                charge.getDateCharge(),
                null
        );
    }

    private Charge mapToEntity(ChargeDTO dto) {
        Charge charge = new Charge();
        charge.setId(dto.getId());
        charge.setDescription(dto.getDescription());
        charge.setMontant(dto.getMontant());
        charge.setDateCharge(dto.getDateCharge());
        return charge;
    }

    public Double getTotalCharges() {
        return chargeRepository.getTotalCharges();
    }
}
package com.pro.Facture.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ChargeDTO {

    private Long id;
    private String description;
    private String beneficiaire;
    private String motif;
    private BigDecimal montant;
    private LocalDate dateCharge;

    public ChargeDTO() {}

    public ChargeDTO(Long id, String description, String beneficiaire, String motif,
                     BigDecimal montant, LocalDate dateCharge) {
        this.id = id;
        this.description = description;
        this.beneficiaire = beneficiaire;
        this.motif = motif;
        this.montant = montant;
        this.dateCharge = dateCharge;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBeneficiaire() { return beneficiaire; }
    public void setBeneficiaire(String beneficiaire) { this.beneficiaire = beneficiaire; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public LocalDate getDateCharge() { return dateCharge; }
    public void setDateCharge(LocalDate dateCharge) { this.dateCharge = dateCharge; }
}
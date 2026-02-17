package com.pro.Facture.Dto;

import com.pro.Facture.enums.ModePaiement;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RecuDto {

    private Long id;

    @NotBlank(message = "Le numéro de pièce est obligatoire")
    private String numeroPiece;

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;

    @NotBlank(message = "Le bénéficiaire est obligatoire")
    private String beneficiaire;

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être supérieur à 0")
    private BigDecimal montantEncaisse;

    @NotNull(message = "Le mode de paiement est obligatoire")
    private ModePaiement mode;

    private String motif;

    // =========================
    // Constructeurs
    // =========================

    public RecuDto() {
    }

    public RecuDto(Long id, String numeroPiece, LocalDate date, String beneficiaire,
                   BigDecimal montantEncaisse, ModePaiement mode, String motif) {
        this.id = id;
        this.numeroPiece = numeroPiece;
        this.date = date;
        this.beneficiaire = beneficiaire;
        this.montantEncaisse = montantEncaisse;
        this.mode = mode;
        this.motif = motif;
    }

    // =========================
    // Getters & Setters
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroPiece() {
        return numeroPiece;
    }

    public void setNumeroPiece(String numeroPiece) {
        this.numeroPiece = numeroPiece;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getBeneficiaire() {
        return beneficiaire;
    }

    public void setBeneficiaire(String beneficiaire) {
        this.beneficiaire = beneficiaire;
    }

    public BigDecimal getMontantEncaisse() {
        return montantEncaisse;
    }

    public void setMontantEncaisse(BigDecimal montantEncaisse) {
        this.montantEncaisse = montantEncaisse;
    }

    public ModePaiement getMode() {
        return mode;
    }

    public void setMode(ModePaiement mode) {
        this.mode = mode;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }
}

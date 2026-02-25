package com.pro.Facture.Dto;

import com.pro.Facture.enums.ModePaiement;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class RecuDto {

    private Long id;

    private String numeroPieceAffichage;  // valeur générée pour le PDF


    private String numeroPiece;

    private LocalDate date;

    private String beneficiaire;

    private BigDecimal montantEncaisse;

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

    private UtilisateurDto utilisateur;


    // =========================
    // Getters & Setters
    // =========================




}

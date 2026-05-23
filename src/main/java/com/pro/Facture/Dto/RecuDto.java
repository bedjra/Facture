package com.pro.Facture.Dto;

import com.pro.Facture.enums.ModePaiement;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RecuDto {

    private Long id;

    private String numeroPieceAffichage; // valeur générée pour le PDF

    private String numeroPiece;

    private LocalDate date;

    private String beneficiaire;

    private BigDecimal montantEncaisse;

    // Nouveau champ
    private BigDecimal montantTotal;

    // Nouveau champ
    private BigDecimal reste;

    private ModePaiement mode;

    private String motif;

    private UtilisateurDto utilisateur;

    // =========================
    // Constructeurs
    // =========================

    public RecuDto() {
    }

    public RecuDto(Long id,
                   String numeroPiece,
                   LocalDate date,
                   String beneficiaire,
                   BigDecimal montantEncaisse,
                   BigDecimal montantTotal,
                   BigDecimal reste,
                   ModePaiement mode,
                   String motif) {

        this.id = id;
        this.numeroPiece = numeroPiece;
        this.date = date;
        this.beneficiaire = beneficiaire;
        this.montantEncaisse = montantEncaisse;
        this.montantTotal = montantTotal;
        this.reste = reste;
        this.mode = mode;
        this.motif = motif;
    }
}
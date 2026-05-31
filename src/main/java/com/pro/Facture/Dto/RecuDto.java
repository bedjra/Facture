package com.pro.Facture.Dto;

import com.pro.Facture.enums.ModePaiement;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RecuDto {

    private Long id;
    private String numeroPieceAffichage;
    private String numeroPiece;
    private LocalDate date;
    private String beneficiaire;
    private String numBenef;          // ← nouveau
    private BigDecimal montantEncaisse;
    private BigDecimal montantTotal;
    private BigDecimal reste;
    private ModePaiement mode;
    private String motif;
    private UtilisateurDto utilisateur;

    public RecuDto() {}

    public RecuDto(Long id,
                   String numeroPiece,
                   LocalDate date,
                   String beneficiaire,
                   String numBenef,          // ← nouveau
                   BigDecimal montantEncaisse,
                   BigDecimal montantTotal,
                   BigDecimal reste,
                   ModePaiement mode,
                   String motif) {
        this.id = id;
        this.numeroPiece = numeroPiece;
        this.date = date;
        this.beneficiaire = beneficiaire;
        this.numBenef = numBenef;             // ← nouveau
        this.montantEncaisse = montantEncaisse;
        this.montantTotal = montantTotal;
        this.reste = reste;
        this.mode = mode;
        this.motif = motif;
    }
}
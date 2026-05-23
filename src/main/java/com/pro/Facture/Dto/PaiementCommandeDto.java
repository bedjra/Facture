package com.pro.Facture.Dto;

import java.time.LocalDate;

public class PaiementCommandeDto {

    private Long id;
    private Double montantPaye;  // ✅ renommé
    private LocalDate datePaiement;
    private Long commandeId;

    public PaiementCommandeDto() {}

    public PaiementCommandeDto(Long id, Double montantPaye, LocalDate datePaiement, Long commandeId) {
        this.id = id;
        this.montantPaye = montantPaye;
        this.datePaiement = datePaiement;
        this.commandeId = commandeId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getMontantPaye() { return montantPaye; }       // ✅
    public void setMontantPaye(Double montantPaye) { this.montantPaye = montantPaye; }  // ✅

    public LocalDate getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDate datePaiement) { this.datePaiement = datePaiement; }

    public Long getCommandeId() { return commandeId; }
    public void setCommandeId(Long commandeId) { this.commandeId = commandeId; }
}
package com.pro.Facture.Dto;

import java.time.LocalDate;
import java.util.List;

public class CommandeRequestDto {

    private Long clientId;
    private LocalDate dateFacture;
    private Double retenue;      // % Retenue à source
    private Double avance;
    private List<LigneCommandeDto> lignes;

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public LocalDate getDateFacture() {
        return dateFacture;
    }

    public void setDateFacture(LocalDate dateFacture) {
        this.dateFacture = dateFacture;
    }

    public List<LigneCommandeDto> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneCommandeDto> lignes) {
        this.lignes = lignes;
    }

    public Double getRetenue() {
        return retenue;
    }

    public void setRetenue(Double retenue) {
        this.retenue = retenue;
    }

    public Double getAvance() {
        return avance;
    }

    public void setAvance(Double avance) {
        this.avance = avance;
    }
}

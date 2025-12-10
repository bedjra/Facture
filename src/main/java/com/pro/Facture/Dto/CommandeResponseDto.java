package com.pro.Facture.Dto;


import java.time.LocalDate;
import java.util.List;

public class CommandeResponseDto {

    private String ref;
    private LocalDate dateFacture;

    private ClientDto client;

    private List<LigneCommandeResponseDto> lignes;

    private Double totalBaseHT;
    private Double totalRetenue;
    private Double totalHTNet;
    private Double totalTva;
    private Double totalTTC;
    private Double totalAvance;
    private Double totalNetAPayer;

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public LocalDate getDateFacture() {
        return dateFacture;
    }

    public void setDateFacture(LocalDate dateFacture) {
        this.dateFacture = dateFacture;
    }

    public ClientDto getClient() {
        return client;
    }

    public void setClient(ClientDto client) {
        this.client = client;
    }

    public List<LigneCommandeResponseDto> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneCommandeResponseDto> lignes) {
        this.lignes = lignes;
    }

    public Double getTotalBaseHT() {
        return totalBaseHT;
    }

    public void setTotalBaseHT(Double totalBaseHT) {
        this.totalBaseHT = totalBaseHT;
    }

    public Double getTotalRetenue() {
        return totalRetenue;
    }

    public void setTotalRetenue(Double totalRetenue) {
        this.totalRetenue = totalRetenue;
    }

    public Double getTotalHTNet() {
        return totalHTNet;
    }

    public void setTotalHTNet(Double totalHTNet) {
        this.totalHTNet = totalHTNet;
    }

    public Double getTotalTva() {
        return totalTva;
    }

    public void setTotalTva(Double totalTva) {
        this.totalTva = totalTva;
    }

    public Double getTotalTTC() {
        return totalTTC;
    }

    public void setTotalTTC(Double totalTTC) {
        this.totalTTC = totalTTC;
    }

    public Double getTotalAvance() {
        return totalAvance;
    }

    public void setTotalAvance(Double totalAvance) {
        this.totalAvance = totalAvance;
    }

    public Double getTotalNetAPayer() {
        return totalNetAPayer;
    }

    public void setTotalNetAPayer(Double totalNetAPayer) {
        this.totalNetAPayer = totalNetAPayer;
    }
}

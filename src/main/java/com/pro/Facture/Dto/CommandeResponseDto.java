package com.pro.Facture.Dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommandeResponseDto {

    private int id;
    private String ref;
    private LocalDate dateFacture;

    private ClientDto client;
    private String pdfBase64;

//    private List<LigneCommandeResponseDto> lignes;
private List<LigneCommandeResponseDto> lignes = new ArrayList<>();

    private Double totalBaseHT;
    private Double totalRetenue;
    private Double totalHTNet;
    private Double totalTva;
    private Double totalTTC;
    private Double totalAvance;
    private Double totalNetAPayer;



}

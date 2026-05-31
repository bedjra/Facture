package com.pro.Facture.Dto;

import lombok.Data;

import java.time.LocalDate;
@Data

public class PaiementCommandeResponseDto {
    private Long id;
    private double montant;
    private LocalDate datePaiement;


}

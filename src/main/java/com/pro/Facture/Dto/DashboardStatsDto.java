package com.pro.Facture.Dto;

import lombok.Data;

@Data
public class DashboardStatsDto {

    // TOTAL FACTURÉ
    private double totalFacture;
    private long nombreFactures;
    private double evolutionFacture; // %

    // TOTAL PAYÉ
    private double totalPaye;
    private long nombreFacturesPayees;
    private double evolutionPaye; // %

    // IMPAYÉ
    private double totalImpaye;

    // EN RETARD
    private long facturesEnRetard;
}

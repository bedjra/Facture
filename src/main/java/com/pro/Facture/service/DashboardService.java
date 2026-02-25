package com.pro.Facture.service;

import com.pro.Facture.repository.ChargeRepository;
import com.pro.Facture.repository.CommandeRepository;
import com.pro.Facture.repository.PaiementCommandeRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final CommandeRepository commandeRepository;
    private final ChargeRepository chargeRepository;
    private final PaiementCommandeRepository paiementCommandeRepository;

    public DashboardService(CommandeRepository commandeRepository, ChargeRepository chargeRepository, PaiementCommandeRepository paiementCommandeRepository) {
        this.commandeRepository = commandeRepository;
        this.chargeRepository = chargeRepository;
        this.paiementCommandeRepository = paiementCommandeRepository;
    }

    public Map<String, Object> getTotalFacture() {

        double total = commandeRepository.totalFacture();
        long count = commandeRepository.nombreFactures();

        double moisActuel = commandeRepository.totalFactureMoisActuel();
        double moisPrecedent = commandeRepository.totalFactureMoisPrecedent();

        double evolution = moisPrecedent == 0
                ? 100
                : ((moisActuel - moisPrecedent) / moisPrecedent) * 100;

        Map<String, Object> res = new HashMap<>();
        res.put("totalFacture", total);
        res.put("nombreFactures", count);
        res.put("evolutionFacture", Math.round(evolution));

        return res;
    }


    public Map<String, Object> getTotalPaye() {

        Map<String, Object> res = new HashMap<>();
        res.put("totalPaye", commandeRepository.totalPaye());
        res.put("nombreFacturesPayees", commandeRepository.nombreFacturesPayees());
        res.put("evolutionPaye", 100);
        return res;
    }


    public Map<String, Object> getTotalImpaye() {

        return Map.of(
                "totalImpaye", commandeRepository.totalImpaye()
        );
    }


    public Map<String, Object> getFacturesEnRetard() {

        return Map.of(
                "facturesEnRetard", commandeRepository.facturesEnRetard()
        );
    }


     // API 1 : Nombre total de factures
    public Long getNombreFactures() {
        return commandeRepository.countFactures();
    }

    // API 2 : Somme totale des factures
    public Double getTotalFactures() {
        return commandeRepository.sumFactures();
    }


    public Double getBenefice() {
        Double totalFactures = commandeRepository.sumFactures();
        Double totalCharges = chargeRepository.getTotalCharges();
        return totalFactures - totalCharges;
    }

    // ----------------------------
    // CA JOUR
    // ----------------------------
    public Double getCaJour() {
        return paiementCommandeRepository.caJour();
    }

    // ----------------------------
// CA HEBDOMADAIRE
// ----------------------------
    public Double getCaHebdo() {

        LocalDate today = LocalDate.now();

        LocalDate lundi = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate dimanche = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        Double result = paiementCommandeRepository.caBetween(lundi, dimanche);

        return result != null ? result : 0.0;
    }


    // ----------------------------
// CA MENSUEL
// ----------------------------
    public Double getCaMois() {

        LocalDate today = LocalDate.now();

        LocalDate debutMois = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate finMois = today.with(TemporalAdjusters.lastDayOfMonth());

        Double result = paiementCommandeRepository.caBetween(debutMois, finMois);

        return result != null ? result : 0.0;
    }
}
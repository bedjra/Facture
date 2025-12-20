package com.pro.Facture.service;

import com.pro.Facture.repository.CommandeRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final CommandeRepository commandeRepository;

    public DashboardService(CommandeRepository commandeRepository) {
        this.commandeRepository = commandeRepository;
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

}

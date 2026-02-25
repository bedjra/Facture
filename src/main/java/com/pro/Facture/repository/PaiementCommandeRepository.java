package com.pro.Facture.repository;


import com.pro.Facture.Entity.PaiementCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PaiementCommandeRepository extends JpaRepository<PaiementCommande, Long> {

    // 🔹 Total payé pour une commande
    @Query("""
        SELECT COALESCE(SUM(p.montant), 0)
        FROM PaiementCommande p
        WHERE p.commande.id = :commandeId
    """)
    Double sumMontantByCommandeId(Long commandeId);


    // 🔹 CA du jour (encaissements aujourd’hui)
    @Query("""
        SELECT COALESCE(SUM(p.montant), 0)
        FROM PaiementCommande p
        WHERE p.datePaiement = CURRENT_DATE
    """)
    Double caJour();



        @Query("""
        SELECT COALESCE(SUM(p.montant),0)
        FROM PaiementCommande p
        WHERE p.datePaiement BETWEEN :startDate AND :endDate
    """)
        Double caBetween(LocalDate startDate, LocalDate endDate);

}

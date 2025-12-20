package com.pro.Facture.repository;

import com.pro.Facture.Entity.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Long> {
    // Les 3 dernières commandes (par ID décroissant)
    List<Commande> findTop3ByOrderByIdDesc();

   // ----- TOTALS -----
   @Query("SELECT COALESCE(SUM(c.mtTtc),0) FROM Commande c")
   double totalFacture();

    @Query("SELECT COUNT(c) FROM Commande c")
    long nombreFactures();

    @Query("""
    SELECT COALESCE(SUM(c.mtTtc),0)
    FROM Commande c
    WHERE MONTH(c.dateFacture) = MONTH(CURRENT_DATE)
      AND YEAR(c.dateFacture) = YEAR(CURRENT_DATE)
""")
    double totalFactureMoisActuel();

    @Query("""
    SELECT COALESCE(SUM(c.mtTtc),0)
    FROM Commande c
    WHERE MONTH(c.dateFacture) = MONTH(CURRENT_DATE) - 1
      AND YEAR(c.dateFacture) = YEAR(CURRENT_DATE)
""")
    double totalFactureMoisPrecedent();

}

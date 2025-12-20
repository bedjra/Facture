package com.pro.Facture.repository;

import com.pro.Facture.Entity.Commande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Long> {
    // Les 5 dernières commandes (par ID décroissant)
    List<Commande> findTop5ByOrderByIdDesc();
}

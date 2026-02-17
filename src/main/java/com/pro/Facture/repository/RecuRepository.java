package com.pro.Facture.repository;

import com.pro.Facture.Entity.Recu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecuRepository extends JpaRepository<Recu, Long> {

    // Tous les reçus d'une Place, du plus récent au plus ancien
    List<Recu> findByPlace_IdOrderByDateDesc(Long placeId);

    // Compter les reçus d'une Place (pour générer la référence)
    long countByPlace_Id(Long placeId);
}
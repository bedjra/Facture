package com.pro.Facture.repository;

import com.pro.Facture.Entity.Charge;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface ChargeRepository extends JpaRepository<Charge, Long> {

    @Query("SELECT COALESCE(SUM(c.montant), 0) FROM Charge c")
    Double getTotalCharges();

}
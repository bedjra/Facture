package com.pro.Facture.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class PaiementCommande {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private Double montant;

    private LocalDate datePaiement;

    @ManyToOne
    private Commande commande;
}
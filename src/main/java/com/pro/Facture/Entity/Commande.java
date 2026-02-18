package com.pro.Facture.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;


    // 🔗 Place → pour récupérer TVA automatiquement
    @ManyToOne
    @JoinColumn(name = "place_id")
    private Place place;

    private String ref;  // ex : 0001, 0002...

    private String design;
    private Double ht;
//    private String code;

//    private Double base;   // = ht automatique
    private Double retenue;
    private Double mt;

    private Double tva;     // Prend valeur depuis Place
    private Double mtTtc;   // ht + (ht * tva/100)
    private Double avance;
    private Double net;     // mtTtc - avance

    private LocalDate dateFacture;

    @Column(length = 20)
    private String statut;

    public Commande() {}

    @Column(columnDefinition = "TEXT") // ou LONGTEXT selon MySQL
    private String lignesJson;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    // Getters & Setters
    // ...


}

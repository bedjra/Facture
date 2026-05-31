package com.pro.Facture.Entity;

import com.pro.Facture.enums.ModePaiement;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "recu")
public class Recu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_piece")
    private String numeroPiece;

    private LocalDate date;

    private String beneficiaire;
    @Column(name = "num_benef")
    private String numBenef;

    @Column(name = "montant_encaisse", precision = 15, scale = 2)
    private BigDecimal montantEncaisse;

    // Montant total
    @Column(name = "montant_total", precision = 15, scale = 2)
    private BigDecimal montantTotal;

    // Reste à payer
    @Column(name = "reste", precision = 15, scale = 2)
    private BigDecimal reste;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModePaiement mode;

    @Column(columnDefinition = "TEXT")
    private String motif;

    // Relation vers Place
    @ManyToOne
    @JoinColumn(name = "place_id")
    private Place place;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    // =========================
    // Calcul automatique du reste
    // =========================
    @PrePersist
    @PreUpdate
    public void calculerReste() {

        if (montantTotal != null && montantEncaisse != null) {
            this.reste = montantTotal.subtract(montantEncaisse);
        } else {
            this.reste = BigDecimal.ZERO;
        }
    }

    // =========================
    // Constructeur
    // =========================
    public Recu() {
    }
}
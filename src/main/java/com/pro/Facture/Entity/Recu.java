package com.pro.Facture.Entity;

import com.pro.Facture.enums.ModePaiement;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Table(name = "recu")
public class Recu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_piece", nullable = false, unique = true)
    private String numeroPiece;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String beneficiaire;

    @Column(name = "montant_encaisse", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantEncaisse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModePaiement mode;

    @Column(columnDefinition = "TEXT")
    private String motif;

    // Relation vers Place
    @ManyToOne
    @JoinColumn(name = "place_id") // colonne de jointure dans la table recu
    private Place place;

    // =========================
    // Constructeurs
    // =========================
    public Recu() { }

    // =========================
    // Getters & Setters
    // =========================
    public Long getId() { return id; }

    public String getNumeroPiece() { return numeroPiece; }
    public void setNumeroPiece(String numeroPiece) { this.numeroPiece = numeroPiece; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getBeneficiaire() { return beneficiaire; }
    public void setBeneficiaire(String beneficiaire) { this.beneficiaire = beneficiaire; }

    public BigDecimal getMontantEncaisse() { return montantEncaisse; }
    public void setMontantEncaisse(BigDecimal montantEncaisse) { this.montantEncaisse = montantEncaisse; }

    public ModePaiement getMode() { return mode; }
    public void setMode(ModePaiement mode) { this.mode = mode; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public Place getPlace() { return place; }
    public void setPlace(Place place) { this.place = place; }
}

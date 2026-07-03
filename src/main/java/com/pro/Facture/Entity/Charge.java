package com.pro.Facture.Entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "charge")
public class Charge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String beneficiaire;   // "Bénéficiaire" sur le reçu

    @Column
    private String motif;          // "Motif" sur le reçu (ex: "Salaire Mai")

    @Column(name = "montant", nullable = false)
    private BigDecimal montant;

    private LocalDate dateCharge; // <-- PAS "date" !

    @PrePersist
    public void prePersist() {
        if (dateCharge == null) {
            dateCharge = LocalDate.now();
        }
    }

    // Getters & Setters
    public LocalDate getDateCharge() {
        return dateCharge;
    }

    public void setDateCharge(LocalDate dateCharge) {
        this.dateCharge = dateCharge;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBeneficiaire() {
        return beneficiaire;
    }

    public void setBeneficiaire(String beneficiaire) {
        this.beneficiaire = beneficiaire;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }
}
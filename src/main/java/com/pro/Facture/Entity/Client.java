package com.pro.Facture.Entity;

import com.pro.Facture.enums.StatutClient;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String NIF;
    private String telephone;
    private String adresse;

    @Enumerated(EnumType.STRING)
    private StatutClient statutClient;

    private LocalDateTime date;




    @PrePersist
    protected void onCreate() {
        this.date = LocalDateTime.now();
        this.statutClient = StatutClient.Actif;
    }

    // Constructeurs
    public Client() {}

    public Client(String nom, String telephone, String adresse) {
        this.nom = nom;
        this.telephone = telephone;
        this.adresse = adresse;
        this.statutClient = StatutClient.Actif;
        this.date = LocalDateTime.now();
    }

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNIF() {
        return NIF;
    }

    public void setNIF(String NIF) {
        this.NIF = NIF;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }


    public StatutClient getStatutClient() { return statutClient; }
    public void setStatutClient(StatutClient statutClient) { this.statutClient = statutClient; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }


}

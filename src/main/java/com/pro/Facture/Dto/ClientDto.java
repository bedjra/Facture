package com.pro.Facture.Dto;

import com.pro.Facture.enums.StatutClient;

import java.time.LocalDateTime;

public class ClientDto {

    private Long id;
    private String nom;
    private String NIF;
    private String telephone;
    private String adresse;
    private StatutClient statutClient;
    private LocalDateTime date;

    public ClientDto() {}

    public ClientDto(Long id, String nom, String NIF, String telephone, String adresse,
                     StatutClient statutClient, LocalDateTime date) {
        this.id = id;
        this.nom = nom;
        this.NIF = NIF;
        this.telephone = telephone;
        this.adresse = adresse;
        this.statutClient = statutClient;
        this.date = date;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getNIF() { return NIF; }
    public void setNif(String NIF) { this.NIF = NIF; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public StatutClient getStatutClient() { return statutClient; }
    public void setStatutClient(StatutClient statutClient) { this.statutClient = statutClient; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

}


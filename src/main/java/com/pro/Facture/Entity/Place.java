package com.pro.Facture.Entity;

import jakarta.persistence.*;


@Entity
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Lob
    private byte[] logo;
    private String nom;
    private String email;
    private String telephone;
    private String cel;
    private String adresse;
    private String desc;


    public Place() {}

    public Place(String nom, byte[]  logo, String email, String telephone, String cel, String adresse, String desc) {
        this.nom = nom;
        this.logo = logo;
        this.email = email;
        this.telephone = telephone;
        this.cel =cel;
        this.adresse = adresse;
        this.desc = desc;
    }

    // Getters / Setters


    public String getCel() {
        return cel;
    }

    public void setCel(String cel) {
        this.cel = cel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getLogo() {
        return logo;
    }

    public void setLogo( byte[]   logo) {
        this.logo = logo;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }


    public String getEmail() {
        return email;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }


}

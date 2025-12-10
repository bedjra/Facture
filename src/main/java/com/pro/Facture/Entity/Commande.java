package com.pro.Facture.Entity;

import jakarta.persistence.*;

import java.time.LocalDate;

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
    private String code;

    private Double base;   // = ht automatique
    private Double retenue;
    private Double mt;

    private Double tva;     // Prend valeur depuis Place
    private Double mtTtc;   // ht + (ht * tva/100)
    private Double avance;
    private Double net;     // mtTtc - avance

    private LocalDate dateFacture;


    public Commande() {}

    // Getters & Setters
    // ...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getDesign() {
        return design;
    }

    public void setDesign(String design) {
        this.design = design;
    }

    public Double getHt() {
        return ht;
    }

    public void setHt(Double ht) {
        this.ht = ht;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getBase() {
        return base;
    }

    public void setBase(Double base) {
        this.base = base;
    }

    public Double getRetenue() {
        return retenue;
    }

    public void setRetenue(Double retenue) {
        this.retenue = retenue;
    }

    public Double getMt() {
        return mt;
    }

    public void setMt(Double mt) {
        this.mt = mt;
    }

    public Double getTva() {
        return tva;
    }

    public void setTva(Double tva) {
        this.tva = tva;
    }

    public Double getMtTtc() {
        return mtTtc;
    }

    public void setMtTtc(Double mtTtc) {
        this.mtTtc = mtTtc;
    }

    public Double getAvance() {
        return avance;
    }

    public void setAvance(Double avance) {
        this.avance = avance;
    }

    public Double getNet() {
        return net;
    }

    public void setNet(Double net) {
        this.net = net;
    }


    public LocalDate getDateFacture() {
        return dateFacture;
    }

    public void setDateFacture(LocalDate dateFacture) {
        this.dateFacture = dateFacture;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}

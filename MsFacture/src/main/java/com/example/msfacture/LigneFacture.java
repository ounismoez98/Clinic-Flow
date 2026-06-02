package com.example.msfacture;

import jakarta.persistence.*;

@Entity

public class LigneFacture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Long id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    public LigneFacture(String description, double prix, int quantite, Facture facture) {
        this.description = description;
        this.prix = prix;
        this.quantite = quantite;
        this.facture = facture;
    }

    private String description;
    private double prix;
    private int quantite;

    @ManyToOne
    @JoinColumn(name = "facture_id")
    @com.fasterxml.jackson.annotation.JsonBackReference
    private Facture facture;

    @com.fasterxml.jackson.annotation.JsonCreator
    public LigneFacture() {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }



    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public Facture getFacture() {
        return facture;
    }

    public void setFacture(Facture facture) {
        this.facture = facture;
    }
}



package com.example.msfacture;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Facture {
    @Id
    @GeneratedValue

    private Long id;

    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateFacture;

    private double montantHT;
    private double montantTTC;

    @Enumerated(EnumType.STRING)
    private StatutFacture statut;

    @Enumerated(EnumType.STRING)
    private TypeTVA tva;

    private int patientId;

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private List<LigneFacture> lignes = new ArrayList<>();

    @com.fasterxml.jackson.annotation.JsonCreator
    public Facture() {

    }

    public LocalDate getDateFacture() {
        return dateFacture;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public Facture(Long id, LocalDate dateFacture, double montantHT, double montantTTC, TypeTVA tva,
            StatutFacture statut, int patientId, List<LigneFacture> lignes) {

        this.dateFacture = dateFacture;
        this.montantHT = montantHT;
        this.montantTTC = montantTTC;
        this.tva = tva;
        this.statut = statut;
        this.patientId = patientId;
        this.lignes = lignes;
    }

    public void setDateFacture(LocalDate dateFacture) {
        this.dateFacture = dateFacture;
    }

    public double getMontantHT() {
        return montantHT;
    }

    public void setMontantHT(double montantHT) {
        this.montantHT = montantHT;
    }

    public double getMontantTTC() {
        return montantTTC;
    }

    public void setMontantTTC(double montantTTC) {
        this.montantTTC = montantTTC;
    }

    public StatutFacture getStatut() {
        return statut;
    }

    public void setStatut(StatutFacture statut) {
        this.statut = statut;
    }

    public TypeTVA getTva() {
        return tva;
    }

    public void setTva(TypeTVA tva) {
        this.tva = tva;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public List<LigneFacture> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneFacture> lignes) {
        this.lignes = lignes;
    }

    public Long getId() {
        return id;
    }
// pour evite les problemes 
    @PrePersist
    protected void onCreate() {
        if (this.dateFacture == null) {
            this.dateFacture = LocalDate.now();
        }
        if (this.statut == null) {
            this.statut = StatutFacture.NON_PAYEE;
        }
    }

}

package com.example.msordonnance;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Ordonnance {
    @Id
    @GeneratedValue
    private int id;
    private String nom, prenom, email;
    @ElementCollection
    private Set<Integer> medicamentsIds = new HashSet<>();

    public Ordonnance() {
    }

    public Ordonnance(String nom, String prenom, String email) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Integer> getMedicamentsIds() {
        return medicamentsIds;
    }

    public void setMedicamentsIds(Set<Integer> medicamentsIds) {
        this.medicamentsIds = medicamentsIds;
    }
}

package tn.esprit.spring.patientmedcin;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nom;
    private String prenom;
    private String email;

    private Integer userId;

    @ElementCollection
    private Set<Integer> favoriteMedecinIds = new HashSet<>();

    public Patient() {
    }

    public Patient(String nom, String prenom, String email) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
    }

    public Patient(String nom, String prenom, String email, Integer userId) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Set<Integer> getFavoriteMedecinIds() {
        return favoriteMedecinIds;
    }

    public void setFavoriteMedecinIds(Set<Integer> favoriteMedecinIds) {
        this.favoriteMedecinIds = favoriteMedecinIds;
    }
}

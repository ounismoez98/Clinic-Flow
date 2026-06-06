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

    private String telephone;
    private String dateNaissance;   // ISO yyyy-MM-dd; age is derived on the frontend
    private String genre;           // Male / Female
    private String groupeSanguin;   // A+, O-, ...
    private Integer medecinId;      // assigned doctor (links to Medecin.id)
    private String statut;          // Active / Admitted / Discharged

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

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(String dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getGroupeSanguin() {
        return groupeSanguin;
    }

    public void setGroupeSanguin(String groupeSanguin) {
        this.groupeSanguin = groupeSanguin;
    }

    public Integer getMedecinId() {
        return medecinId;
    }

    public void setMedecinId(Integer medecinId) {
        this.medecinId = medecinId;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
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

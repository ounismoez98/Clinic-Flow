package com.example.msconsultation.dto;

public class MedecinDto {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String specialite;
    private Integer userId;

    public MedecinDto() {}

    public MedecinDto(int id, String nom, String prenom, String email, String specialite, Integer userId) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.specialite = specialite;
        this.userId = userId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
}

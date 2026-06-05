package com.example.mscandidat;

import java.util.Set;

public class OrdonnanceDTO {
    private int id;
    private String nom, prenom, email;
    private int patientId, medcinId, laboratoireId;
    private String type, status;
    private Set<Integer> medicamentsIds;

    public OrdonnanceDTO() {
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

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getMedcinId() {
        return medcinId;
    }

    public void setMedcinId(int medcinId) {
        this.medcinId = medcinId;
    }

    public int getLaboratoireId() {
        return laboratoireId;
    }

    public void setLaboratoireId(int laboratoireId) {
        this.laboratoireId = laboratoireId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<Integer> getMedicamentsIds() {
        return medicamentsIds;
    }

    public void setMedicamentsIds(Set<Integer> medicamentsIds) {
        this.medicamentsIds = medicamentsIds;
    }
}

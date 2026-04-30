package com.example.mscandidat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class RendezVous {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String date;
    private String cause;
    private String patient;
    private String medcin;


    public RendezVous() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getPatient() {
        return patient;
    }

    public void setPatient(String patient) {
        this.patient = patient;
    }

    public String getMedcin() {
        return medcin;
    }

    public void setMedcin(String medcin) {
        this.medcin = medcin;
    }

    public RendezVous(String date, String cause, String patient, String medcin) {

        this.date = date;
        this.cause = cause;
        this.patient = patient;
        this.medcin = medcin;
    }

    @Override
    public String toString() {
        return "RendezVous{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", cause='" + cause + '\'' +
                ", patient='" + patient + '\'' +
                ", medcin='" + medcin + '\'' +
                '}';
    }
}

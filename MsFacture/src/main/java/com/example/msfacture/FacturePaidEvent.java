package com.example.msfacture;

import java.io.Serializable;

public class FacturePaidEvent implements Serializable {
    private Long id;
    private int patientId;
    private double montant;

    public FacturePaidEvent() {}

    public FacturePaidEvent(Long id, int patientId, double montant) {
        this.id = id;
        this.patientId = patientId;
        this.montant = montant;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }
}

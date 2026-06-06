package com.example.msconsultation.dto;

import java.time.LocalDateTime;

public class ConsultationFinalizedEvent {
    private Long consultationId;
    private int patientId;
    private int medecinId;
    private double montant;
    private LocalDateTime date;

    public ConsultationFinalizedEvent() {}

    public ConsultationFinalizedEvent(Long consultationId, int patientId, int medecinId, double montant, LocalDateTime date) {
        this.consultationId = consultationId;
        this.patientId = patientId;
        this.medecinId = medecinId;
        this.montant = montant;
        this.date = date;
    }

    public Long getConsultationId() { return consultationId; }
    public void setConsultationId(Long consultationId) { this.consultationId = consultationId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getMedecinId() { return medecinId; }
    public void setMedecinId(int medecinId) { this.medecinId = medecinId; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
}

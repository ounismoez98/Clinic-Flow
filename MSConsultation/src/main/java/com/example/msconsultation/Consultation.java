package com.example.msconsultation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;

@Entity
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateConsultation;
    private int patientId;
    private int medecinId;
    private Long rendezVousId;
    private Long ordonnanceId;
    private String diagnostic;
    private String notes;
    private double prixConsultation;

    public Consultation() {}

    public Consultation(LocalDateTime dateConsultation, int patientId, int medecinId, Long rendezVousId, Long ordonnanceId, String diagnostic, String notes, double prixConsultation) {
        this.dateConsultation = dateConsultation;
        this.patientId = patientId;
        this.medecinId = medecinId;
        this.rendezVousId = rendezVousId;
        this.ordonnanceId = ordonnanceId;
        this.diagnostic = diagnostic;
        this.notes = notes;
        this.prixConsultation = prixConsultation;
    }

    @PrePersist
    protected void onCreate() {
        if (this.dateConsultation == null) {
            this.dateConsultation = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDateConsultation() { return dateConsultation; }
    public void setDateConsultation(LocalDateTime dateConsultation) { this.dateConsultation = dateConsultation; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getMedecinId() { return medecinId; }
    public void setMedecinId(int medecinId) { this.medecinId = medecinId; }

    public Long getRendezVousId() { return rendezVousId; }
    public void setRendezVousId(Long rendezVousId) { this.rendezVousId = rendezVousId; }

    public Long getOrdonnanceId() { return ordonnanceId; }
    public void setOrdonnanceId(Long ordonnanceId) { this.ordonnanceId = ordonnanceId; }

    public String getDiagnostic() { return diagnostic; }
    public void setDiagnostic(String diagnostic) { this.diagnostic = diagnostic; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public double getPrixConsultation() { return prixConsultation; }
    public void setPrixConsultation(double prixConsultation) { this.prixConsultation = prixConsultation; }
}

package tn.esprit.spring.notification;

import java.io.Serializable;

public class FacturePaidEvent implements Serializable {
    private Long factureId;
    private int patientId;
    private double montantTTC;
    private String datePaiement;

    public FacturePaidEvent() {}

    public FacturePaidEvent(Long factureId, int patientId, double montantTTC, String datePaiement) {
        this.factureId = factureId;
        this.patientId = patientId;
        this.montantTTC = montantTTC;
        this.datePaiement = datePaiement;
    }

    public Long getFactureId() { return factureId; }
    public void setFactureId(Long factureId) { this.factureId = factureId; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public double getMontantTTC() { return montantTTC; }
    public void setMontantTTC(double montantTTC) { this.montantTTC = montantTTC; }
    public String getDatePaiement() { return datePaiement; }
    public void setDatePaiement(String datePaiement) { this.datePaiement = datePaiement; }
}

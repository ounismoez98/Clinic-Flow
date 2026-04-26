package com.example.mscandidat;

public class AnalysisRequestMessage {
    private int patientId;
    private int medcinId;
    private int laboratoireId;
    private String type;
    private String status;

    public AnalysisRequestMessage() {
    }

    public AnalysisRequestMessage(int patientId, int medcinId, int laboratoireId, String type, String status) {
        this.patientId = patientId;
        this.medcinId = medcinId;
        this.laboratoireId = laboratoireId;
        this.type = type;
        this.status = status;
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
}

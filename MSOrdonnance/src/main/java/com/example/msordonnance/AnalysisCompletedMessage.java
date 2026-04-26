package com.example.msordonnance;

public class AnalysisCompletedMessage {
    private int analysisId;
    private int ordonnanceId;
    private int patientId;
    private int medcinId;
    private String result;

    public AnalysisCompletedMessage() {}

    public AnalysisCompletedMessage(int analysisId, int ordonnanceId, int patientId, int medcinId, String result) {
        this.analysisId = analysisId;
        this.ordonnanceId = ordonnanceId;
        this.patientId = patientId;
        this.medcinId = medcinId;
        this.result = result;
    }

    public int getAnalysisId() { return analysisId; }
    public void setAnalysisId(int analysisId) { this.analysisId = analysisId; }
    public int getOrdonnanceId() { return ordonnanceId; }
    public void setOrdonnanceId(int ordonnanceId) { this.ordonnanceId = ordonnanceId; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public int getMedcinId() { return medcinId; }
    public void setMedcinId(int medcinId) { this.medcinId = medcinId; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}

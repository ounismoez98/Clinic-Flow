package tn.esprit.spring.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Mirror of the message MSPatientMedcin publishes on patient.event. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientEventMessage {

    private String eventType;     // CREATED / ADMITTED
    private int patientId;
    private String patientName;
    private String patientEmail;

    public PatientEventMessage() {
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }
}

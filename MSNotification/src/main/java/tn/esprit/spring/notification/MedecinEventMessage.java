package tn.esprit.spring.notification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Mirror of the message MSPatientMedcin publishes on medecin.event. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedecinEventMessage {

    private String eventType;     // CREATED
    private int medecinId;
    private String medecinName;
    private String specialite;
    private String email;

    public MedecinEventMessage() {
    }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public int getMedecinId() { return medecinId; }
    public void setMedecinId(int medecinId) { this.medecinId = medecinId; }

    public String getMedecinName() { return medecinName; }
    public void setMedecinName(String medecinName) { this.medecinName = medecinName; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

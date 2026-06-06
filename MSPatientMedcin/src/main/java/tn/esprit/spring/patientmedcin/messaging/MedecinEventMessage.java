package tn.esprit.spring.patientmedcin.messaging;

/**
 * Async event published when something notable happens to a medecin
 * (e.g. CREATED). MSNotification consumes it and reacts.
 */
public class MedecinEventMessage {

	private String eventType;     // CREATED
	private int medecinId;
	private String medecinName;
	private String specialite;
	private String email;

	public MedecinEventMessage() {
	}

	public MedecinEventMessage(String eventType, int medecinId, String medecinName,
	                           String specialite, String email) {
		this.eventType = eventType;
		this.medecinId = medecinId;
		this.medecinName = medecinName;
		this.specialite = specialite;
		this.email = email;
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

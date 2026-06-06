package tn.esprit.spring.patientmedcin.messaging;

/**
 * Async event published when something notable happens to a patient
 * (e.g. CREATED, ADMITTED). MSNotification consumes it and reacts.
 */
public class PatientEventMessage {

	private String eventType;     // CREATED / ADMITTED
	private int patientId;
	private String patientName;
	private String patientEmail;

	public PatientEventMessage() {
	}

	public PatientEventMessage(String eventType, int patientId, String patientName, String patientEmail) {
		this.eventType = eventType;
		this.patientId = patientId;
		this.patientName = patientName;
		this.patientEmail = patientEmail;
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

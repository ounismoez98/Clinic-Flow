package tn.esprit.spring.patientmedcin.messaging;

public class PatientUserLinkedMessage {

	private int patientId;
	private int userId;
	private String patientEmail;

	public PatientUserLinkedMessage() {
	}

	public PatientUserLinkedMessage(int patientId, int userId, String patientEmail) {
		this.patientId = patientId;
		this.userId = userId;
		this.patientEmail = patientEmail;
	}

	public int getPatientId() {
		return patientId;
	}

	public void setPatientId(int patientId) {
		this.patientId = patientId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getPatientEmail() {
		return patientEmail;
	}

	public void setPatientEmail(String patientEmail) {
		this.patientEmail = patientEmail;
	}
}

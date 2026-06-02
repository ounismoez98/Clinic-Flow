package com.example.mspharmacie.web;

public class PatientNotFoundException extends RuntimeException {

	private final int patientId;

	public PatientNotFoundException(int patientId) {
		super("Patient not found: " + patientId);
		this.patientId = patientId;
	}

	public int getPatientId() {
		return patientId;
	}
}

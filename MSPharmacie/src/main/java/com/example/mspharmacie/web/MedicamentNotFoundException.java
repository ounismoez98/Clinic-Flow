package com.example.mspharmacie.web;

public class MedicamentNotFoundException extends RuntimeException {

	private final int medicamentId;

	public MedicamentNotFoundException(int medicamentId) {
		super("Medicament not found: " + medicamentId);
		this.medicamentId = medicamentId;
	}

	public int getMedicamentId() {
		return medicamentId;
	}
}

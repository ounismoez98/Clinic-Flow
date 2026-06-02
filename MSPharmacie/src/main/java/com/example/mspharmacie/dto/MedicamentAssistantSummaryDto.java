package com.example.mspharmacie.dto;

public class MedicamentAssistantSummaryDto {

	private int medicamentId;
	private String nomMedicament;
	private String summary;
	private String disclaimer;

	public MedicamentAssistantSummaryDto() {
	}

	public MedicamentAssistantSummaryDto(int medicamentId, String nomMedicament, String summary, String disclaimer) {
		this.medicamentId = medicamentId;
		this.nomMedicament = nomMedicament;
		this.summary = summary;
		this.disclaimer = disclaimer;
	}

	public int getMedicamentId() {
		return medicamentId;
	}

	public void setMedicamentId(int medicamentId) {
		this.medicamentId = medicamentId;
	}

	public String getNomMedicament() {
		return nomMedicament;
	}

	public void setNomMedicament(String nomMedicament) {
		this.nomMedicament = nomMedicament;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDisclaimer() {
		return disclaimer;
	}

	public void setDisclaimer(String disclaimer) {
		this.disclaimer = disclaimer;
	}
}

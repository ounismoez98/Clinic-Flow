package com.example.mspharmacie.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class UpdateMedicamentRequest {

	@NotNull
	private String nomMedicament;

	@NotNull
	private Boolean etat;

	private BigDecimal prixUnitaire;

	public String getNomMedicament() {
		return nomMedicament;
	}

	public void setNomMedicament(String nomMedicament) {
		this.nomMedicament = nomMedicament;
	}

	public Boolean getEtat() {
		return etat;
	}

	public void setEtat(Boolean etat) {
		this.etat = etat;
	}

	public BigDecimal getPrixUnitaire() {
		return prixUnitaire;
	}

	public void setPrixUnitaire(BigDecimal prixUnitaire) {
		this.prixUnitaire = prixUnitaire;
	}
}

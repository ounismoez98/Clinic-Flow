package com.example.mspharmacie.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class DispenseRequestDto {

	@NotNull
	@Min(1)
	private Integer patientId;

	@NotNull
	@Min(1)
	private Integer quantity;

	public Integer getPatientId() {
		return patientId;
	}

	public void setPatientId(Integer patientId) {
		this.patientId = patientId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
}

package com.example.mspharmacie.dto;

import java.math.BigDecimal;

public class StockInfoDto {

	private int medicamentId;
	private int stockQuantity;
	private BigDecimal prixUnitaire;

	public StockInfoDto() {
	}

	public StockInfoDto(int medicamentId, int stockQuantity, BigDecimal prixUnitaire) {
		this.medicamentId = medicamentId;
		this.stockQuantity = stockQuantity;
		this.prixUnitaire = prixUnitaire;
	}

	public int getMedicamentId() {
		return medicamentId;
	}

	public void setMedicamentId(int medicamentId) {
		this.medicamentId = medicamentId;
	}

	public int getStockQuantity() {
		return stockQuantity;
	}

	public void setStockQuantity(int stockQuantity) {
		this.stockQuantity = stockQuantity;
	}

	public BigDecimal getPrixUnitaire() {
		return prixUnitaire;
	}

	public void setPrixUnitaire(BigDecimal prixUnitaire) {
		this.prixUnitaire = prixUnitaire;
	}
}

package com.example.mspharmacie.messaging;

public class StockLowAlertMessage {

	private int medicamentId;
	private String nomMedicament;
	private int stockQuantity;

	public StockLowAlertMessage() {
	}

	public StockLowAlertMessage(int medicamentId, String nomMedicament, int stockQuantity) {
		this.medicamentId = medicamentId;
		this.nomMedicament = nomMedicament;
		this.stockQuantity = stockQuantity;
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

	public int getStockQuantity() {
		return stockQuantity;
	}

	public void setStockQuantity(int stockQuantity) {
		this.stockQuantity = stockQuantity;
	}
}

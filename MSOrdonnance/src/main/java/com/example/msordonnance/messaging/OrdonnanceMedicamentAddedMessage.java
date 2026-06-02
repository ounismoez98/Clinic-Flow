package com.example.msordonnance.messaging;

public class OrdonnanceMedicamentAddedMessage {

	private int ordonnanceId;
	private int medicamentId;
	private int quantity = 1;

	public OrdonnanceMedicamentAddedMessage() {
	}

	public OrdonnanceMedicamentAddedMessage(int ordonnanceId, int medicamentId, int quantity) {
		this.ordonnanceId = ordonnanceId;
		this.medicamentId = medicamentId;
		this.quantity = quantity;
	}

	public int getOrdonnanceId() {
		return ordonnanceId;
	}

	public void setOrdonnanceId(int ordonnanceId) {
		this.ordonnanceId = ordonnanceId;
	}

	public int getMedicamentId() {
		return medicamentId;
	}

	public void setMedicamentId(int medicamentId) {
		this.medicamentId = medicamentId;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
}

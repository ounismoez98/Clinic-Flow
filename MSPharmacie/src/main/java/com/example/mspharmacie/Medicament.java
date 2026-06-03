package com.example.mspharmacie;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Medicament {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private String nomMedicament;

	@Column(nullable = false)
	private boolean etat;

	@Column(nullable = false)
	private int stockQuantity;

	@Column(precision = 12, scale = 2)
	private BigDecimal prixUnitaire;

	public Medicament() {
	}

	public Medicament(String nomMedicament, boolean etat, int stockQuantity, BigDecimal prixUnitaire) {
		this.nomMedicament = nomMedicament;
		this.etat = etat;
		this.stockQuantity = stockQuantity;
		this.prixUnitaire = prixUnitaire;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNomMedicament() {
		return nomMedicament;
	}

	public void setNomMedicament(String nomMedicament) {
		this.nomMedicament = nomMedicament;
	}

	public boolean isEtat() {
		return etat;
	}

	public void setEtat(boolean etat) {
		this.etat = etat;
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

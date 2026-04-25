package com.example.msordonnance;

public class MedicamentDTO {
    private int id;
    private String nomMedicament;
    private boolean etat;

    public MedicamentDTO() {
    }

    public MedicamentDTO(int id, String nomMedicament, boolean etat) {
        this.id = id;
        this.nomMedicament = nomMedicament;
        this.etat = etat;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
}

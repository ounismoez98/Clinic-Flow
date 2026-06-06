package tn.esprit.spring.patientmedcin;

import java.util.List;

public interface IMedecinService {
    List<Medecin> getAll();

    Medecin create(Medecin medecin);

    Medecin update(int id, Medecin medecin);

    boolean delete(int id);

    Medecin getById(int id);

    MedecinStats getStats();

    List<Medecin> filter(String statut, String specialite);
}

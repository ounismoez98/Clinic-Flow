package tn.esprit.spring.patientmedcin;

import java.util.List;

public interface IPatientService {
    List<Patient> getAll();

    Patient create(Patient patient);

    Patient update(int id, Patient patient);

    boolean delete(int id);

    List<Medecin> getFavoriteMedecins(int patientId);

    void saveFavoriteMedecin(int patientId, int medecinId);
}

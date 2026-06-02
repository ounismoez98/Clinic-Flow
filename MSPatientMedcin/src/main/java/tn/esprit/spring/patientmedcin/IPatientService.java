package tn.esprit.spring.patientmedcin;

import java.util.List;
import java.util.Optional;

public interface IPatientService {
    List<Patient> getAll();

    Optional<Patient> getPatientById(int id);

    PatientLinkedAccountResponse getPatientWithLinkedAccountFeign(int patientId);

    Patient create(Patient patient);

    Patient update(int id, Patient patient);

    boolean delete(int id);

    List<Medecin> getFavoriteMedecins(int patientId);

    void saveFavoriteMedecin(int patientId, int medecinId);
}

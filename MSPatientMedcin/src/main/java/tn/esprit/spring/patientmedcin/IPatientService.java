package tn.esprit.spring.patientmedcin;

import java.util.List;
import java.util.Optional;

public interface IPatientService {
    List<Patient> getAll();

    Optional<Patient> getPatientById(int id);

    PatientLinkedAccountResponse getPatientWithLinkedAccountFeign(int patientId);

    Optional<Patient> getById(int id);

    List<Patient> searchByNomAndPrenom(String nom, String prenom);

    Patient create(Patient patient);

    Patient update(int id, Patient patient);

    boolean delete(int id);

    List<Medecin> getFavoriteMedecins(int patientId);

    void saveFavoriteMedecin(int patientId, int medecinId);

    PatientStats getStats();

    List<Patient> filter(String statut, String genre, String groupeSanguin, Integer medecinId);

    PatientDetailsResponse getPatientDetails(int id);
}

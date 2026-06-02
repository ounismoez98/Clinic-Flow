package tn.esprit.spring.patientmedcin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientService implements IPatientService {

    private final PatientRepository patientRepository;
    private final MedecinRepository medecinRepository;

    public PatientService(PatientRepository patientRepository, MedecinRepository medecinRepository) {
        this.patientRepository = patientRepository;
        this.medecinRepository = medecinRepository;
    }

    @Override
    public List<Patient> getAll() {
        return patientRepository.findAll();
    }

    @Override
    public Optional<Patient> getById(int id) {
        return patientRepository.findById(id);
    }

    @Override
    public List<Patient> searchByNomAndPrenom(String nom, String prenom) {
        return patientRepository.findByNomIgnoreCaseAndPrenomIgnoreCase(
                nom != null ? nom.trim() : "",
                prenom != null ? prenom.trim() : "");
    }

    @Override
    @Transactional
    public Patient create(Patient patient) {
        return patientRepository.save(patient);
    }

    @Override
    @Transactional
    public Patient update(int id, Patient patient) {
        return patientRepository.findById(id)
                .map(existing -> {
                    existing.setNom(patient.getNom());
                    existing.setPrenom(patient.getPrenom());
                    existing.setEmail(patient.getEmail());
                    if (patient.getFavoriteMedecinIds() != null) {
                        existing.setFavoriteMedecinIds(patient.getFavoriteMedecinIds());
                    }
                    return patientRepository.save(existing);
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean delete(int id) {
        if (!patientRepository.existsById(id)) {
            return false;
        }
        patientRepository.deleteById(id);
        return true;
    }

    @Override
    public List<Medecin> getFavoriteMedecins(int patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
        return patient.getFavoriteMedecinIds().stream()
                .map(medecinId -> medecinRepository.findById(medecinId).orElse(null))
                .filter(m -> m != null)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveFavoriteMedecin(int patientId, int medecinId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));
        patient.getFavoriteMedecinIds().add(medecinId);
        patientRepository.save(patient);
    }
}
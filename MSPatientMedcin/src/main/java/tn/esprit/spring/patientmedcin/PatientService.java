package tn.esprit.spring.patientmedcin;

import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.spring.patientmedcin.client.UserClient;
import tn.esprit.spring.patientmedcin.client.UserDto;
import tn.esprit.spring.patientmedcin.messaging.PatientUserLinkedPublisher;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientService implements IPatientService {

	private final PatientRepository patientRepository;
	private final MedecinRepository medecinRepository;
	private final UserClient userClient;
	private final PatientUserLinkedPublisher patientUserLinkedPublisher;

	public PatientService(
			PatientRepository patientRepository,
			MedecinRepository medecinRepository,
			UserClient userClient,
			PatientUserLinkedPublisher patientUserLinkedPublisher) {
		this.patientRepository = patientRepository;
		this.medecinRepository = medecinRepository;
		this.userClient = userClient;
		this.patientUserLinkedPublisher = patientUserLinkedPublisher;
	}

    @Override
    public List<Patient> getAll() {
        return patientRepository.findAll();
    }

	@Override
	public Optional<Patient> getPatientById(int id) {
		return patientRepository.findById(id);
	}

	@Override
	@Transactional
	public Patient create(Patient patient) {
		ensureLinkedUserExistsAndIsPatient(patient.getUserId());
		Patient saved = patientRepository.save(patient);
		if (saved.getUserId() != null) {
			patientUserLinkedPublisher.publish(saved.getId(), saved.getUserId(), saved.getEmail());
		}
		return saved;
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
					ensureLinkedUserExistsAndIsPatient(patient.getUserId());
					existing.setNom(patient.getNom());
					existing.setPrenom(patient.getPrenom());
					existing.setEmail(patient.getEmail());
					existing.setUserId(patient.getUserId());
					if (patient.getFavoriteMedecinIds() != null) {
						existing.setFavoriteMedecinIds(patient.getFavoriteMedecinIds());
					}
					Patient saved = patientRepository.save(existing);
					if (saved.getUserId() != null) {
						patientUserLinkedPublisher.publish(saved.getId(), saved.getUserId(), saved.getEmail());
					}
					return saved;
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
	public PatientLinkedAccountResponse getPatientWithLinkedAccountFeign(int patientId) {
		Patient p = patientRepository.findById(patientId).orElse(null);
		if (p == null) {
			return null;
		}
		UserDto linked = null;
		if (p.getUserId() != null) {
			try {
				linked = userClient.getUserById(p.getUserId());
			} catch (FeignException.NotFound e) {
				linked = null;
			} catch (FeignException e) {
				throw new IllegalStateException("MSUser unreachable: " + e.getMessage());
			}
		}
		return new PatientLinkedAccountResponse(p, linked);
	}

	private void ensureLinkedUserExistsAndIsPatient(Integer userId) {
		if (userId == null) {
			return;
		}
		try {
			UserDto u = userClient.getUserById(userId);
			if (!u.isPatientRole()) {
				throw new IllegalArgumentException("User id=" + userId + " must have role PATIENT to be linked.");
			}
		} catch (FeignException.NotFound e) {
			throw new UnknownUserAccountException(userId);
		} catch (FeignException e) {
			throw new IllegalStateException("MSUser unreachable: " + e.getMessage());
		}
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

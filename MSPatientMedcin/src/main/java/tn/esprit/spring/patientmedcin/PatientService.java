package tn.esprit.spring.patientmedcin;

import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.spring.patientmedcin.client.UserClient;
import tn.esprit.spring.patientmedcin.client.UserDto;
import tn.esprit.spring.patientmedcin.client.IdentityProvisioningService;
import tn.esprit.spring.patientmedcin.messaging.PatientEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PatientService implements IPatientService {

	private final PatientRepository patientRepository;
	private final MedecinRepository medecinRepository;
	private final UserClient userClient;
	private final PatientEventPublisher patientEventPublisher;
	private final IdentityProvisioningService identityProvisioningService;

	private static final String DEFAULT_PASSWORD = "changeme123";

	public PatientService(
			PatientRepository patientRepository,
			MedecinRepository medecinRepository,
			UserClient userClient,
			PatientEventPublisher patientEventPublisher,
			IdentityProvisioningService identityProvisioningService) {
		this.patientRepository = patientRepository;
		this.medecinRepository = medecinRepository;
		this.userClient = userClient;
		this.patientEventPublisher = patientEventPublisher;
		this.identityProvisioningService = identityProvisioningService;
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
		// If no account was provided, auto-create one (MSUser + Keycloak) so the
		// patient has a real login. If a userId was provided, just validate it.
		if (patient.getUserId() == null && patient.getEmail() != null) {
			Integer newUserId = provisionPatientAccount(patient);
			patient.setUserId(newUserId);
		} else {
			ensureLinkedUserExistsAndIsPatient(patient.getUserId());
		}

		Patient saved = patientRepository.save(patient);

		// SYNCHRONOUS (Feign): link the user to this patient now that we have both
		// ids. This replaces the old async patient.user.linked event — we're already
		// talking to MSUser synchronously, so an extra message was redundant.
		if (saved.getUserId() != null) {
			try {
				userClient.linkPatient(saved.getUserId(), saved.getId());
			} catch (FeignException e) {
				// non-fatal: the patient is saved; the back-link just isn't set
			}
		}

		// ASYNCHRONOUS (RabbitMQ): tell MSNotification a patient was created.
		patientEventPublisher.publish("CREATED", saved.getId(),
				fullName(saved), saved.getEmail());
		return saved;
	}
    @Override
    public Optional<Patient> getById(int id) {
        return patientRepository.findById(id);
    }

	/**
	 * Creates the app account (MSUser, role PATIENT) via Feign and provisions a
	 * matching Keycloak login. Returns the MSUser id, or null if MSUser failed.
	 */
	private Integer provisionPatientAccount(Patient patient) {
		String username = patient.getEmail();
		Integer userId = null;
		try {
			UserDto created = userClient.createUser(
					new UserDto(username, patient.getEmail(), DEFAULT_PASSWORD, "PATIENT"));
			userId = created.getId();
		} catch (FeignException e) {
			// MSUser unreachable -> patient still created, just unlinked.
			return null;
		}
		// Provision the Keycloak login through the Node identity service
		// (the single owner of Keycloak admin ops). Best-effort.
		identityProvisioningService.createLogin(username, patient.getEmail(), DEFAULT_PASSWORD,
				"PATIENT", patient.getPrenom(), patient.getNom());
		return userId;
	}

	private String fullName(Patient p) {
		return ((p.getNom() != null ? p.getNom() : "") + " "
				+ (p.getPrenom() != null ? p.getPrenom() : "")).trim();
	}

    @Override
    public List<Patient> searchByNomAndPrenom(String nom, String prenom) {
        return patientRepository.findByNomIgnoreCaseAndPrenomIgnoreCase(
                nom != null ? nom.trim() : "",
                prenom != null ? prenom.trim() : "");
    }


	@Override
	@Transactional
	public Patient update(int id, Patient patient) {
		return patientRepository.findById(id)
				.map(existing -> {
					ensureLinkedUserExistsAndIsPatient(patient.getUserId());
					String previousStatut = existing.getStatut();
					existing.setNom(patient.getNom());
					existing.setPrenom(patient.getPrenom());
					existing.setEmail(patient.getEmail());
					existing.setTelephone(patient.getTelephone());
					existing.setDateNaissance(patient.getDateNaissance());
					existing.setGenre(patient.getGenre());
					existing.setGroupeSanguin(patient.getGroupeSanguin());
					existing.setMedecinId(patient.getMedecinId());
					existing.setStatut(patient.getStatut());
					existing.setUserId(patient.getUserId());
					if (patient.getFavoriteMedecinIds() != null) {
						existing.setFavoriteMedecinIds(patient.getFavoriteMedecinIds());
					}
					Patient saved = patientRepository.save(existing);
					// keep the user<->patient link in sync (synchronous Feign)
					if (saved.getUserId() != null) {
						try {
							userClient.linkPatient(saved.getUserId(), saved.getId());
						} catch (FeignException e) {
							// non-fatal
						}
					}
					// async fire-and-forget: only when the patient becomes Admitted
					if ("Admitted".equals(saved.getStatut()) && !"Admitted".equals(previousStatut)) {
						patientEventPublisher.publish("ADMITTED", saved.getId(),
								fullName(saved), saved.getEmail());
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

    @Override
    public PatientStats getStats() {
        List<Patient> all = patientRepository.findAll();
        PatientStats s = new PatientStats();
        s.setTotal(all.size());
        s.setActive(countByStatut(all, "Active"));
        s.setAdmitted(countByStatut(all, "Admitted"));
        s.setDischarged(countByStatut(all, "Discharged"));
        s.setAssigned(all.stream().filter(p -> p.getMedecinId() != null).count());
        s.setUnassigned(all.stream().filter(p -> p.getMedecinId() == null).count());
        s.setByBloodType(all.stream()
                .filter(p -> p.getGroupeSanguin() != null && !p.getGroupeSanguin().isBlank())
                .collect(Collectors.groupingBy(Patient::getGroupeSanguin, Collectors.counting())));
        return s;
    }

    private long countByStatut(List<Patient> list, String statut) {
        return list.stream().filter(p -> statut.equals(p.getStatut())).count();
    }

    @Override
    public PatientDetailsResponse getPatientDetails(int id) {
        Patient p = patientRepository.findById(id).orElse(null);
        if (p == null) {
            return null;
        }

        // assigned doctor lives in this same service -> direct repository lookup
        Medecin doctor = (p.getMedecinId() != null)
                ? medecinRepository.findById(p.getMedecinId()).orElse(null)
                : null;

        // linked account lives in MSUser -> SYNCHRONOUS Feign call (UserClient)
        UserDto linked = null;
        if (p.getUserId() != null) {
            try {
                linked = userClient.getUserById(p.getUserId());
            } catch (FeignException.NotFound e) {
                linked = null;   // account no longer exists; details still returned
            } catch (FeignException e) {
                throw new IllegalStateException("MSUser unreachable: " + e.getMessage());
            }
        }

        return new PatientDetailsResponse(p, doctor, linked);
    }

    @Override
    public List<Patient> filter(String statut, String genre, String groupeSanguin, Integer medecinId) {
        return patientRepository.filter(
                blankToNull(statut),
                blankToNull(genre),
                blankToNull(groupeSanguin),
                medecinId);
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}

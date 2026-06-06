package tn.esprit.spring.patientmedcin;

import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.spring.patientmedcin.client.IdentityProvisioningService;
import tn.esprit.spring.patientmedcin.client.UserClient;
import tn.esprit.spring.patientmedcin.client.UserDto;
import tn.esprit.spring.patientmedcin.messaging.MedecinEventPublisher;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedecinService implements IMedecinService {

    private static final String DEFAULT_PASSWORD = "changeme123";

    private final MedecinRepository medecinRepository;
    private final UserClient userClient;
    private final IdentityProvisioningService identityProvisioningService;
    private final MedecinEventPublisher medecinEventPublisher;

    public MedecinService(MedecinRepository medecinRepository,
                          UserClient userClient,
                          IdentityProvisioningService identityProvisioningService,
                          MedecinEventPublisher medecinEventPublisher) {
        this.medecinRepository = medecinRepository;
        this.userClient = userClient;
        this.identityProvisioningService = identityProvisioningService;
        this.medecinEventPublisher = medecinEventPublisher;
    }

    @Override
    public List<Medecin> getAll() {
        return medecinRepository.findAll();
    }

    @Override
    @Transactional
    public Medecin create(Medecin medecin) {
        // Auto-create a MEDECIN login if none was provided.
        if (medecin.getUserId() == null && medecin.getEmail() != null) {
            medecin.setUserId(provisionMedecinAccount(medecin));
        }
        Medecin saved = medecinRepository.save(medecin);

        // ASYNCHRONOUS (RabbitMQ): tell MSNotification a doctor was created.
        medecinEventPublisher.publish("CREATED", saved.getId(),
                fullName(saved), saved.getSpecialite(), saved.getEmail());
        return saved;
    }

    private String fullName(Medecin m) {
        return ("Dr. " + (m.getNom() != null ? m.getNom() : "") + " "
                + (m.getPrenom() != null ? m.getPrenom() : "")).trim();
    }

    /** Create MSUser account (role MEDECIN) + Keycloak login. Returns MSUser id or null. */
    private Integer provisionMedecinAccount(Medecin medecin) {
        String username = medecin.getEmail();
        Integer userId = null;
        try {
            UserDto created = userClient.createUser(
                    new UserDto(username, medecin.getEmail(), DEFAULT_PASSWORD, "MEDECIN"));
            userId = created.getId();
        } catch (FeignException e) {
            return null;   // MSUser unreachable -> doctor still created, unlinked
        }
        identityProvisioningService.createLogin(username, medecin.getEmail(), DEFAULT_PASSWORD,
                "MEDECIN", medecin.getPrenom(), medecin.getNom());
        return userId;
    }

    @Override
    @Transactional
    public Medecin update(int id, Medecin medecin) {
        return medecinRepository.findById(id)
                .map(existing -> {
                    existing.setNom(medecin.getNom());
                    existing.setPrenom(medecin.getPrenom());
                    existing.setEmail(medecin.getEmail());
                    existing.setSpecialite(medecin.getSpecialite());
                    existing.setTelephone(medecin.getTelephone());
                    existing.setExperience(medecin.getExperience());
                    existing.setStatut(medecin.getStatut());
                    return medecinRepository.save(existing);
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public boolean delete(int id) {
        if (!medecinRepository.existsById(id)) {
            return false;
        }
        medecinRepository.deleteById(id);
        return true;
    }

    @Override
    public Medecin getById(int id) {
        return medecinRepository.findById(id).orElse(null);
    }

    @Override
    public MedecinStats getStats() {
        List<Medecin> all = medecinRepository.findAll();
        MedecinStats s = new MedecinStats();
        s.setTotal(all.size());
        s.setAvailable(countByStatut(all, "Available"));
        s.setInConsultation(countByStatut(all, "In Consultation"));
        s.setOffDuty(countByStatut(all, "Off Duty"));
        s.setAvgExperience(all.stream()
                .filter(m -> m.getExperience() != null)
                .mapToInt(Medecin::getExperience)
                .average()
                .orElse(0.0));
        s.setBySpecialty(all.stream()
                .filter(m -> m.getSpecialite() != null && !m.getSpecialite().isBlank())
                .collect(Collectors.groupingBy(Medecin::getSpecialite, Collectors.counting())));
        return s;
    }

    private long countByStatut(List<Medecin> list, String statut) {
        return list.stream().filter(m -> statut.equals(m.getStatut())).count();
    }

    @Override
    public List<Medecin> filter(String statut, String specialite) {
        return medecinRepository.filter(blankToNull(statut), blankToNull(specialite));
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}

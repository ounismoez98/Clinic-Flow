package tn.esprit.spring.patientmedcin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients")
public class PatientController {

    private final IPatientService patientService;
    private final IMedecinService medecinService;

    public PatientController(IPatientService patientService, IMedecinService medecinService) {
        this.patientService = patientService;
        this.medecinService = medecinService;
    }

    @GetMapping
    public ResponseEntity<List<Patient>> getAll() {
        List<Patient> patients = patientService.getAll();
        if (patients.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getById(@PathVariable int id) {
        return patientService.getPatientById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Patient> create(@RequestBody Patient patient) {
        Patient created = patientService.create(patient);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patient> update(@PathVariable int id, @RequestBody Patient patient) {
        Patient updated = patientService.update(id, patient);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/with-linked-account")
    public ResponseEntity<PatientLinkedAccountResponse> getWithLinkedAccount(@PathVariable int id) {
        PatientLinkedAccountResponse body = patientService.getPatientWithLinkedAccountFeign(id);
        if (body == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        if (!patientService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/favorite-medecins")
    public ResponseEntity<List<Medecin>> getFavoriteMedecins(@PathVariable int id) {
        List<Medecin> favorites = patientService.getFavoriteMedecins(id);
        return ResponseEntity.ok(favorites);
    }

    @PostMapping("/{id}/favorite-medecins/{medecinId}")
    public ResponseEntity<String> saveFavoriteMedecin(@PathVariable int id, @PathVariable int medecinId) {
        Medecin medecin = medecinService.getById(medecinId);
        if (medecin == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Medecin not found with ID: " + medecinId);
        }
        patientService.saveFavoriteMedecin(id, medecinId);
        return ResponseEntity.ok("Medecin saved as favorite successfully.");
    }
    @GetMapping("/search")
    public ResponseEntity<List<Patient>> search(
            @RequestParam String nom,
            @RequestParam String prenom) {
        List<Patient> patients = patientService.searchByNomAndPrenom(nom, prenom);
        if (patients.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(patients);
    }
}

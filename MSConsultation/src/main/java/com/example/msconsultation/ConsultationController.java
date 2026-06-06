package com.example.msconsultation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/consultations")
@CrossOrigin(origins = "*")
public class ConsultationController {

    @Autowired
    private ConsultationService consultationService;

    @PostMapping
    public ResponseEntity<Consultation> create(@RequestBody Consultation consultation) {
        try {
            Consultation created = consultationService.createConsultation(consultation);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<Consultation>> getAll() {
        List<Consultation> consultations = consultationService.getAll();
        if (consultations.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(consultations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Consultation> getById(@PathVariable Long id) {
        try {
            Consultation consultation = consultationService.getById(id);
            return ResponseEntity.ok(consultation);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Consultation>> getByPatient(@PathVariable int patientId) {
        List<Consultation> consultations = consultationService.getByPatient(patientId);
        return ResponseEntity.ok(consultations);
    }

    @GetMapping("/medecin/{medecinId}")
    public ResponseEntity<List<Consultation>> getByMedecin(@PathVariable int medecinId) {
        List<Consultation> consultations = consultationService.getByMedecin(medecinId);
        return ResponseEntity.ok(consultations);
    }

    @GetMapping("/rendezvous/{rendezVousId}")
    public ResponseEntity<List<Consultation>> getByRendezVous(@PathVariable Long rendezVousId) {
        List<Consultation> consultations = consultationService.getByRendezVous(rendezVousId);
        return ResponseEntity.ok(consultations);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            consultationService.deleteConsultation(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

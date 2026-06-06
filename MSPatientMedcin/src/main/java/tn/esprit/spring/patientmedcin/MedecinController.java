package tn.esprit.spring.patientmedcin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/medecins")
public class MedecinController {

    private final IMedecinService medecinService;

    public MedecinController(IMedecinService medecinService) {
        this.medecinService = medecinService;
    }

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello from MicroService Patient / Medecin";
    }

    @GetMapping
    public ResponseEntity<List<Medecin>> getAll() {
        List<Medecin> medecins = medecinService.getAll();
        if (medecins.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(medecins);
    }

    @PostMapping
    public ResponseEntity<Medecin> create(@RequestBody Medecin medecin) {
        Medecin created = medecinService.create(medecin);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medecin> update(@PathVariable int id, @RequestBody Medecin medecin) {
        Medecin updated = medecinService.update(id, medecin);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        if (!medecinService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<MedecinStats> getStats() {
        return ResponseEntity.ok(medecinService.getStats());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Medecin>> filter(
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String specialite) {
        return ResponseEntity.ok(medecinService.filter(statut, specialite));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medecin> getById(@PathVariable int id) {
        Medecin medecin = medecinService.getById(id);
        if (medecin == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(medecin);
    }
}

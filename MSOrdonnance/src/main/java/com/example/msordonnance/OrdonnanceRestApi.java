package com.example.msordonnance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordonnances")
public class OrdonnanceRestApi {
    @RequestMapping("/hello")
    public String sayHello() {
        return "Hello FROM MS Ordonnance";
    }

    @Autowired
    private IOrdonnanceService iOrdonnanceService;

    @GetMapping
    public ResponseEntity<List<Ordonnance>> getAll() {
        List<Ordonnance> ordonnances = iOrdonnanceService.getAll();
        if (ordonnances.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(ordonnances);
    }

    @RequestMapping("/medicaments")
    public List<MedicamentDTO> getAllMedicaments() {
        return iOrdonnanceService.getAllMedicaments();
    }

    @RequestMapping("medicaments/{id}")
    public MedicamentDTO getMedicamentById(@PathVariable int id) {
        return iOrdonnanceService.getMedicamentById(id);
    }

    @GetMapping("/{id}/medicaments")
    public List<MedicamentDTO> getMedicamentsByOrdonnance(@PathVariable int id) {
        return iOrdonnanceService.getMedicamentsByOrdonnance(id);
    }

    @PostMapping("/{id}/medicaments/{medicamentId}")
    public ResponseEntity<String> addMedicamentToOrdonnance(@PathVariable int id, @PathVariable int medicamentId) {
        MedicamentDTO medicament = iOrdonnanceService.getMedicamentById(medicamentId);
        if (medicament != null) {
            iOrdonnanceService.saveMedicamentToOrdonnance(id, medicamentId);
            return ResponseEntity.status(HttpStatus.OK).body("Medicament added to ordonnance successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Medicament not found with ID: " + medicamentId);
        }
    }

    @Value("${welcome.message}")
    private String welcomeMessage;
    @Value("${server.port}")
    private String portserver;

    @GetMapping("/welcome")
    public String welcome() {
        return welcomeMessage + " " + portserver;
    }
}

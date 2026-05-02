package com.example.msfacture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/factures")
public class FactureController {

    @Autowired
    private FactureService factureService;

    @PostMapping
    public Facture create(@RequestBody Facture facture) {
        return factureService.createFacture(facture);
    }

    @GetMapping
    public List<Facture> getAll() {
        return factureService.getAll();
    }

    @GetMapping("/{id}")
    public Facture getById(@PathVariable Long id) {
        return factureService.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        factureService.deleteFacture(id);
    }

    @PutMapping("/{id}/payer")
    public Facture payer(@PathVariable Long id) {
        return factureService.markAsPaid(id);
    }

    @GetMapping("/patient/{id}")
    public List<Facture> getByPatient(@PathVariable int id) {
        return factureService.getByPatient(id);
    }

    @GetMapping("/statut/{statut}")
    public List<Facture> getByStatut(@PathVariable StatutFacture statut) {
        return factureService.getByStatut(statut);
    }

    @GetMapping("/search")
    public List<Facture> search(
            @RequestParam int patientId,
            @RequestParam StatutFacture statut) {

        return factureService.getByPatientAndStatut(patientId, statut);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPdfByPatient(@PathVariable int id) {
        byte[] pdf = factureService.generatePdfByPatient(id);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=facture_patient_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
    @GetMapping("/search-by-patient")
    public List<Facture> searchByPatientName(
            @RequestParam String nom,
            @RequestParam String prenom) {
        return factureService.findByPatientName(nom, prenom);
    }

    @GetMapping("/count-today")
    public long countToday() {
        return factureService.countFacturesToday();
    }

    @GetMapping("/count-by-date")
    public long countByDate(@RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        return factureService.countFacturesByDate(date);
    }

}

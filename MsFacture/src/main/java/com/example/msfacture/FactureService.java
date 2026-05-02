package com.example.msfacture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@org.springframework.transaction.annotation.Transactional
public class FactureService {

    @Autowired
    private FactureRepository factureRepository;

    @Autowired
    private PatientClient patientClient;

    public Facture createFacture(Facture facture) {

        // 🔹 Associer chaque ligne à la facture
        if (facture.getLignes() != null) {
            facture.getLignes().forEach(l -> l.setFacture(facture));
        }

        // 🔹 Calcul HT
        double montantHT = facture.getLignes()
                .stream()
                .mapToDouble(l -> l.getPrix() * l.getQuantite())
                .sum();

        // 🔹 TVA
        double taux = (facture.getTva() != null) ? facture.getTva().getTaux() : 0;
        double montantTVA = montantHT * taux;

        // 🔹 TTC
        double montantTTC = montantHT + montantTVA;

        facture.setMontantHT(montantHT);
        facture.setMontantTTC(montantTTC);

        return factureRepository.save(facture);
    }

    public List<Facture> getAll() {
        return factureRepository.findAll();
    }

    public Facture getById(Long id) {
        return factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture not found"));
    }

    public void deleteFacture(Long id) {
        factureRepository.deleteById(id);
    }

    @Autowired
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    public Facture markAsPaid(Long id) {
        Facture f = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture not found with id " + id));

        f.setStatut(StatutFacture.PAYEE);
        Facture saved = factureRepository.save(f);

        // 🔹 Envoi du message à RabbitMQ
        rabbitTemplate.convertAndSend("facture_paid_queue", 
            new FacturePaidEvent(saved.getId(), saved.getPatientId(), saved.getMontantTTC()));

        return saved;
    }

    public List<Facture> getByPatient(int patientId) {
        return factureRepository.findByPatientId(patientId);
    }

    public List<Facture> getByStatut(StatutFacture statut) {
        return factureRepository.findByStatut(statut);
    }

    public List<Facture> getByPatientAndStatut(int patientId, StatutFacture statut) {
        return factureRepository.findByPatientAndStatut(patientId, statut);
    }

    @Autowired
    private PdfService pdfService;

    public byte[] generatePdf(Long id) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture not found"));

        facture.getLignes().size(); // Force loading of the lines collection

        DTOPatient patient = null;
        try {
            int targetId = facture.getPatientId();
            System.out.println("Generating PDF for Facture #" + id + ". Looking for Patient ID: " + targetId);

            List<DTOPatient> allPatients = patientClient.getAllPatients();
            System.out.println("Total patients fetched: " + allPatients.size());

            // Log every patient in the list to see what we got
            allPatients
                    .forEach(p -> System.out.println(" -> Patient in list: ID=" + p.getId() + ", Name=" + p.getNom()));

            patient = allPatients.stream()
                    .filter(p -> p.getId() == targetId)
                    .findFirst()
                    .orElse(null);

            if (patient != null) {
                System.out.println("MATCH FOUND: Patient ID " + targetId + " is " + patient.getNom());
            } else {
                System.out.println("NO MATCH for ID: " + targetId);
            }
        } catch (Exception e) {
            System.err.println("Error during patient lookup: " + e.getMessage());
        }

        return pdfService.generateFacturePdf(facture, patient);
    }

    public byte[] generatePdfByPatient(int patientId) {
        List<Facture> factures = getByPatient(patientId);
        if (factures.isEmpty()) {
            throw new RuntimeException("No invoices found for patient ID: " + patientId);
        }
        // Take the most recent one (the last one in the list usually)
        Facture latestFacture = factures.get(factures.size() - 1);
        return generatePdf(latestFacture.getId());
    }

    public List<Facture> findByPatientName(String nom, String prenom) {
        try {
            System.out.println("Searching for patient: " + nom + " " + prenom);
            // 1. Call MSPatientMedcin to search for patients by name
            List<DTOPatient> patients = patientClient.searchByNomAndPrenom(nom, prenom);

            // 🔹 Smart Search: If nothing found, try swapping Nom and Prenom
            if (patients == null || patients.isEmpty()) {
                System.out.println("No results for " + nom + " " + prenom + ". Trying swapped version...");
                patients = patientClient.searchByNomAndPrenom(prenom, nom);
            }

            if (patients == null || patients.isEmpty()) {
                System.out.println("No patients found with name: " + nom + " " + prenom);
                return List.of();
            }

            System.out.println("Found " + patients.size() + " patients. Fetching invoices...");

            // 2. Use those IDs to query FactureRepository
            return patients.stream()
                    .flatMap(p -> {
                        List<Facture> factures = factureRepository.findByPatientId((int) p.getId());
                        return factures != null ? factures.stream() : java.util.stream.Stream.empty();
                    })
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error searching invoices by patient name: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error communicating with Patient Service: " + e.getMessage());
        }
    }

    public long countFacturesByDate(java.time.LocalDate date) {
        return factureRepository.countByDateFacture(date);
    }

    public long countFacturesToday() {
        return countFacturesByDate(java.time.LocalDate.now());
    }

}

package com.example.msconsultation;

import com.example.msconsultation.dto.ConsultationFinalizedEvent;
import com.example.msconsultation.dto.MedecinDto;
import com.example.msconsultation.dto.PatientDto;
import com.example.msconsultation.feign.PatientMedecinClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ConsultationService {

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private PatientMedecinClient patientMedecinClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public Consultation createConsultation(Consultation consultation) {
        // 1. Verify Patient Exists
        try {
            PatientDto patient = patientMedecinClient.getPatientById(consultation.getPatientId());
            if (patient == null) {
                throw new RuntimeException("Patient with ID " + consultation.getPatientId() + " not found.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Validation failed: Could not verify Patient. Details: " + e.getMessage());
        }

        // 2. Verify Medecin Exists
        try {
            MedecinDto medecin = patientMedecinClient.getMedecinById(consultation.getMedecinId());
            if (medecin == null) {
                throw new RuntimeException("Medecin with ID " + consultation.getMedecinId() + " not found.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Validation failed: Could not verify Medecin. Details: " + e.getMessage());
        }

        // 3. Save the Consultation
        Consultation saved = consultationRepository.save(consultation);

        // 4. Publish billing event to RabbitMQ
        ConsultationFinalizedEvent event = new ConsultationFinalizedEvent(
                saved.getId(),
                saved.getPatientId(),
                saved.getMedecinId(),
                saved.getPrixConsultation(),
                saved.getDateConsultation()
        );
        rabbitTemplate.convertAndSend(RabbitConfig.CONSULTATION_FINALIZED_QUEUE, event);

        return saved;
    }

    public List<Consultation> getAll() {
        return consultationRepository.findAll();
    }

    public Consultation getById(Long id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consultation not found with id " + id));
    }

    public List<Consultation> getByPatient(int patientId) {
        return consultationRepository.findByPatientId(patientId);
    }

    public List<Consultation> getByMedecin(int medecinId) {
        return consultationRepository.findByMedecinId(medecinId);
    }

    public List<Consultation> getByRendezVous(Long rendezVousId) {
        return consultationRepository.findByRendezVousId(rendezVousId);
    }

    public void deleteConsultation(Long id) {
        if (!consultationRepository.existsById(id)) {
            throw new RuntimeException("Consultation not found with id " + id);
        }
        consultationRepository.deleteById(id);
    }
}

package com.example.msconsultation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    List<Consultation> findByPatientId(int patientId);
    List<Consultation> findByMedecinId(int medecinId);
    List<Consultation> findByRendezVousId(Long rendezVousId);
}

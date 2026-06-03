package com.example.msfacture;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FactureRepository extends JpaRepository<Facture, Long> {

    @Query("SELECT f FROM Facture f WHERE f.patientId = :patientId")
    List<Facture> findByPatientId(@Param("patientId") int patientId);

    @Query("SELECT f FROM Facture f WHERE f.statut = :statut")
    List<Facture> findByStatut(@Param("statut") StatutFacture statut);

    @Query("SELECT f FROM Facture f WHERE f.patientId = :patientId AND f.statut = :statut")
    List<Facture> findByPatientAndStatut(@Param("patientId") int patientId,
            @Param("statut") StatutFacture statut);

    @Query("SELECT COUNT(f) FROM Facture f WHERE f.dateFacture = :date")
    long countByDateFacture(@Param("date") java.time.LocalDate date);

}

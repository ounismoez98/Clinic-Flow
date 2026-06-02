package tn.esprit.spring.patientmedcin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {

    @Query("select p from Patient p where p.nom like :name")
    Page<Patient> patientsByNom(@Param("name") String name, Pageable pageable);

    List<Patient> findByNomIgnoreCaseAndPrenomIgnoreCase(String nom, String prenom);
}
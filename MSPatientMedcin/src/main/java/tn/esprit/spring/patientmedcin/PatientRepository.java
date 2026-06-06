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

    /**
     * Filter patients in the DB. Any param left null is ignored, so any
     * combination of filters works with a single query.
     */
    @Query("""
            select p from Patient p
            where (:statut is null or p.statut = :statut)
              and (:genre is null or p.genre = :genre)
              and (:groupeSanguin is null or p.groupeSanguin = :groupeSanguin)
              and (:medecinId is null or p.medecinId = :medecinId)
            """)
    List<Patient> filter(@Param("statut") String statut,
                         @Param("genre") String genre,
                         @Param("groupeSanguin") String groupeSanguin,
                         @Param("medecinId") Integer medecinId);
}
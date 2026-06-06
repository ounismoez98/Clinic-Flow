package tn.esprit.spring.patientmedcin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, Integer> {

    /** Filter doctors in the DB. Null params are ignored. */
    @Query("""
            select m from Medecin m
            where (:statut is null or m.statut = :statut)
              and (:specialite is null or m.specialite = :specialite)
            """)
    List<Medecin> filter(@Param("statut") String statut,
                         @Param("specialite") String specialite);
}

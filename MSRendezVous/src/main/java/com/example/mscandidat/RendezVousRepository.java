package com.example.mscandidat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RendezVousRepository extends JpaRepository<RendezVous,Integer> {


    @Query("select r from RendezVous r where r.patient like :patient")
    public Page<RendezVous> findByPatient(@Param("patient") String patient, Pageable pageable);

    @Query("select r from RendezVous r where r.medcin like :medcin")
    public Page<RendezVous> findByMedcin(@Param("medcin") String medcin, Pageable pageable);

    @Query("select r from RendezVous r where r.cause like :cause")
    public Page<RendezVous> findByCause(@Param("cause") String cause, Pageable pageable);


}

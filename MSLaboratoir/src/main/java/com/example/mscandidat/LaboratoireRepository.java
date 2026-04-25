package com.example.mscandidat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LaboratoireRepository extends JpaRepository<Laboratoire, Integer> {

    @Query("select l from Laboratoire l where l.nom like :name")
    Page<Laboratoire> laboratoireByNom(@Param("name") String n, Pageable pageable);
}

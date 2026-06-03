package com.example.msfacture;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "MSPatientMedcin")
public interface PatientClient {

    @GetMapping("/patients/{id}")
    DTOPatient getPatientById(@PathVariable("id") int id);

    @GetMapping("/patients")
    List<DTOPatient> getAllPatients();

    @GetMapping("/patients/search")
    List<DTOPatient> searchByNomAndPrenom(@RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom);
}

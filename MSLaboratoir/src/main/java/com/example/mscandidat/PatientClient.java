package com.example.mscandidat;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "MSPatientMedcin-patient", url = "http://localhost:8082")
public interface PatientClient {
    @GetMapping("/patients/{id}")
    PatientDTO getPatientById(@PathVariable("id") int id);
}

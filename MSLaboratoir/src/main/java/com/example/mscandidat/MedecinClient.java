package com.example.mscandidat;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "MSPatientMedcin", url = "http://localhost:8082")
public interface MedecinClient {
    @GetMapping("/medecins/{id}")
    MedecinDTO getMedecinById(@PathVariable("id") int id);
}

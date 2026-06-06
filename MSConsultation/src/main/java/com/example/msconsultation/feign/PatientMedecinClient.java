package com.example.msconsultation.feign;

import com.example.msconsultation.dto.MedecinDto;
import com.example.msconsultation.dto.PatientDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "MSPatientMedcin")
public interface PatientMedecinClient {

    @GetMapping("/patients/{id}")
    PatientDto getPatientById(@PathVariable("id") int id);

    @GetMapping("/medecins/{id}")
    MedecinDto getMedecinById(@PathVariable("id") int id);
}

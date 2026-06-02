package tn.esprit.spring.notification;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "MSPatientMedcin")
public interface PatientClient {

    @GetMapping("/patients/{id}")
    DTOPatient getPatientById(@PathVariable("id") int id);
}

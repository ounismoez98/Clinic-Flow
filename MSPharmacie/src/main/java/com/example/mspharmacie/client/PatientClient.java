package com.example.mspharmacie.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "MSPatientMedcin")
public interface PatientClient {

	@GetMapping("/patients/{id}")
	PatientSummaryDto getPatientById(@PathVariable("id") int id);
}

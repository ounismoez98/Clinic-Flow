package tn.esprit.spring.patientmedcin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "MSUser")
public interface UserClient {

	@GetMapping("/users/{id}")
	UserDto getUserById(@PathVariable("id") int id);

	/** Create an app account in MSUser (role PATIENT or MEDECIN). */
	@PostMapping("/users")
	UserDto createUser(@RequestBody UserDto user);

	/** Synchronously link a user to a patient (sets linkedPatientId). */
	@PutMapping("/users/{id}/link/{patientId}")
	void linkPatient(@PathVariable("id") int id, @PathVariable("patientId") int patientId);
}

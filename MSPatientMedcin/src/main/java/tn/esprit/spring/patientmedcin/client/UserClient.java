package tn.esprit.spring.patientmedcin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "MSUser")
public interface UserClient {

	@GetMapping("/users/{id}")
	UserDto getUserById(@PathVariable("id") int id);
}

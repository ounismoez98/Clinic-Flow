package com.example.msordonnance;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "MSPharmacie")
public interface MedicamentClient {

	@GetMapping("/medicaments")
	List<MedicamentDTO> getAll();

	@GetMapping("/medicaments/{id}")
	MedicamentDTO getMedicamentById(@PathVariable("id") int id);
}

package com.example.mspharmacie.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mspharmacie.ai.GeminiSummaryService;
import com.example.mspharmacie.IMedicamentService;
import com.example.mspharmacie.dto.CreateMedicamentRequest;
import com.example.mspharmacie.dto.MedicamentAssistantSummaryDto;
import com.example.mspharmacie.dto.DispenseRequestDto;
import com.example.mspharmacie.dto.MedicamentResponseDto;
import com.example.mspharmacie.dto.PatchStockRequest;
import com.example.mspharmacie.dto.StockInfoDto;
import com.example.mspharmacie.dto.UpdateMedicamentRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/medicaments")
public class MedicamentRestController {

	private final IMedicamentService medicamentService;
	private final GeminiSummaryService geminiSummaryService;

	public MedicamentRestController(IMedicamentService medicamentService, GeminiSummaryService geminiSummaryService) {
		this.medicamentService = medicamentService;
		this.geminiSummaryService = geminiSummaryService;
	}

	@GetMapping
	public ResponseEntity<List<MedicamentResponseDto>> listCatalog() {
		List<MedicamentResponseDto> list = medicamentService.findAllCatalog();
		if (list.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(list);
	}

	@GetMapping("/{id}/assistant-summary")
	public MedicamentAssistantSummaryDto assistantSummary(@PathVariable int id) {
		return geminiSummaryService.summarizeMedicamentCatalog(medicamentService.findCatalogById(id));
	}

	@GetMapping("/{id}")
	public MedicamentResponseDto getCatalog(@PathVariable int id) {
		return medicamentService.findCatalogById(id);
	}

	@GetMapping("/{id}/stock-info")
	public StockInfoDto getStock(@PathVariable int id) {
		return medicamentService.getStockInfo(id);
	}

	@PostMapping
	public ResponseEntity<MedicamentResponseDto> create(@Valid @RequestBody CreateMedicamentRequest body) {
		MedicamentResponseDto created = medicamentService.create(body);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping("/{id}")
	public MedicamentResponseDto update(@PathVariable int id, @Valid @RequestBody UpdateMedicamentRequest body) {
		return medicamentService.update(id, body);
	}

	@PatchMapping("/{id}/stock")
	public MedicamentResponseDto patchStock(@PathVariable int id, @Valid @RequestBody PatchStockRequest body) {
		return medicamentService.patchStock(id, body);
	}

	@PostMapping("/{id}/dispense")
	public MedicamentResponseDto dispense(@PathVariable int id, @Valid @RequestBody DispenseRequestDto body) {
		return medicamentService.dispense(id, body);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable int id) {
		medicamentService.delete(id);
		return ResponseEntity.noContent().build();
	}
}

package com.example.mspharmacie.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import feign.FeignException;

@RestControllerAdvice
public class RestExceptionHandler {

	@ExceptionHandler(MedicamentNotFoundException.class)
	public ResponseEntity<Void> handleMedicamentNotFound(MedicamentNotFoundException ex) {
		return ResponseEntity.notFound().build();
	}

	@ExceptionHandler(PatientNotFoundException.class)
	public ResponseEntity<String> handlePatientNotFound(PatientNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	}

	@ExceptionHandler(InsufficientStockException.class)
	public ResponseEntity<String> handleInsufficientStock(InsufficientStockException ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
	}

	@ExceptionHandler(GeminiNotConfiguredException.class)
	public ResponseEntity<String> handleGeminiNotConfigured(GeminiNotConfiguredException ex) {
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
	}

	@ExceptionHandler(GeminiUpstreamException.class)
	public ResponseEntity<String> handleGeminiUpstream(GeminiUpstreamException ex) {
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ex.getMessage());
	}

	/** When patient service is down or Eureka has no instance — {@code MedicamentService} rethrows non-404 Feign errors. */
	public ResponseEntity<String> handleFeign(FeignException ex) {
		if (ex.status() == 404) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient introuvable (réponse 404 du service patient).");
		}
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
				"Service patient injoignable via Feign. Vérifier Eureka + MSPatientMedcin démarrés. Détail : " + ex.getMessage());
	}
}

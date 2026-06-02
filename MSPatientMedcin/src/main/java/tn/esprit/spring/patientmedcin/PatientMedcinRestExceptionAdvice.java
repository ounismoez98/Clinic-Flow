package tn.esprit.spring.patientmedcin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PatientMedcinRestExceptionAdvice {

	@ExceptionHandler(UnknownUserAccountException.class)
	public ResponseEntity<String> unknownUser(UnknownUserAccountException ex) {
		return ResponseEntity.status(404).body(ex.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> badArgument(IllegalArgumentException ex) {
		return ResponseEntity.badRequest().body(ex.getMessage());
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<String> serviceUnavailable(IllegalStateException ex) {
		return ResponseEntity.status(503).body(ex.getMessage());
	}
}

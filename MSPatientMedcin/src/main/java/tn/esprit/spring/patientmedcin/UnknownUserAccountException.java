package tn.esprit.spring.patientmedcin;

public class UnknownUserAccountException extends RuntimeException {

	public UnknownUserAccountException(int userId) {
		super("No user exists with id: " + userId);
	}
}

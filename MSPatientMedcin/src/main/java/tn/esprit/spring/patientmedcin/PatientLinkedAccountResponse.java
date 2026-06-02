package tn.esprit.spring.patientmedcin;

import tn.esprit.spring.patientmedcin.client.UserDto;

public record PatientLinkedAccountResponse(Patient patient, UserDto linkedUserAccount) {
}

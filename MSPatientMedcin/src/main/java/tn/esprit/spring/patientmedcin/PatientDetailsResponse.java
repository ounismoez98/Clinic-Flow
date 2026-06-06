package tn.esprit.spring.patientmedcin;

import tn.esprit.spring.patientmedcin.client.UserDto;

/**
 * Aggregated, enriched view of a patient for GET /patients/{id}/details.
 *
 * - patient: the patient row itself
 * - assignedDoctor: resolved from medecinId (same service, direct repo lookup)
 * - linkedUserAccount: fetched SYNCHRONOUSLY from MSUser via OpenFeign (UserClient).
 *   This is the real cross-service synchronous call: the response cannot be
 *   built until MSUser answers, so blocking here is the correct behaviour.
 */
public record PatientDetailsResponse(
        Patient patient,
        Medecin assignedDoctor,
        UserDto linkedUserAccount
) {
}

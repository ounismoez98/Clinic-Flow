package tn.esprit.spring.patientmedcin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

/**
 * Calls the Node.js identity service to provision Keycloak logins.
 *
 * This replaces the old KeycloakAdminClient: MSPatientMedcin no longer talks to
 * Keycloak's admin API directly. The Node service is now the SINGLE place that
 * creates Keycloak users. Discovered via Eureka (IDENTITY-SERVICE).
 */
@FeignClient(name = "IDENTITY-SERVICE")
public interface IdentityServiceClient {

    @PostMapping("/api/internal/users")
    Map<String, Object> createKeycloakUser(
            @RequestHeader("X-Internal-Api-Key") String apiKey,
            @RequestBody Map<String, Object> body);
}

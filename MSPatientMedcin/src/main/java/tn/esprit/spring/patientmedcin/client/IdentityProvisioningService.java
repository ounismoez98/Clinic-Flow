package tn.esprit.spring.patientmedcin.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Provisions Keycloak logins by delegating to the Node.js identity service
 * (the single owner of Keycloak admin operations). Replaces direct use of
 * Keycloak's admin API from this Spring service.
 *
 * Best-effort: failures are logged and swallowed so patient/doctor creation is
 * never broken by the identity service being unavailable.
 */
@Component
public class IdentityProvisioningService {

    private static final Logger log = LoggerFactory.getLogger(IdentityProvisioningService.class);

    private final IdentityServiceClient identityClient;
    private final String apiKey;

    public IdentityProvisioningService(
            IdentityServiceClient identityClient,
            @Value("${identity.service.api-key:clinicflow-internal-secret}") String apiKey) {
        this.identityClient = identityClient;
        this.apiKey = apiKey;
    }

    /** Create a Keycloak login via the Node identity service. Returns true on success. */
    public boolean createLogin(String username, String email, String password,
                               String role, String firstName, String lastName) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("username", username);
            body.put("email", email);
            body.put("password", password);
            body.put("role", role);
            body.put("firstName", firstName != null ? firstName : "User");
            body.put("lastName", lastName != null ? lastName : "Account");

            identityClient.createKeycloakUser(apiKey, body);
            log.info("Identity service: created Keycloak login '{}' ({})", username, role);
            return true;
        } catch (Exception e) {
            log.warn("Identity service provisioning failed for '{}' (continuing): {}",
                    username, e.getMessage());
            return false;
        }
    }
}

package tn.esprit.spring.patientmedcin.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Provisions users in Keycloak via its Admin REST API so auto-created
 * patients/doctors can actually log in.
 *
 * Best-effort: any failure here is logged and swallowed by the caller, so a
 * Keycloak hiccup never breaks patient/doctor creation.
 */
@Component
public class KeycloakAdminClient {

    private static final Logger log = LoggerFactory.getLogger(KeycloakAdminClient.class);

    private final String baseUrl;     // e.g. http://keycloak:8080
    private final String realm;       // clinic-flow
    private final String adminUser;
    private final String adminPass;
    private final RestClient rest = RestClient.create();

    public KeycloakAdminClient(
            @Value("${keycloak.admin.base-url:http://localhost:8180}") String baseUrl,
            @Value("${keycloak.admin.realm:clinic-flow}") String realm,
            @Value("${keycloak.admin.username:admin}") String adminUser,
            @Value("${keycloak.admin.password:admin}") String adminPass) {
        this.baseUrl = baseUrl;
        this.realm = realm;
        this.adminUser = adminUser;
        this.adminPass = adminPass;
    }

    /**
     * Creates a user in the realm with the given role and a password.
     * Returns true on success, false if anything failed (already logged).
     */
    public boolean createUser(String username, String email, String password, String role,
                              String firstName, String lastName) {
        try {
            String token = adminToken();

            // 1. Create the user (enabled, with credentials inline).
            //    firstName/lastName are required: Keycloak's "Verify Profile" otherwise
            //    blocks login with "Account is not fully set up".
            Map<String, Object> body = Map.of(
                    "username", username,
                    "email", email,
                    "firstName", firstName != null ? firstName : "User",
                    "lastName", lastName != null ? lastName : "Account",
                    "enabled", true,
                    "emailVerified", true,
                    "requiredActions", List.of(),
                    "credentials", List.of(Map.of(
                            "type", "password",
                            "value", password,
                            "temporary", false)));

            rest.post()
                    .uri(baseUrl + "/admin/realms/" + realm + "/users")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            // 2. Find the new user's id, then assign the realm role.
            assignRealmRole(token, username, role);
            log.info("Keycloak: created user '{}' with role {}", username, role);
            return true;

        } catch (Exception e) {
            log.warn("Keycloak provisioning failed for '{}' (continuing anyway): {}", username, e.getMessage());
            return false;
        }
    }

    private String adminToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "password");
        form.add("client_id", "admin-cli");
        form.add("username", adminUser);
        form.add("password", adminPass);

        Map<?, ?> resp = rest.post()
                .uri(baseUrl + "/realms/master/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        return (String) resp.get("access_token");
    }

    @SuppressWarnings("unchecked")
    private void assignRealmRole(String token, String username, String role) {
        // Look up the user id by username.
        List<Map<String, Object>> users = rest.get()
                .uri(baseUrl + "/admin/realms/" + realm + "/users?username=" + username + "&exact=true")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(List.class);
        if (users == null || users.isEmpty()) {
            return;
        }
        String userId = String.valueOf(users.get(0).get("id"));

        // Look up the role representation.
        Map<String, Object> roleRep = rest.get()
                .uri(baseUrl + "/admin/realms/" + realm + "/roles/" + role)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(Map.class);
        if (roleRep == null) {
            return;
        }

        // Assign it.
        rest.post()
                .uri(baseUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(List.of(roleRep))
                .retrieve()
                .toBodilessEntity();
    }
}

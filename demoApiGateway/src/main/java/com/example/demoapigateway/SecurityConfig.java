package com.example.demoapigateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security at the gateway.
 *
 * Every incoming request must carry a valid Keycloak JWT (Authorization: Bearer ...),
 * except a small public allow-list. The gateway verifies the token's signature using
 * Keycloak's public keys (jwk-set-uri in application.properties), then forwards the
 * request (token included) to the right microservice.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            // Use the CORS rules defined in CorsConfig.
            .cors(cors -> {})
            // Gateway is stateless and called from a browser SPA -> CSRF not needed.
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchange -> exchange
                // CORS preflight requests must pass without a token.
                .pathMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                // Public endpoints (no login required).
                .pathMatchers("/actuator/**", "/welcome-message").permitAll()
                // Everything else requires a valid token.
                .anyExchange().authenticated()
            )
            // Treat this app as an OAuth2 Resource Server validating JWTs.
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
        return http.build();
    }
}

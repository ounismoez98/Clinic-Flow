import { HttpRequest } from '@angular/common/http';
import { KeycloakService } from 'keycloak-angular';
import { keycloakConfig } from './keycloak.config';

/**
 * Initializes Keycloak when the Angular app starts.
 *
 * onLoad: 'check-sso' -> if the user is already logged in, pick up the session
 * silently; if not, the app still loads (no forced redirect). Call
 * keycloak.login() from a button/guard to send the user to the login page.
 */
export function initializeKeycloak(keycloak: KeycloakService): () => Promise<boolean> {
  return () =>
    keycloak
      .init({
        config: keycloakConfig,
        initOptions: {
          onLoad: 'check-sso',
          silentCheckSsoRedirectUri:
            window.location.origin + '/assets/silent-check-sso.html',
          checkLoginIframe: false,
        },
        // Auto-attach the token to the API gateway (8085) and the Node identity API (5000).
        enableBearerInterceptor: true,
        shouldAddToken: (req: HttpRequest<unknown>) =>
          req.url.startsWith('http://localhost:8085') ||
          req.url.startsWith('http://localhost:5000'),
      })
      .catch(err => {
        console.warn(
          'Keycloak unavailable (start Docker Keycloak on :8180 or run run-all-services.ps1). App loads without SSO.',
          err
        );
        return false;
      });
}

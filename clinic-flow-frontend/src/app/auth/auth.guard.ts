import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';

/**
 * Blocks a route unless the user is logged in. If not, it sends them to the
 * Keycloak login page and returns them to the page they wanted afterwards.
 */
export const authGuard: CanActivateFn = async (route, state) => {
  const keycloak = inject(KeycloakService);

  if (keycloak.isLoggedIn()) {
    return true;
  }

  await keycloak.login({
    redirectUri: window.location.origin + state.url,
  });
  return false;
};

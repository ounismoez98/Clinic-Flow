import { Injectable } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';

export interface CurrentUser {
  name: string;        // full name (or username fallback)
  username: string;
  email: string;
  role: string;        // primary app role: ADMIN / MEDECIN / PATIENT
  initials: string;
}

/** App-level roles we care about (ignores Keycloak's built-in default roles). */
const APP_ROLES = ['ADMIN', 'MEDECIN', 'PATIENT'];

@Injectable({ providedIn: 'root' })
export class CurrentUserService {
  constructor(private keycloak: KeycloakService) {}

  /** Reads the logged-in user's profile from the Keycloak token. */
  get(): CurrentUser {
    // The parsed access token holds the standard claims (name, email, roles).
    const token: any = this.keycloak.getKeycloakInstance()?.tokenParsed ?? {};

    const name: string = token.name || token.preferred_username || 'User';
    const username: string = token.preferred_username || '';
    const email: string = token.email || '';

    const roles: string[] = token.realm_access?.roles ?? [];
    const role = roles.find((r) => APP_ROLES.includes(r)) ?? 'USER';

    return { name, username, email, role, initials: this.initialsOf(name) };
  }

  isLoggedIn(): boolean {
    return this.keycloak.isLoggedIn();
  }

  logout(): void {
    this.keycloak.logout(window.location.origin + '/login');
  }

  private initialsOf(name: string): string {
    const parts = name.trim().split(/\s+/);
    if (parts.length >= 2) return (parts[0][0] + parts[1][0]).toUpperCase();
    return (name.slice(0, 2) || 'U').toUpperCase();
  }
}

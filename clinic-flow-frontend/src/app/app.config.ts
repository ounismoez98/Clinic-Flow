import { ApplicationConfig, APP_INITIALIZER } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { KeycloakService, KeycloakBearerInterceptor } from 'keycloak-angular';

import { routes } from './app.routes';
import { initializeKeycloak } from './auth/keycloak-init';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),

    // HttpClient that runs DI-registered interceptors (so the bearer token gets attached).
    provideHttpClient(withInterceptorsFromDi()),

    // The Keycloak service (login, logout, token access).
    KeycloakService,

    // Initialize Keycloak before the app starts.
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi: true,
      deps: [KeycloakService],
    },

    // Auto-attach the Keycloak token to outgoing HTTP requests (see shouldAddToken in keycloak-init).
    {
      provide: HTTP_INTERCEPTORS,
      useClass: KeycloakBearerInterceptor,
      multi: true,
    },
  ],
};

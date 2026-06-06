import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpErrorResponse,
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { KeycloakService } from 'keycloak-angular';

/**
 * Session safety net: if any API call comes back 401 (token expired or invalid),
 * send the user back to the Keycloak login instead of leaving a broken page.
 *
 * We only react to 401 from OUR APIs (gateway / identity service), and we guard
 * against redirect loops by only triggering when actually logged out / expired.
 */
@Injectable()
export class UnauthorizedInterceptor implements HttpInterceptor {
  private redirecting = false;

  constructor(private keycloak: KeycloakService) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(req).pipe(
      catchError((err: HttpErrorResponse) => {
        if (err.status === 401 && this.isOurApi(req.url) && !this.redirecting) {
          this.redirecting = true;
          // Token is no longer accepted -> re-authenticate, returning to the
          // current page afterwards.
          this.keycloak.login({ redirectUri: window.location.href });
        }
        return throwError(() => err);
      })
    );
  }

  private isOurApi(url: string): boolean {
    return url.startsWith('http://localhost:8085') || url.startsWith('http://localhost:5000');
  }
}

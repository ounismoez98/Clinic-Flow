import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Identity API reached THROUGH the Spring gateway:
 *   gateway :8085  -- /iam/** --> (StripPrefix) --> Node :5000 /api/**
 * So /iam/api/users at the gateway hits /api/users on the Node service.
 */
const IAM_BASE = 'http://localhost:8085/iam/api';

export interface IamUser {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  enabled: boolean;
  emailVerified: boolean;
  roles?: string[];
}

export interface IamRole {
  id: string;
  name: string;
  description: string;
}

export interface NewIamUser {
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  password?: string;
  role?: string;
}

/** Calls the Node identity API (which validates our Keycloak token). */
@Injectable({ providedIn: 'root' })
export class IdentityService {
  constructor(private http: HttpClient) {}

  listUsers(search = ''): Observable<IamUser[]> {
    const q = search ? `?search=${encodeURIComponent(search)}` : '';
    return this.http.get<IamUser[]>(`${IAM_BASE}/users${q}`);
  }

  getUser(id: string): Observable<IamUser> {
    return this.http.get<IamUser>(`${IAM_BASE}/users/${id}`);
  }

  createUser(user: NewIamUser): Observable<IamUser> {
    return this.http.post<IamUser>(`${IAM_BASE}/users`, user);
  }

  deleteUser(id: string): Observable<void> {
    return this.http.delete<void>(`${IAM_BASE}/users/${id}`);
  }

  assignRole(id: string, role: string): Observable<any> {
    return this.http.post(`${IAM_BASE}/users/${id}/roles/${role}`, {});
  }

  removeRole(id: string, role: string): Observable<any> {
    return this.http.delete(`${IAM_BASE}/users/${id}/roles/${role}`);
  }

  listRoles(): Observable<IamRole[]> {
    return this.http.get<IamRole[]>(`${IAM_BASE}/roles`);
  }
}

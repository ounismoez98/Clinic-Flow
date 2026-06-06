import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../../core/api.config';

/** Matches the backend Patient entity (tn.esprit.spring.patientmedcin.Patient). */
export interface Patient {
  id?: number;
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
  dateNaissance?: string;   // yyyy-MM-dd
  genre?: string;           // Male / Female
  groupeSanguin?: string;   // A+, O-, ...
  medecinId?: number | null;
  statut?: string;          // Active / Admitted / Discharged
}

/** Linked Keycloak/MSUser account (subset returned by MSUser via Feign). */
export interface LinkedUserAccount {
  id: number;
  username: string;
  role: string;
}

/** Response of GET /patients/{id}/details — patient enriched via Feign. */
export interface PatientDetails {
  patient: Patient;
  assignedDoctor: {
    id: number; nom: string; prenom: string; email: string; specialite?: string;
  } | null;
  linkedUserAccount: LinkedUserAccount | null;
}

/** Matches the backend PatientStats DTO. */
export interface PatientStats {
  total: number;
  active: number;
  admitted: number;
  discharged: number;
  assigned: number;
  unassigned: number;
  byBloodType: Record<string, number>;
}

@Injectable({ providedIn: 'root' })
export class PatientService {
  private url = `${API_BASE}/patients`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Patient[]> {
    return this.http.get<Patient[]>(this.url);
  }

  getStats(): Observable<PatientStats> {
    return this.http.get<PatientStats>(`${this.url}/stats`);
  }

  /** Feign-enriched details: assigned doctor + linked MSUser account. */
  getDetails(id: number): Observable<PatientDetails> {
    return this.http.get<PatientDetails>(`${this.url}/${id}/details`);
  }

  /** Backend filter. Pass only the fields you want to filter by. */
  filter(f: { statut?: string; genre?: string; groupeSanguin?: string; medecinId?: number }): Observable<Patient[]> {
    let params = new HttpParams();
    if (f.statut) params = params.set('statut', f.statut);
    if (f.genre) params = params.set('genre', f.genre);
    if (f.groupeSanguin) params = params.set('groupeSanguin', f.groupeSanguin);
    if (f.medecinId != null) params = params.set('medecinId', String(f.medecinId));
    return this.http.get<Patient[]>(`${this.url}/filter`, { params });
  }

  create(patient: Patient): Observable<Patient> {
    return this.http.post<Patient>(this.url, patient);
  }

  update(id: number, patient: Patient): Observable<Patient> {
    return this.http.put<Patient>(`${this.url}/${id}`, patient);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}

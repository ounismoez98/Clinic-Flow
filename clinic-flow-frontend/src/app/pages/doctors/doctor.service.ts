import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../../core/api.config';

/** Matches the backend Medecin entity (tn.esprit.spring.patientmedcin.Medecin). */
export interface Doctor {
  id?: number;
  nom: string;
  prenom: string;
  email: string;
  specialite?: string;
  telephone?: string;
  experience?: number;   // years
  statut?: string;       // Available / In Consultation / Off Duty
}

/** Matches the backend MedecinStats DTO. */
export interface DoctorStats {
  total: number;
  available: number;
  inConsultation: number;
  offDuty: number;
  avgExperience: number;
  bySpecialty: Record<string, number>;
}

@Injectable({ providedIn: 'root' })
export class DoctorService {
  private url = `${API_BASE}/medecins`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(this.url);
  }

  getStats(): Observable<DoctorStats> {
    return this.http.get<DoctorStats>(`${this.url}/stats`);
  }

  /** Backend filter. Pass only the fields you want to filter by. */
  filter(f: { statut?: string; specialite?: string }): Observable<Doctor[]> {
    let params = new HttpParams();
    if (f.statut) params = params.set('statut', f.statut);
    if (f.specialite) params = params.set('specialite', f.specialite);
    return this.http.get<Doctor[]>(`${this.url}/filter`, { params });
  }

  create(doctor: Doctor): Observable<Doctor> {
    return this.http.post<Doctor>(this.url, doctor);
  }

  update(id: number, doctor: Doctor): Observable<Doctor> {
    return this.http.put<Doctor>(`${this.url}/${id}`, doctor);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}

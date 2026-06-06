import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RENDEZVOUS_API } from '../../core/api.config';

/** Matches the backend RendezVous entity. */
export interface RendezVous {
  id?: number;
  date: string;       // yyyy-MM-dd or free text
  cause: string;
  patient: string;
  medcin: string;
}

@Injectable({ providedIn: 'root' })
export class RendezVousService {
  private url = RENDEZVOUS_API;

  constructor(private http: HttpClient) {}

  getAll(): Observable<RendezVous[]> {
    return this.http.get<RendezVous[]>(`${this.url}/getall`);
  }

  getById(id: number): Observable<RendezVous> {
    return this.http.get<RendezVous>(`${this.url}/rendezvousbyid/${id}`);
  }

  create(rdv: RendezVous): Observable<RendezVous> {
    return this.http.post<RendezVous>(`${this.url}/addrendezvous`, rdv);
  }

  update(id: number, rdv: RendezVous): Observable<RendezVous> {
    return this.http.put<RendezVous>(`${this.url}/updaterendezvous/${id}`, rdv);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/deleterendezvous/${id}`);
  }
}

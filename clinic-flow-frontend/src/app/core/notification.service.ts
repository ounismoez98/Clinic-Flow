import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from './api.config';

/** Matches the backend Notification entity. */
export interface AppNotification {
  id: number;
  type: string;
  message: string;
  recipient: string;
  read: boolean;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private url = `${API_BASE}/notifications`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(this.url);
  }

  unreadCount(): Observable<number> {
    return this.http.get<number>(`${this.url}/unread-count`);
  }

  markRead(id: number): Observable<void> {
    return this.http.put<void>(`${this.url}/${id}/read`, {});
  }

  markAllRead(): Observable<void> {
    return this.http.put<void>(`${this.url}/read-all`, {});
  }
}

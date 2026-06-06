import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';

export interface Consultation {
  id?: number;
  dateConsultation?: string;
  patientId: number;
  medecinId: number;
  rendezVousId?: number;
  ordonnanceId?: number;
  diagnostic: string;
  notes: string;
  prixConsultation: number;
}

export interface Patient {
  id: number;
  nom: string;
  prenom: string;
  email: string;
}

export interface Doctor {
  id: number;
  nom: string;
  prenom: string;
  specialite: string;
}

const CONSULTATIONS_API = 'http://localhost:8089/consultations';
const PATIENTS_API = 'http://localhost:8085/patients';
const DOCTORS_API = 'http://localhost:8085/medecins';

@Component({
  selector: 'app-consultations',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './consultations.component.html',
  styleUrl: './consultations.component.scss'
})
export class ConsultationsComponent implements OnInit {
  consultations: Consultation[] = [];
  patients: Patient[] = [];
  doctors: Doctor[] = [];
  
  loading = false;
  error = '';
  successMsg = '';

  search = '';
  viewMode: 'table' | 'grid' = 'table';

  showModal = false;
  modalMode: 'create' | 'view' = 'create';
  showDeleteConfirm = false;
  deleteTargetId: number | null = null;

  form: Consultation = this.emptyForm();

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadAll();
  }

  loadAll() {
    this.loading = true;
    this.error = '';
    
    this.http.get<Consultation[]>(CONSULTATIONS_API).subscribe({
      next: data => {
        this.consultations = data || [];
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger les consultations. Assurez-vous que MSConsultation tourne.';
        this.loading = false;
      }
    });

    this.http.get<Patient[]>(PATIENTS_API).subscribe({
      next: data => this.patients = data || [],
      error: () => console.warn('Impossible de charger la liste des patients depuis la Gateway.')
    });

    this.http.get<Doctor[]>(DOCTORS_API).subscribe({
      next: data => this.doctors = data || [],
      error: () => console.warn('Impossible de charger la liste des médecins depuis la Gateway.')
    });
  }

  get stats() {
    const total = this.consultations.length;
    const today = this.consultations.filter(c => {
      if (!c.dateConsultation) return false;
      const todayStr = new Date().toISOString().split('T')[0];
      return c.dateConsultation.startsWith(todayStr);
    }).length;
    const revenue = this.consultations.reduce((acc, c) => acc + c.prixConsultation, 0);
    const avg = total > 0 ? (revenue / total) : 0;

    return [
      { label: 'Total Consultations', value: String(total), color: 'text-blue-600 bg-blue-50', icon: 'M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2M9 5a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2M9 5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2' },
      { label: 'Consultations Aujourd\'hui', value: String(today), color: 'text-orange-600 bg-orange-50', icon: 'M12 6v6h4.5m4.5 0a9 9 0 1 1-18 0 9 9 0 0 1 18 0z' },
      { label: 'Revenus Totaux', value: revenue.toFixed(2) + ' DT', color: 'text-green-600 bg-green-50', icon: 'M2.25 18.75a60.07 60.07 0 0 1 15.797 2.101c.727.198 1.453-.342 1.453-1.096V18.75' },
      { label: 'Prix Moyen', value: avg.toFixed(2) + ' DT', color: 'text-purple-600 bg-purple-50', icon: 'M12 6c-3.18 0-6 2-6 6v3.75h12V12c0-4-2.82-6-6-6z' }
    ];
  }

  get filtered(): Consultation[] {
    return this.consultations.filter(c => {
      const pName = this.getPatientName(c.patientId).toLowerCase();
      const dName = this.getDoctorName(c.medecinId).toLowerCase();
      const diag = (c.diagnostic || '').toLowerCase();
      const note = (c.notes || '').toLowerCase();
      const query = this.search.toLowerCase();

      return pName.includes(query) ||
             dName.includes(query) ||
             diag.includes(query) ||
             note.includes(query) ||
             String(c.id).includes(query);
    });
  }

  getPatientName(id: number): string {
    const p = this.patients.find(x => x.id === id);
    return p ? `${p.prenom} ${p.nom}` : `Patient #${id}`;
  }

  getDoctorName(id: number): string {
    const d = this.doctors.find(x => x.id === id);
    return d ? `Dr. ${d.prenom} ${d.nom}` : `Médecin #${id}`;
  }

  getDoctorSpeciality(id: number): string {
    const d = this.doctors.find(x => x.id === id);
    return d ? d.specialite : '';
  }

  save() {
    if (this.form.patientId <= 0 || this.form.medecinId <= 0 || !this.form.diagnostic) {
      this.error = 'Veuillez remplir tous les champs obligatoires (*).';
      return;
    }

    this.loading = true;
    this.http.post<Consultation>(CONSULTATIONS_API, this.form).subscribe({
      next: data => {
        this.consultations.unshift(data);
        this.closeModal();
        this.flash('Consultation enregistrée avec succès !');
        this.loading = false;
      },
      error: err => {
        this.error = err.error?.message || 'Erreur lors de la création de la consultation.';
        this.loading = false;
      }
    });
  }

  confirmDelete(id: number) {
    this.deleteTargetId = id;
    this.showDeleteConfirm = true;
  }

  doDelete() {
    if (this.deleteTargetId == null) return;
    this.loading = true;
    this.http.delete(`${CONSULTATIONS_API}/${this.deleteTargetId}`).subscribe({
      next: () => {
        this.consultations = this.consultations.filter(c => c.id !== this.deleteTargetId);
        this.showDeleteConfirm = false;
        this.deleteTargetId = null;
        this.flash('Consultation supprimée.');
        this.loading = false;
      },
      error: () => {
        this.error = 'Erreur lors de la suppression.';
        this.showDeleteConfirm = false;
        this.loading = false;
      }
    });
  }

  openCreate() {
    this.form = this.emptyForm();
    this.modalMode = 'create';
    this.showModal = true;
  }

  openView(c: Consultation) {
    this.form = { ...c };
    this.modalMode = 'view';
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.error = '';
  }

  emptyForm(): Consultation {
    return {
      patientId: 0,
      medecinId: 0,
      diagnostic: '',
      notes: '',
      prixConsultation: 50.00
    };
  }

  flash(msg: string) {
    this.successMsg = msg;
    setTimeout(() => this.successMsg = '', 3000);
  }

  consultationCode(c: Consultation): string {
    return 'CS' + String(c.id ?? 0).padStart(4, '0');
  }

  formatDate(dStr?: string): string {
    if (!dStr) return '—';
    try {
      const dt = new Date(dStr);
      return dt.toLocaleString('fr-FR', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch {
      return dStr;
    }
  }
}

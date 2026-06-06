import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PatientService, Patient, PatientStats, PatientDetails } from './patient.service';
import { DoctorService, Doctor } from '../doctors/doctor.service';

@Component({
  selector: 'app-patients',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './patients.component.html',
  styleUrl: './patients.component.scss'
})
export class PatientsComponent implements OnInit {
  patients: Patient[] = [];
  doctors: Doctor[] = [];          // to pick an assigned doctor
  stats: PatientStats | null = null;
  loading = false;
  error = '';

  search = '';                       // free-text, filtered client-side
  filterStatus = 'All';              // sent to backend
  filterBlood = 'All';               // sent to backend
  statusOptions = ['All', 'Active', 'Admitted', 'Discharged'];
  bloodOptions = ['All', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];

  // Modal state
  showModal = false;
  saving = false;
  editing = false;
  form: Patient = this.emptyForm();

  // Details panel (Feign-enriched)
  showDetails = false;
  detailsLoading = false;
  details: PatientDetails | null = null;

  // Async-event toast
  toast = '';

  constructor(
    private patientService: PatientService,
    private doctorService: DoctorService
  ) {}

  ngOnInit(): void {
    this.load();
    // Load doctors so we can show/select the assigned doctor name.
    this.doctorService.getAll().subscribe({
      next: (d) => (this.doctors = d ?? []),
      error: () => (this.doctors = []),
    });
  }

  load(): void {
    this.loading = true;
    this.error = '';

    const hasFilter = this.filterStatus !== 'All' || this.filterBlood !== 'All';
    const source = hasFilter
      ? this.patientService.filter({
          statut: this.filterStatus !== 'All' ? this.filterStatus : undefined,
          groupeSanguin: this.filterBlood !== 'All' ? this.filterBlood : undefined,
        })
      : this.patientService.getAll();

    source.subscribe({
      next: (data) => {
        this.patients = data ?? [];   // 204 No Content -> null -> empty list
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load patients. Is the gateway running and are you logged in?';
        this.loading = false;
        console.error(err);
      },
    });
    this.loadStats();
  }

  /** Called when a backend filter dropdown changes. */
  applyFilters(): void {
    this.load();
  }

  loadStats(): void {
    this.patientService.getStats().subscribe({
      next: (s) => (this.stats = s),
      error: () => (this.stats = null),
    });
  }

  // ---------- Free-text search (client-side over the backend-filtered rows) ----------
  get filtered(): Patient[] {
    const q = this.search.toLowerCase();
    return this.patients.filter((p) =>
      `${p.nom} ${p.prenom} ${p.email}`.toLowerCase().includes(q)
    );
  }

  // ---------- Display helpers ----------
  fullName(p: Patient): string {
    return `${p.nom ?? ''} ${p.prenom ?? ''}`.trim();
  }

  initials(p: Patient): string {
    return `${(p.nom || '?')[0] ?? ''}${(p.prenom || '?')[0] ?? ''}`.toUpperCase();
  }

  age(p: Patient): string {
    if (!p.dateNaissance) return '—';
    const birth = new Date(p.dateNaissance);
    if (isNaN(birth.getTime())) return '—';
    const diff = Date.now() - birth.getTime();
    return String(Math.floor(diff / (1000 * 60 * 60 * 24 * 365.25)));
  }

  doctorName(id?: number | null): string {
    if (id == null) return 'Unassigned';
    const d = this.doctors.find((x) => x.id === id);
    return d ? `Dr. ${d.nom} ${d.prenom}` : 'Unassigned';
  }

  statusClass(status?: string): string {
    const map: Record<string, string> = {
      Active: 'bg-green-100 text-green-700',
      Admitted: 'bg-blue-100 text-blue-700',
      Discharged: 'bg-gray-100 text-gray-500',
    };
    return map[status ?? ''] ?? 'bg-gray-100 text-gray-500';
  }

  // ---------- Create / Edit ----------
  emptyForm(): Patient {
    return {
      nom: '', prenom: '', email: '', telephone: '',
      dateNaissance: '', genre: 'Male', groupeSanguin: 'A+',
      medecinId: null, statut: 'Active',
    };
  }

  openCreate(): void {
    this.editing = false;
    this.form = this.emptyForm();
    this.showModal = true;
  }

  openEdit(p: Patient): void {
    this.editing = true;
    this.form = { ...p };   // copy so we don't mutate the list until saved
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.saving = false;
  }

  save(): void {
    if (!this.form.nom || !this.form.prenom || !this.form.email) return;
    this.saving = true;

    const wasAdmitted = this.editing && this.form.statut === 'Admitted';
    const fail = (err: any) => { this.saving = false; this.error = 'Save failed.'; console.error(err); };

    if (this.editing && this.form.id != null) {
      this.patientService.update(this.form.id, this.form).subscribe({
        next: () => {
          this.saving = false; this.showModal = false; this.load();
          if (wasAdmitted) this.showToast('🔔 Admission event published to MSNotification (async).');
        },
        error: fail,
      });
    } else {
      this.patientService.create(this.form).subscribe({
        next: () => {
          this.saving = false; this.showModal = false; this.load();
          this.showToast('🔔 Patient-created event published to MSNotification (async).');
        },
        error: fail,
      });
    }
  }

  // ---------- Details (Feign) ----------
  openDetails(p: Patient): void {
    if (p.id == null) return;
    this.showDetails = true;
    this.detailsLoading = true;
    this.details = null;
    this.patientService.getDetails(p.id).subscribe({
      next: (d) => { this.details = d; this.detailsLoading = false; },
      error: (err) => { this.detailsLoading = false; this.error = 'Failed to load details.'; console.error(err); },
    });
  }

  closeDetails(): void {
    this.showDetails = false;
    this.details = null;
  }

  private showToast(msg: string): void {
    this.toast = msg;
    setTimeout(() => (this.toast = ''), 4000);
  }

  remove(p: Patient): void {
    if (p.id == null) return;
    if (!confirm(`Delete patient ${this.fullName(p)}?`)) return;
    this.patientService.delete(p.id).subscribe({
      next: () => this.load(),
      error: (err) => { this.error = 'Delete failed.'; console.error(err); },
    });
  }
}

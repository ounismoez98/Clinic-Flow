import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RendezVousService, RendezVous } from './rendezvous.service';

@Component({
  selector: 'app-appointments',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './appointments.component.html',
  styleUrl: './appointments.component.scss'
})
export class AppointmentsComponent implements OnInit {
  appointments: RendezVous[] = [];
  loading = false;
  error = '';

  // Search & filter
  search = '';
  filterDoctor = 'All';

  // Modal
  showModal = false;
  editing = false;
  saving = false;
  form: RendezVous = this.emptyForm();

  // Details drawer
  showDetails = false;
  selectedAppointment: RendezVous | null = null;

  // Delete confirmation
  showDeleteConfirm = false;
  deleteTarget: RendezVous | null = null;

  // Toast
  toast = '';

  // View mode
  viewMode: 'table' | 'cards' = 'table';

  constructor(private rdvService: RendezVousService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.rdvService.getAll().subscribe({
      next: (data) => {
        this.appointments = data ?? [];
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load appointments. Is the gateway running?';
        this.loading = false;
        console.error(err);
      },
    });
  }

  // ── Stats ─────────────────────────────────────────────────────────────────
  get totalCount(): number { return this.appointments.length; }
  get todayCount(): number {
    const today = new Date().toISOString().slice(0, 10);
    return this.appointments.filter(a => a.date === today).length;
  }
  get upcomingCount(): number {
    const today = new Date().toISOString().slice(0, 10);
    return this.appointments.filter(a => a.date > today).length;
  }
  get uniqueDoctors(): string[] {
    return [...new Set(this.appointments.map(a => a.medcin).filter(Boolean))];
  }

  // ── Filtering ─────────────────────────────────────────────────────────────
  get doctorOptions(): string[] {
    return ['All', ...this.uniqueDoctors];
  }

  get filtered(): RendezVous[] {
    const q = this.search.toLowerCase();
    return this.appointments.filter((a) => {
      const matchSearch = `${a.patient} ${a.medcin} ${a.cause} ${a.date}`.toLowerCase().includes(q);
      const matchDoctor = this.filterDoctor === 'All' || a.medcin === this.filterDoctor;
      return matchSearch && matchDoctor;
    });
  }

  // ── Display helpers ───────────────────────────────────────────────────────
  initials(name: string): string {
    if (!name) return '?';
    const parts = name.trim().split(/\s+/);
    return parts.map(p => p[0]).join('').toUpperCase().slice(0, 2);
  }

  dateStatus(date: string): 'past' | 'today' | 'upcoming' {
    const today = new Date().toISOString().slice(0, 10);
    if (date === today) return 'today';
    if (date < today) return 'past';
    return 'upcoming';
  }

  dateStatusClass(date: string): string {
    const s = this.dateStatus(date);
    const map: Record<string, string> = {
      today: 'bg-green-100 text-green-700',
      upcoming: 'bg-blue-100 text-blue-700',
      past: 'bg-gray-100 text-gray-500',
    };
    return map[s];
  }

  dateStatusLabel(date: string): string {
    const s = this.dateStatus(date);
    const map: Record<string, string> = {
      today: 'Today',
      upcoming: 'Upcoming',
      past: 'Past',
    };
    return map[s];
  }

  formatDate(date: string): string {
    if (!date) return '—';
    try {
      const d = new Date(date + 'T00:00:00');
      return d.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' });
    } catch {
      return date;
    }
  }

  // ── CRUD modal ────────────────────────────────────────────────────────────
  emptyForm(): RendezVous {
    return { date: '', cause: '', patient: '', medcin: '' };
  }

  openCreate(): void {
    this.editing = false;
    this.form = this.emptyForm();
    // Default date to today
    this.form.date = new Date().toISOString().slice(0, 10);
    this.showModal = true;
  }

  openEdit(a: RendezVous): void {
    this.editing = true;
    this.form = { ...a };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.saving = false;
  }

  save(): void {
    if (!this.form.patient || !this.form.medcin || !this.form.date || !this.form.cause) return;
    this.saving = true;

    if (this.editing && this.form.id != null) {
      this.rdvService.update(this.form.id, this.form).subscribe({
        next: () => {
          this.saving = false; this.showModal = false; this.load();
          this.showToast('✅ Appointment updated successfully.');
        },
        error: (err) => { this.saving = false; this.error = 'Update failed.'; console.error(err); },
      });
    } else {
      this.rdvService.create(this.form).subscribe({
        next: () => {
          this.saving = false; this.showModal = false; this.load();
          this.showToast('✅ Appointment created successfully.');
        },
        error: (err) => { this.saving = false; this.error = 'Create failed.'; console.error(err); },
      });
    }
  }

  // ── Details drawer ────────────────────────────────────────────────────────
  openDetails(a: RendezVous): void {
    this.selectedAppointment = a;
    this.showDetails = true;
  }

  closeDetails(): void {
    this.showDetails = false;
    this.selectedAppointment = null;
  }

  // ── Delete ────────────────────────────────────────────────────────────────
  confirmDelete(a: RendezVous): void {
    this.deleteTarget = a;
    this.showDeleteConfirm = true;
  }

  doDelete(): void {
    if (this.deleteTarget?.id == null) return;
    this.rdvService.delete(this.deleteTarget.id).subscribe({
      next: () => {
        this.showDeleteConfirm = false;
        this.deleteTarget = null;
        this.load();
        this.showToast('🗑️ Appointment deleted.');
      },
      error: (err) => {
        this.showDeleteConfirm = false;
        this.error = 'Delete failed.';
        console.error(err);
      },
    });
  }

  cancelDelete(): void {
    this.showDeleteConfirm = false;
    this.deleteTarget = null;
  }

  // ── Toast ─────────────────────────────────────────────────────────────────
  private showToast(msg: string): void {
    this.toast = msg;
    setTimeout(() => (this.toast = ''), 4000);
  }
}

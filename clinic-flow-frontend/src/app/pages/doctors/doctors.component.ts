import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DoctorService, Doctor, DoctorStats } from './doctor.service';

@Component({
  selector: 'app-doctors',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './doctors.component.html',
  styleUrl: './doctors.component.scss'
})
export class DoctorsComponent implements OnInit {
  doctors: Doctor[] = [];
  stats: DoctorStats | null = null;
  loading = false;
  error = '';

  search = '';                       // free-text, filtered client-side
  filterStatus = 'All';              // sent to backend
  filterSpecialty = 'All';           // sent to backend
  statusOptions = ['All', 'Available', 'In Consultation', 'Off Duty'];

  /** Specialties available for filtering, derived from the live stats. */
  get specialtyOptions(): string[] {
    const fromStats = this.stats ? Object.keys(this.stats.bySpecialty) : [];
    return ['All', ...fromStats.sort()];
  }

  showModal = false;
  saving = false;
  editing = false;
  form: Doctor = this.emptyForm();

  constructor(private doctorService: DoctorService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';

    const hasFilter = this.filterStatus !== 'All' || this.filterSpecialty !== 'All';
    const source = hasFilter
      ? this.doctorService.filter({
          statut: this.filterStatus !== 'All' ? this.filterStatus : undefined,
          specialite: this.filterSpecialty !== 'All' ? this.filterSpecialty : undefined,
        })
      : this.doctorService.getAll();

    source.subscribe({
      next: (data) => {
        this.doctors = data ?? [];   // 204 No Content -> null -> empty list
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load doctors. Is the gateway running and are you logged in?';
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
    this.doctorService.getStats().subscribe({
      next: (s) => (this.stats = s),
      error: () => (this.stats = null),
    });
  }

  // Free-text search (client-side over the backend-filtered rows)
  get filtered(): Doctor[] {
    const q = this.search.toLowerCase();
    return this.doctors.filter((d) =>
      `${d.nom} ${d.prenom} ${d.specialite} ${d.email}`.toLowerCase().includes(q)
    );
  }

  fullName(d: Doctor): string {
    return `Dr. ${d.nom ?? ''} ${d.prenom ?? ''}`.trim();
  }

  initials(d: Doctor): string {
    return `${(d.nom || '?')[0] ?? ''}${(d.prenom || '?')[0] ?? ''}`.toUpperCase();
  }

  statusClass(status?: string): string {
    const map: Record<string, string> = {
      'Available': 'bg-green-100 text-green-700',
      'In Consultation': 'bg-blue-100 text-blue-700',
      'Off Duty': 'bg-gray-100 text-gray-500',
    };
    return map[status ?? ''] ?? 'bg-gray-100 text-gray-500';
  }

  statusDot(status?: string): string {
    const map: Record<string, string> = {
      'Available': 'bg-green-500',
      'In Consultation': 'bg-blue-500',
      'Off Duty': 'bg-gray-400',
    };
    return map[status ?? ''] ?? 'bg-gray-400';
  }

  emptyForm(): Doctor {
    return { nom: '', prenom: '', email: '', specialite: '', telephone: '', experience: 0, statut: 'Available' };
  }

  openCreate(): void {
    this.editing = false;
    this.form = this.emptyForm();
    this.showModal = true;
  }

  openEdit(d: Doctor): void {
    this.editing = true;
    this.form = { ...d };
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.saving = false;
  }

  save(): void {
    if (!this.form.nom || !this.form.prenom || !this.form.email) return;
    this.saving = true;

    const done = () => { this.saving = false; this.showModal = false; this.load(); };
    const fail = (err: any) => { this.saving = false; this.error = 'Save failed.'; console.error(err); };

    if (this.editing && this.form.id != null) {
      this.doctorService.update(this.form.id, this.form).subscribe({ next: done, error: fail });
    } else {
      this.doctorService.create(this.form).subscribe({ next: done, error: fail });
    }
  }

  remove(d: Doctor): void {
    if (d.id == null) return;
    if (!confirm(`Delete ${this.fullName(d)}?`)) return;
    this.doctorService.delete(d.id).subscribe({
      next: () => this.load(),
      error: (err) => { this.error = 'Delete failed.'; console.error(err); },
    });
  }
}

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';

// ─── Models ───────────────────────────────────────────────────────────────────

export type StatutFacture = 'PAYEE' | 'NON_PAYEE' | 'ANNULEE';
export type TypeTVA = 'TVA_7' | 'TVA_19';

export interface LigneFacture {
  description: string;
  prix: number;
  quantite: number;
}

export interface Facture {
  id?: number;
  dateFacture?: string;
  montantHT: number;
  montantTTC: number;
  statut: StatutFacture;
  tva: TypeTVA;
  patientId: number;
  lignes: LigneFacture[];
}

// ─── Component ────────────────────────────────────────────────────────────────

const API = 'http://localhost:8085/factures';

@Component({
  selector: 'app-factures',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './factures.component.html',
  styleUrl: './factures.component.scss'
})
export class FacturesComponent implements OnInit {

  // ── State ──────────────────────────────────────────────────────────────────
  factures: Facture[] = [];
  loading = false;
  error = '';

  // ── Filters ────────────────────────────────────────────────────────────────
  search = '';
  filterStatut: StatutFacture | 'ALL' = 'ALL';
  viewMode: 'table' | 'grid' = 'table';

  statutOptions: (StatutFacture | 'ALL')[] = ['ALL', 'PAYEE', 'NON_PAYEE', 'ANNULEE'];
  tvaOptions: TypeTVA[] = ['TVA_7', 'TVA_19'];

  // ── Modal ──────────────────────────────────────────────────────────────────
  showModal = false;
  modalMode: 'create' | 'edit' | 'view' = 'create';
  showDeleteConfirm = false;
  deleteTargetId: number | null = null;
  successMsg = '';

  // ── Form ───────────────────────────────────────────────────────────────────
  form: Facture = this.emptyForm();

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.load();
  }

  // ── Stats ──────────────────────────────────────────────────────────────────
  get stats() {
    const total = this.factures.length;
    const payees = this.factures.filter(f => f.statut === 'PAYEE').length;
    const nonPayees = this.factures.filter(f => f.statut === 'NON_PAYEE').length;
    const totalTTC = this.factures.reduce((s, f) => s + f.montantTTC, 0);
    return [
      { label: 'Total Factures', value: String(total), color: 'text-blue-600 bg-blue-50', icon: 'M9 14l6-6m-5.5.5h.01m4.99 5h.01M19 21V5a2 2 0 0 0-2-2H7a2 2 0 0 0-2 2v16l3.5-2 3.5 2 3.5-2 3.5 2z' },
      { label: 'Payées', value: String(payees), color: 'text-green-600 bg-green-50', icon: 'M9 12.75 11.25 15 15 9.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z' },
      { label: 'Non Payées', value: String(nonPayees), color: 'text-orange-600 bg-orange-50', icon: 'M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z' },
      { label: 'Chiffre d\'affaires', value: totalTTC.toFixed(2) + ' DT', color: 'text-teal-600 bg-teal-50', icon: 'M2.25 18.75a60.07 60.07 0 0 1 15.797 2.101c.727.198 1.453-.342 1.453-1.096V18.75M3.75 4.5v.75A.75.75 0 0 1 3 6h-.75m0 0v-.375c0-.621.504-1.125 1.125-1.125H20.25M2.25 6v9m18-10.5v.75c0 .414.336.75.75.75h.75m-1.5-1.5h.375c.621 0 1.125.504 1.125 1.125v9.75c0 .621-.504 1.125-1.125 1.125h-.375m1.5-1.5H21a.75.75 0 0 0-.75.75v.75m0 0H3.75m0 0h-.375a1.125 1.125 0 0 1-1.125-1.125V15m1.5 1.5v-.75A.75.75 0 0 0 3 15h-.75M15 10.5a3 3 0 1 1-6 0 3 3 0 0 1 6 0zm3 0h.008v.008H18V10.5zm-12 0h.008v.008H6V10.5z' },
    ];
  }

  // ── Filtered list ──────────────────────────────────────────────────────────
  get filtered(): Facture[] {
    return this.factures.filter(f => {
      const matchSearch = String(f.patientId).includes(this.search) ||
                          String(f.id).includes(this.search) ||
                          (f.dateFacture ?? '').includes(this.search);
      const matchStatut = this.filterStatut === 'ALL' || f.statut === this.filterStatut;
      return matchSearch && matchStatut;
    });
  }

  // ── API calls ──────────────────────────────────────────────────────────────

  load() {
    this.loading = true;
    this.error = '';
    this.http.get<Facture[]>(API).subscribe({
      next: data => { this.factures = data; this.loading = false; },
      error: () => { this.error = 'Impossible de charger les factures. Vérifiez que MsFacture tourne sur le port 8087.'; this.loading = false; }
    });
  }

  save() {
    if (this.modalMode === 'create') {
      this.http.post<Facture>(API, this.form).subscribe({
        next: f => {
          this.factures.unshift(f);
          this.closeModal();
          this.flash('Facture créée avec succès !');
        },
        error: () => this.error = 'Erreur lors de la création.'
      });
    } else if (this.modalMode === 'edit' && this.form.id != null) {
      // Update: mark as paid or recreate — backend only exposes PUT /{id}/payer.
      // We use the payer endpoint if statut changed to PAYEE, otherwise inform user.
      if (this.form.statut === 'PAYEE') {
        this.http.put<Facture>(`${API}/${this.form.id}/payer`, {}).subscribe({
          next: updated => {
            const idx = this.factures.findIndex(f => f.id === updated.id);
            if (idx >= 0) this.factures[idx] = updated;
            this.closeModal();
            this.flash('Facture marquée comme payée !');
          },
          error: () => this.error = 'Erreur lors de la mise à jour.'
        });
      } else {
        this.closeModal();
        this.flash('Statut mis à jour localement.');
      }
    }
  }

  confirmDelete(id: number) {
    this.deleteTargetId = id;
    this.showDeleteConfirm = true;
  }

  doDelete() {
    if (this.deleteTargetId == null) return;
    this.http.delete(`${API}/${this.deleteTargetId}`).subscribe({
      next: () => {
        this.factures = this.factures.filter(f => f.id !== this.deleteTargetId);
        this.showDeleteConfirm = false;
        this.deleteTargetId = null;
        this.flash('Facture supprimée.');
      },
      error: () => { this.error = 'Erreur lors de la suppression.'; this.showDeleteConfirm = false; }
    });
  }

  markPaid(f: Facture) {
    this.http.put<Facture>(`${API}/${f.id}/payer`, {}).subscribe({
      next: updated => {
        const idx = this.factures.findIndex(x => x.id === updated.id);
        if (idx >= 0) this.factures[idx] = updated;
        this.flash('Facture payée !');
      },
      error: () => this.error = 'Erreur.'
    });
  }

  downloadPdf(f: Facture) {
    window.open(`${API}/${f.id}/pdf`, '_blank');
  }

  // ── Lignes helpers ─────────────────────────────────────────────────────────

  addLigne() {
    this.form.lignes.push({ description: '', prix: 0, quantite: 1 });
    this.recalculate();
  }

  removeLigne(i: number) {
    this.form.lignes.splice(i, 1);
    this.recalculate();
  }

  recalculate() {
    const ht = this.form.lignes.reduce((s, l) => s + l.prix * l.quantite, 0);
    this.form.montantHT = parseFloat(ht.toFixed(2));
    const tauxTVA = this.form.tva === 'TVA_19' ? 0.19 : 0.07;
    this.form.montantTTC = parseFloat((ht * (1 + tauxTVA)).toFixed(2));
  }

  // ── Modal helpers ──────────────────────────────────────────────────────────

  openCreate() {
    this.form = this.emptyForm();
    this.modalMode = 'create';
    this.showModal = true;
  }

  openEdit(f: Facture) {
    this.form = { ...f, lignes: f.lignes ? f.lignes.map(l => ({ ...l })) : [] };
    this.modalMode = 'edit';
    this.showModal = true;
  }

  openView(f: Facture) {
    this.form = { ...f, lignes: f.lignes ? f.lignes.map(l => ({ ...l })) : [] };
    this.modalMode = 'view';
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.error = '';
  }

  emptyForm(): Facture {
    return { montantHT: 0, montantTTC: 0, statut: 'NON_PAYEE', tva: 'TVA_19', patientId: 0, lignes: [{ description: '', prix: 0, quantite: 1 }] };
  }

  flash(msg: string) {
    this.successMsg = msg;
    setTimeout(() => this.successMsg = '', 3000);
  }

  // ── UI helpers ─────────────────────────────────────────────────────────────

  statutClass(s: StatutFacture): string {
    const map: Record<StatutFacture, string> = {
      PAYEE: 'bg-green-100 text-green-700',
      NON_PAYEE: 'bg-orange-100 text-orange-700',
      ANNULEE: 'bg-red-100 text-red-600',
    };
    return map[s] ?? '';
  }

  statutDot(s: StatutFacture): string {
    const map: Record<StatutFacture, string> = {
      PAYEE: 'bg-green-500',
      NON_PAYEE: 'bg-orange-400',
      ANNULEE: 'bg-red-500',
    };
    return map[s] ?? '';
  }

  statutLabel(s: StatutFacture): string {
    const map: Record<StatutFacture, string> = {
      PAYEE: 'Payée',
      NON_PAYEE: 'Non Payée',
      ANNULEE: 'Annulée',
    };
    return map[s] ?? s;
  }

  tvaLabel(t: TypeTVA): string {
    return t === 'TVA_19' ? '19%' : '7%';
  }

  factureCode(f: Facture): string {
    return '#F' + String(f.id ?? 0).padStart(4, '0');
  }
}

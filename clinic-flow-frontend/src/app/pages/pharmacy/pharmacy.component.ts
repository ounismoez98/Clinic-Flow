import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { LOW_STOCK_THRESHOLD, MEDICAMENTS_API, ORDONNANCES_API, PATIENTS_API } from '../../core/api.config';

// ─── Models ───────────────────────────────────────────────────────────────────

interface Medicament {
  id: number;
  nomMedicament: string;
  etat: boolean;
}

interface StockInfo {
  medicamentId: number;
  stockQuantity: number;
  prixUnitaire: number;
}

interface MedicamentRow extends Medicament {
  stockQuantity: number;
  prixUnitaire: number;
}

interface PatientOption {
  id: number;
  nom: string;
  prenom: string;
  email?: string;
}

interface OrdonnanceOption {
  id: number;
  nom: string;
  prenom: string;
  email?: string;
  medicamentsIds?: number[];
}

interface AssistantSummary {
  medicamentId: number;
  nomMedicament: string;
  summary: string;
  disclaimer: string;
}

interface CreateMedicamentForm {
  nomMedicament: string;
  etat: boolean;
  stockQuantity: number;
  prixUnitaire: number | null;
}

interface UpdateMedicamentForm {
  nomMedicament: string;
  etat: boolean;
  prixUnitaire: number | null;
}

type EtatFilter = 'ALL' | 'ACTIVE' | 'INACTIVE';
type ModalMode = 'create' | 'edit' | 'stock' | 'dispense' | 'detail';

// ─── Component ────────────────────────────────────────────────────────────────

@Component({
  selector: 'app-pharmacy',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './pharmacy.component.html',
  styleUrl: './pharmacy.component.scss'
})
export class PharmacyComponent implements OnInit {

  medicaments: MedicamentRow[] = [];
  patients: PatientOption[] = [];
  loading = false;
  error = '';
  successMsg = '';

  search = '';
  filterEtat: EtatFilter = 'ALL';
  etatOptions: EtatFilter[] = ['ALL', 'ACTIVE', 'INACTIVE'];

  showModal = false;
  modalMode: ModalMode = 'create';
  selectedRow: MedicamentRow | null = null;

  createForm: CreateMedicamentForm = this.emptyCreateForm();
  editForm: UpdateMedicamentForm = this.emptyEditForm();
  stockQuantity = 0;
  dispensePatientId: number | null = null;
  dispenseQuantity = 1;

  assistantSummary: AssistantSummary | null = null;
  assistantLoading = false;
  assistantError = '';
  patientsLoading = false;
  patientsError = '';

  showDeleteConfirm = false;
  deleteTargetId: number | null = null;
  deleteTargetName = '';

  ordonnances: OrdonnanceOption[] = [];
  ordonnancesLoading = false;
  ordonnancesError = '';
  mqOrdonnanceId: number | null = null;
  mqMedicamentId: number | null = null;
  mqLoading = false;
  mqError = '';
  mqInfo = '';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.load();
    this.loadOrdonnances();
  }

  // ── Stats ──────────────────────────────────────────────────────────────────

  get stats() {
    const total = this.medicaments.length;
    const actifs = this.medicaments.filter(m => m.etat).length;
    const lowStock = this.medicaments.filter(m => m.stockQuantity <= LOW_STOCK_THRESHOLD).length;
    const valeur = this.medicaments.reduce((s, m) => s + m.stockQuantity * (m.prixUnitaire ?? 0), 0);
    return [
      { label: 'Total médicaments', value: String(total), color: 'text-green-600 bg-green-50', icon: 'M19 7H5a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2zM16 3H8l-1 4h10l-1-4z' },
      { label: 'Actifs', value: String(actifs), color: 'text-blue-600 bg-blue-50', icon: 'M9 12.75 11.25 15 15 9.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z' },
      { label: 'Stock faible', value: String(lowStock), color: 'text-orange-600 bg-orange-50', icon: 'M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z' },
      { label: 'Valeur stock', value: valeur.toFixed(2) + ' DT', color: 'text-teal-600 bg-teal-50', icon: 'M2.25 18.75a60.07 60.07 0 0 1 15.797 2.101c.727.198 1.453-.342 1.453-1.096V18.75M3.75 4.5v.75A.75.75 0 0 1 3 6h-.75m0 0v-.375c0-.621.504-1.125 1.125-1.125H20.25M2.25 6v9m18-10.5v.75c0 .414.336.75.75.75h.75m-1.5-1.5h.375c.621 0 1.125.504 1.125 1.125v9.75c0 .621-.504 1.125-1.125 1.125h-.375m1.5-1.5H21a.75.75 0 0 0-.75.75v.75m0 0H3.75m0 0h-.375a1.125 1.125 0 0 1-1.125-1.125V15m1.5 1.5v-.75A.75.75 0 0 0 3 15h-.75M15 10.5a3 3 0 1 1-6 0 3 3 0 0 1 6 0zm3 0h.008v.008H18V10.5zm-12 0h.008v.008H6V10.5z' },
    ];
  }

  get filtered(): MedicamentRow[] {
    return this.medicaments.filter(m => {
      const q = this.search.trim().toLowerCase();
      const matchSearch = !q || m.nomMedicament.toLowerCase().includes(q) || String(m.id).includes(q);
      const matchEtat =
        this.filterEtat === 'ALL' ||
        (this.filterEtat === 'ACTIVE' && m.etat) ||
        (this.filterEtat === 'INACTIVE' && !m.etat);
      return matchSearch && matchEtat;
    });
  }

  // ── API ────────────────────────────────────────────────────────────────────

  load() {
    this.loading = true;
    this.error = '';

    this.http.get<Medicament[]>(MEDICAMENTS_API, { observe: 'response' }).subscribe({
      next: (resp: HttpResponse<Medicament[]>) => {
        const catalog = resp.status === 204 || !resp.body ? [] : resp.body;
        if (catalog.length === 0) {
          this.medicaments = [];
          this.loading = false;
          return;
        }
        this.loadStockForCatalog(catalog);
      },
      error: (err: HttpErrorResponse) => {
        this.error = this.extractError(err, 'Impossible de charger le catalogue médicaments.');
        this.loading = false;
      }
    });
  }

  private loadStockForCatalog(catalog: Medicament[]) {
    const stockRequests = catalog.map(m =>
      this.http.get<StockInfo>(`${MEDICAMENTS_API}/${m.id}/stock-info`).pipe(
        catchError(() => of({ medicamentId: m.id, stockQuantity: 0, prixUnitaire: 0 } as StockInfo))
      )
    );

    forkJoin(stockRequests).subscribe({
      next: stocks => {
        this.medicaments = catalog.map((m, i) => ({
          ...m,
          stockQuantity: stocks[i]?.stockQuantity ?? 0,
          prixUnitaire: Number(stocks[i]?.prixUnitaire ?? 0),
        }));
        this.loading = false;
      },
      error: () => {
        this.medicaments = catalog.map(m => ({ ...m, stockQuantity: 0, prixUnitaire: 0 }));
        this.error = 'Catalogue chargé mais certaines informations de stock sont indisponibles.';
        this.loading = false;
      }
    });
  }

  private refreshStockForRow(id: number) {
    this.http.get<StockInfo>(`${MEDICAMENTS_API}/${id}/stock-info`).subscribe({
      next: stock => {
        const idx = this.medicaments.findIndex(m => m.id === id);
        if (idx >= 0) {
          this.medicaments[idx] = {
            ...this.medicaments[idx],
            stockQuantity: stock.stockQuantity,
            prixUnitaire: Number(stock.prixUnitaire ?? 0),
          };
        }
      }
    });
  }

  loadOrdonnances() {
    this.ordonnancesLoading = true;
    this.ordonnancesError = '';
    this.http.get<OrdonnanceOption[]>(ORDONNANCES_API, { observe: 'response' }).subscribe({
      next: (resp: HttpResponse<OrdonnanceOption[]>) => {
        this.ordonnances = resp.status === 204 || !resp.body ? [] : resp.body;
        this.ordonnancesLoading = false;
        if (this.ordonnances.length === 0) {
          this.ordonnancesError = 'Aucune ordonnance. Démarrez MSOrdonnance.';
        } else if (this.mqOrdonnanceId == null) {
          this.mqOrdonnanceId = this.ordonnances[0].id;
        }
      },
      error: (err: HttpErrorResponse) => {
        this.ordonnancesError = this.extractError(err, 'Impossible de charger les ordonnances.');
        this.ordonnancesLoading = false;
      }
    });
  }

  triggerOrdonnanceMq() {
    if (this.mqOrdonnanceId == null || this.mqMedicamentId == null) {
      this.mqError = 'Sélectionnez une ordonnance et un médicament.';
      return;
    }
    const medicamentId = this.mqMedicamentId;
    const stockBefore = this.medicaments.find(m => m.id === medicamentId)?.stockQuantity;

    this.mqLoading = true;
    this.mqError = '';
    this.mqInfo = '';

    this.http.post(
      `${ORDONNANCES_API}/${this.mqOrdonnanceId}/medicaments/${medicamentId}`,
      {},
      { responseType: 'text' }
    ).subscribe({
      next: (msg: string) => {
        this.mqLoading = false;
        this.mqInfo =
          `${msg.trim()} MSOrdonnance a publié sur RabbitMQ (clinic.pharmacy.exchange → ordonnance.medicament.added). ` +
          'MSPharmacie consomme la file et décrémente le stock de façon asynchrone.';
        this.flash('Ordonnance envoyée — mise à jour stock via RabbitMQ…');
        setTimeout(() => {
          this.http.get<StockInfo>(`${MEDICAMENTS_API}/${medicamentId}/stock-info`).subscribe({
            next: stock => {
              const idx = this.medicaments.findIndex(m => m.id === medicamentId);
              if (idx >= 0) {
                this.medicaments[idx] = {
                  ...this.medicaments[idx],
                  stockQuantity: stock.stockQuantity,
                  prixUnitaire: Number(stock.prixUnitaire ?? 0),
                };
              }
              const stockAfter = stock.stockQuantity;
              if (stockBefore != null && stockAfter < stockBefore) {
                this.mqInfo += ` Stock : ${stockBefore} → ${stockAfter}.`;
              } else if (stockBefore != null && stockAfter === stockBefore) {
                this.mqInfo += ` Stock inchangé (${stockAfter}) — stock insuffisant ou MSPharmacie indisponible.`;
              }
            }
          });
        }, 800);
      },
      error: (err: HttpErrorResponse) => {
        this.mqLoading = false;
        this.mqError = this.extractError(err, 'Erreur lors de l\'ajout à l\'ordonnance.');
      }
    });
  }

  loadPatients() {
    this.patientsLoading = true;
    this.patientsError = '';
    this.http.get<PatientOption[]>(PATIENTS_API, { observe: 'response' }).subscribe({
      next: (resp: HttpResponse<PatientOption[]>) => {
        this.patients = resp.status === 204 || !resp.body ? [] : resp.body;
        this.patientsLoading = false;
        if (this.patients.length === 0) {
          this.patientsError = 'Aucun patient trouvé. Démarrez MSPatientMedcin.';
        }
      },
      error: (err: HttpErrorResponse) => {
        this.patientsError = this.extractError(err, 'Impossible de charger les patients.');
        this.patientsLoading = false;
      }
    });
  }

  saveCreate() {
    if (!this.createForm.nomMedicament.trim()) {
      this.error = 'Le nom du médicament est obligatoire.';
      return;
    }
    const body = {
      nomMedicament: this.createForm.nomMedicament.trim(),
      etat: this.createForm.etat,
      stockQuantity: this.createForm.stockQuantity,
      prixUnitaire: this.createForm.prixUnitaire ?? undefined,
    };
    this.http.post<Medicament>(MEDICAMENTS_API, body).subscribe({
      next: created => {
        this.http.get<StockInfo>(`${MEDICAMENTS_API}/${created.id}/stock-info`).subscribe({
          next: stock => {
            this.medicaments.unshift({
              ...created,
              stockQuantity: stock.stockQuantity,
              prixUnitaire: Number(stock.prixUnitaire ?? 0),
            });
            this.closeModal();
            this.flash('Médicament créé avec succès !');
          },
          error: () => {
            this.medicaments.unshift({ ...created, stockQuantity: body.stockQuantity, prixUnitaire: body.prixUnitaire ?? 0 });
            this.closeModal();
            this.flash('Médicament créé (stock partiel).');
          }
        });
      },
      error: (err: HttpErrorResponse) => {
        this.error = this.extractError(err, 'Erreur lors de la création.');
      }
    });
  }

  saveEdit() {
    if (!this.selectedRow || !this.editForm.nomMedicament.trim()) {
      this.error = 'Le nom du médicament est obligatoire.';
      return;
    }
    const body = {
      nomMedicament: this.editForm.nomMedicament.trim(),
      etat: this.editForm.etat,
      prixUnitaire: this.editForm.prixUnitaire ?? undefined,
    };
    this.http.put<Medicament>(`${MEDICAMENTS_API}/${this.selectedRow.id}`, body).subscribe({
      next: updated => {
        const idx = this.medicaments.findIndex(m => m.id === updated.id);
        if (idx >= 0) {
          this.medicaments[idx] = {
            ...this.medicaments[idx],
            nomMedicament: updated.nomMedicament,
            etat: updated.etat,
            prixUnitaire: this.editForm.prixUnitaire ?? this.medicaments[idx].prixUnitaire,
          };
        }
        this.closeModal();
        this.flash('Médicament mis à jour !');
      },
      error: (err: HttpErrorResponse) => {
        this.error = this.extractError(err, 'Erreur lors de la mise à jour.');
      }
    });
  }

  saveStock() {
    if (!this.selectedRow) return;
    this.http.patch<Medicament>(`${MEDICAMENTS_API}/${this.selectedRow.id}/stock`, { quantity: this.stockQuantity }).subscribe({
      next: () => {
        this.refreshStockForRow(this.selectedRow!.id);
        this.closeModal();
        this.flash('Stock mis à jour !');
      },
      error: (err: HttpErrorResponse) => {
        this.error = this.extractError(err, 'Erreur lors de la mise à jour du stock.');
      }
    });
  }

  saveDispense() {
    if (!this.selectedRow || this.dispensePatientId == null || this.dispenseQuantity < 1) {
      this.error = 'Sélectionnez un patient et une quantité valide.';
      return;
    }
    this.http.post<Medicament>(`${MEDICAMENTS_API}/${this.selectedRow.id}/dispense`, {
      patientId: this.dispensePatientId,
      quantity: this.dispenseQuantity,
    }).subscribe({
      next: () => {
        this.refreshStockForRow(this.selectedRow!.id);
        this.closeModal();
        this.flash('Médicament délivré avec succès !');
      },
      error: (err: HttpErrorResponse) => {
        this.error = this.extractError(err, 'Erreur lors de la délivrance.');
      }
    });
  }

  confirmDelete(row: MedicamentRow) {
    this.deleteTargetId = row.id;
    this.deleteTargetName = row.nomMedicament;
    this.showDeleteConfirm = true;
  }

  doDelete() {
    if (this.deleteTargetId == null) return;
    this.http.delete(`${MEDICAMENTS_API}/${this.deleteTargetId}`).subscribe({
      next: () => {
        this.medicaments = this.medicaments.filter(m => m.id !== this.deleteTargetId);
        this.showDeleteConfirm = false;
        this.deleteTargetId = null;
        this.deleteTargetName = '';
        this.flash('Médicament supprimé.');
      },
      error: (err: HttpErrorResponse) => {
        this.error = this.extractError(err, 'Erreur lors de la suppression.');
        this.showDeleteConfirm = false;
      }
    });
  }

  loadAssistantSummary() {
    if (!this.selectedRow) return;
    this.assistantLoading = true;
    this.assistantError = '';
    this.assistantSummary = null;

    this.http.get<AssistantSummary>(`${MEDICAMENTS_API}/${this.selectedRow.id}/assistant-summary`).subscribe({
      next: summary => {
        this.assistantSummary = summary;
        this.assistantLoading = false;
      },
      error: (err: HttpErrorResponse) => {
        this.assistantLoading = false;
        if (err.status === 503) {
          this.assistantError = 'Assistant IA non configuré. Définissez GEMINI_API_KEY sur MSPharmacie.';
        } else if (err.status === 502) {
          this.assistantError = this.extractError(err, 'Service Gemini temporairement indisponible.');
        } else {
          this.assistantError = this.extractError(err, 'Impossible de charger le résumé assistant.');
        }
      }
    });
  }

  // ── Modal helpers ──────────────────────────────────────────────────────────

  openCreate() {
    this.createForm = this.emptyCreateForm();
    this.modalMode = 'create';
    this.selectedRow = null;
    this.error = '';
    this.showModal = true;
  }

  openEdit(row: MedicamentRow) {
    this.selectedRow = row;
    this.editForm = {
      nomMedicament: row.nomMedicament,
      etat: row.etat,
      prixUnitaire: row.prixUnitaire,
    };
    this.modalMode = 'edit';
    this.error = '';
    this.showModal = true;
  }

  openStock(row: MedicamentRow) {
    this.selectedRow = row;
    this.stockQuantity = row.stockQuantity;
    this.modalMode = 'stock';
    this.error = '';
    this.showModal = true;
  }

  openDispense(row: MedicamentRow) {
    this.selectedRow = row;
    this.dispensePatientId = null;
    this.dispenseQuantity = 1;
    this.modalMode = 'dispense';
    this.error = '';
    this.patients = [];
    this.showModal = true;
    this.loadPatients();
  }

  openDetail(row: MedicamentRow) {
    this.selectedRow = row;
    this.modalMode = 'detail';
    this.assistantSummary = null;
    this.assistantError = '';
    this.assistantLoading = false;
    this.error = '';
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.selectedRow = null;
    this.error = '';
    this.assistantSummary = null;
    this.assistantError = '';
  }

  emptyCreateForm(): CreateMedicamentForm {
    return { nomMedicament: '', etat: true, stockQuantity: 0, prixUnitaire: null };
  }

  emptyEditForm(): UpdateMedicamentForm {
    return { nomMedicament: '', etat: true, prixUnitaire: null };
  }

  flash(msg: string) {
    this.successMsg = msg;
    setTimeout(() => (this.successMsg = ''), 3000);
  }

  // ── UI helpers ─────────────────────────────────────────────────────────────

  etatLabel(filter: EtatFilter): string {
    const map: Record<EtatFilter, string> = {
      ALL: 'Tous',
      ACTIVE: 'Actifs',
      INACTIVE: 'Inactifs',
    };
    return map[filter];
  }

  etatClass(etat: boolean): string {
    return etat ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600';
  }

  etatBadge(etat: boolean): string {
    return etat ? 'Actif' : 'Inactif';
  }

  stockClass(qty: number): string {
    if (qty <= LOW_STOCK_THRESHOLD) return 'bg-red-100 text-red-700';
    if (qty <= LOW_STOCK_THRESHOLD * 2) return 'bg-orange-100 text-orange-700';
    return 'bg-green-100 text-green-700';
  }

  modalTitle(): string {
    const map: Record<ModalMode, string> = {
      create: 'Nouveau médicament',
      edit: 'Modifier le médicament',
      stock: 'Ajuster le stock',
      dispense: 'Délivrer le médicament',
      detail: 'Détail du médicament',
    };
    return map[this.modalMode];
  }

  patientLabel(p: PatientOption): string {
    return `#${p.id} — ${p.prenom} ${p.nom}`;
  }

  ordonnanceLabel(o: OrdonnanceOption): string {
    return `#${o.id} — ${o.prenom} ${o.nom}`;
  }

  medicamentLabel(m: MedicamentRow): string {
    return `#${m.id} — ${m.nomMedicament} (stock: ${m.stockQuantity})`;
  }

  private extractError(err: HttpErrorResponse, fallback: string): string {
    if (typeof err.error === 'string' && err.error.trim()) {
      return err.error;
    }
    if (err.error?.message) {
      return err.error.message;
    }
    if (err.status === 404) return 'Ressource introuvable.';
    if (err.status === 400) return 'Requête invalide.';
    if (err.status === 502) return 'Service en amont indisponible.';
    if (err.status === 503) return 'Service temporairement indisponible.';
    return fallback;
  }
}

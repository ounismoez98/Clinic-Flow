import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { IdentityService, IamUser, IamRole, NewIamUser } from './identity.service';

@Component({
  selector: 'app-iam',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './iam.component.html',
  styleUrl: './iam.component.scss',
})
export class IamComponent implements OnInit {
  users: IamUser[] = [];
  roles: IamRole[] = [];
  loading = false;
  error = '';
  search = '';

  showModal = false;
  saving = false;
  form: NewIamUser = this.emptyForm();

  // Roles we let an admin assign (the app roles, not Keycloak's built-ins).
  assignableRoles = ['ADMIN', 'MEDECIN', 'PATIENT'];

  constructor(private identity: IdentityService) {}

  ngOnInit(): void {
    this.load();
    this.identity.listRoles().subscribe({
      next: (r) => (this.roles = r),
      error: () => (this.roles = []),
    });
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.identity.listUsers(this.search).subscribe({
      next: (u) => { this.users = u ?? []; this.loading = false; },
      error: (err) => {
        this.loading = false;
        this.error = err.status === 403
          ? 'You need the ADMIN role to manage identities.'
          : 'Failed to reach the identity API (is it running on :5000?).';
        console.error(err);
      },
    });
  }

  emptyForm(): NewIamUser {
    return { username: '', email: '', firstName: '', lastName: '', password: 'changeme123', role: 'PATIENT' };
  }

  openCreate(): void {
    this.form = this.emptyForm();
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
    this.saving = false;
  }

  save(): void {
    if (!this.form.username || !this.form.email) return;
    this.saving = true;
    this.identity.createUser(this.form).subscribe({
      next: () => { this.saving = false; this.showModal = false; this.load(); },
      error: (err) => { this.saving = false; this.error = 'Create failed.'; console.error(err); },
    });
  }

  remove(u: IamUser): void {
    if (!confirm(`Delete user ${u.username}?`)) return;
    this.identity.deleteUser(u.id).subscribe({
      next: () => this.load(),
      error: (err) => { this.error = 'Delete failed.'; console.error(err); },
    });
  }

  initials(u: IamUser): string {
    const a = (u.firstName || u.username || '?')[0] ?? '';
    const b = (u.lastName || u.username?.[1] || '')[0] ?? '';
    return (a + b).toUpperCase();
  }
}

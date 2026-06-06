import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  email = '';
  password = '';
  showPassword = false;
  loading = false;
  activeRole = 'admin';

  roles = [
    { key: 'admin', label: 'Admin' },
    { key: 'doctor', label: 'Doctor' },
    { key: 'staff', label: 'Staff' },
  ];

  constructor(private router: Router, private keycloak: KeycloakService) {}

  /**
   * Redirect to the Keycloak login page. After a successful login Keycloak
   * sends the user back to /dashboard/patients with a valid token in place.
   */
  onSubmit() {
    this.loading = true;
    this.keycloak.login({
      redirectUri: window.location.origin + '/dashboard/patients',
    });
  }

  logout() {
    this.keycloak.logout(window.location.origin + '/login');
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }
}

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit {
  mode: 'signin' | 'signup' = 'signin';
  username = '';
  email = '';
  password = '';
  showPassword = false;
  loading = false;
  activeRole = 'PATIENT';

  roles = [
    { key: 'PATIENT', label: 'Patient' },
    { key: 'MEDECIN', label: 'Doctor' },
    { key: 'ADMIN', label: 'Admin' },
  ];

  constructor(
    private router: Router, 
    private route: ActivatedRoute,
    private keycloak: KeycloakService,
    private http: HttpClient
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['mode'] === 'signup') {
        this.mode = 'signup';
      } else {
        this.mode = 'signin';
      }
    });
  }

  toggleMode() {
    this.mode = this.mode === 'signin' ? 'signup' : 'signin';
    this.email = '';
    this.password = '';
    this.username = '';
  }

  onSubmit() {
    this.loading = true;
    
    if (this.mode === 'signin') {
      this.keycloak.login({
        redirectUri: window.location.origin + '/dashboard/patients',
      });
    } else {
      const payload = {
        username: this.username,
        email: this.email,
        password: this.password,
        role: this.activeRole
      };
      
      this.http.post('http://localhost:8083/users', payload).subscribe({
        next: () => {
          this.loading = false;
          alert('Account created successfully! You can now sign in.');
          this.toggleMode();
        },
        error: (err) => {
          this.loading = false;
          alert('Failed to create account. Check console for details.');
          console.error('Registration error:', err);
        }
      });
    }
  }

  logout() {
    this.keycloak.logout(window.location.origin + '/login');
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }
}

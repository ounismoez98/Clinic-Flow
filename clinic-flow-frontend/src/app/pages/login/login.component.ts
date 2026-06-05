import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

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

  constructor(private router: Router) {}

  onSubmit() {
    this.loading = true;
    setTimeout(() => {
      this.loading = false;
      this.router.navigate(['/dashboard/patients']);
    }, 1200);
  }

  togglePassword() {
    this.showPassword = !this.showPassword;
  }
}

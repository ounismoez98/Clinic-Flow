import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  currentYear = new Date().getFullYear();
  menuOpen = false;

  navLinks = ['Dashboard', 'Patients', 'Appointments', 'Pharmacy', 'Laboratory', 'Billing'];

  stats = [
    { label: 'Patients', value: '12,480', icon: 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z', color: 'bg-blue-100 text-blue-600' },
    { label: 'Doctors', value: '284', icon: 'M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z', color: 'bg-teal-100 text-teal-600' },
    { label: 'Appointments Today', value: '138', icon: 'M8 2v4M16 2v4M3 10h18M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z', color: 'bg-purple-100 text-purple-600' },
    { label: 'Prescriptions', value: '3,920', icon: 'M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2M9 5a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2M9 5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2', color: 'bg-orange-100 text-orange-600' },
  ];

  services = [
    { title: 'Patient Management', description: 'Register, search and manage patient records with full medical history.', icon: 'M16 7a4 4 0 1 1-8 0 4 4 0 0 1 8 0zM12 14a7 7 0 0 0-7 7h14a7 7 0 0 0-7-7z', color: 'from-blue-500 to-blue-700', badge: 'MS Patient' },
    { title: 'Appointments', description: 'Schedule, confirm and track appointments between patients and doctors.', icon: 'M8 2v4M16 2v4M3 10h18M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z', color: 'from-teal-500 to-teal-700', badge: 'MS Rendez-Vous' },
    { title: 'Prescriptions', description: 'Issue and manage digital prescriptions linked to patient visits.', icon: 'M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2M9 5a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2', color: 'from-purple-500 to-purple-700', badge: 'MS Ordonnance' },
    { title: 'Pharmacy', description: 'Manage drug inventory and dispense medications against prescriptions.', icon: 'M19 7H5a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2zM16 3H8l-1 4h10l-1-4z', color: 'from-green-500 to-green-700', badge: 'MS Pharmacie' },
    { title: 'Laboratory', description: 'Request and receive lab test results integrated with patient files.', icon: 'M9 3H5a2 2 0 0 0-2 2v4m6-6h10a2 2 0 0 1 2 2v4M9 3v18m0 0h10a2 2 0 0 0 2-2V9M9 21H5a2 2 0 0 0-2-2V9m0 0h18', color: 'from-yellow-500 to-orange-500', badge: 'MS Laboratoire' },
    { title: 'Billing & Invoices', description: 'Generate, track and export patient invoices across all services.', icon: 'M9 14l6-6m-5.5.5h.01m4.99 5h.01M19 21V5a2 2 0 0 0-2-2H7a2 2 0 0 0-2 2v16l3.5-2 3.5 2 3.5-2 3.5 2z', color: 'from-rose-500 to-rose-700', badge: 'MS Facture' },
  ];

  toggleMenu() { this.menuOpen = !this.menuOpen; }
}

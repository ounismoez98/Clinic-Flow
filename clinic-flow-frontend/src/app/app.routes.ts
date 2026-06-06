import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { LoginComponent } from './pages/login/login.component';
import { ShellComponent } from './layout/shell/shell.component';
import { PatientsComponent } from './pages/patients/patients.component';
import { DoctorsComponent } from './pages/doctors/doctors.component';
import { FacturesComponent } from './pages/factures/factures.component';
import { PharmacyComponent } from './pages/pharmacy/pharmacy.component';
import { AppointmentsComponent } from './pages/appointments/appointments.component';
import { authGuard } from './auth/auth.guard';
import { ConsultationsComponent } from './pages/consultations/consultations.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  {
    path: 'dashboard',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'patients', pathMatch: 'full' },
      { path: 'patients', component: PatientsComponent },
      { path: 'doctors', component: DoctorsComponent },
      { path: 'factures', component: FacturesComponent },
      { path: 'consultations', component: ConsultationsComponent },
      { path: 'appointments', component: AppointmentsComponent },
    ]
  },
  { path: '**', redirectTo: '' }
];

import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { ShellComponent } from './layout/shell/shell.component';
import { PatientsComponent } from './pages/patients/patients.component';
import { DoctorsComponent } from './pages/doctors/doctors.component';
import { FacturesComponent } from './pages/factures/factures.component';
import { PharmacyComponent } from './pages/pharmacy/pharmacy.component';
import { AppointmentsComponent } from './pages/appointments/appointments.component';
import { IamComponent } from './pages/iam/iam.component';
import { authGuard } from './auth/auth.guard';
import { ConsultationsComponent } from './pages/consultations/consultations.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  // No custom /login page — login goes straight to Keycloak (see authGuard
  // and HomeComponent.login()). /login redirects home for any old links.
  { path: 'login', redirectTo: '', pathMatch: 'full' },
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
      { path: 'pharmacy', component: PharmacyComponent },
      { path: 'iam', component: IamComponent },
    ]
  },
  { path: '**', redirectTo: '' }
];

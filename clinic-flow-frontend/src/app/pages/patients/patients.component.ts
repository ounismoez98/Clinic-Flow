import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface Patient {
  id: number;
  name: string;
  age: number;
  gender: 'Male' | 'Female';
  phone: string;
  email: string;
  bloodType: string;
  doctor: string;
  lastVisit: string;
  status: 'Active' | 'Admitted' | 'Discharged';
  avatar: string;
}

@Component({
  selector: 'app-patients',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './patients.component.html',
  styleUrl: './patients.component.scss'
})
export class PatientsComponent {
  search = '';
  filterStatus = 'All';
  showModal = false;
  selectedPatient: Patient | null = null;

  statusOptions = ['All', 'Active', 'Admitted', 'Discharged'];

  stats = [
    { label: 'Total Patients', value: '12,480', delta: '+8.2%', positive: true, icon: 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z', color: 'text-blue-600 bg-blue-50' },
    { label: 'Admitted Today', value: '48', delta: '+3', positive: true, icon: 'M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126zM12 15.75h.007v.008H12v-.008z', color: 'text-orange-600 bg-orange-50' },
    { label: 'Discharged Today', value: '31', delta: '-2', positive: false, icon: 'M15.75 9V5.25A2.25 2.25 0 0 0 13.5 3h-6a2.25 2.25 0 0 0-2.25 2.25v13.5A2.25 2.25 0 0 0 7.5 21h6a2.25 2.25 0 0 0 2.25-2.25V15M12 9l-3 3m0 0 3 3m-3-3h12.75', color: 'text-teal-600 bg-teal-50' },
    { label: 'New This Month', value: '204', delta: '+12%', positive: true, icon: 'M12 4.5v15m7.5-7.5h-15', color: 'text-purple-600 bg-purple-50' },
  ];

  patients: Patient[] = [
    { id: 1, name: 'Ahmed Ben Salem', age: 45, gender: 'Male', phone: '+216 22 111 222', email: 'ahmed@email.com', bloodType: 'A+', doctor: 'Dr. Karray', lastVisit: '2026-06-03', status: 'Active', avatar: 'AB' },
    { id: 2, name: 'Fatma Trabelsi', age: 32, gender: 'Female', phone: '+216 55 333 444', email: 'fatma@email.com', bloodType: 'O+', doctor: 'Dr. Mansouri', lastVisit: '2026-06-04', status: 'Admitted', avatar: 'FT' },
    { id: 3, name: 'Mohamed Karray', age: 58, gender: 'Male', phone: '+216 98 555 666', email: 'med@email.com', bloodType: 'B-', doctor: 'Dr. Belhaj', lastVisit: '2026-06-01', status: 'Active', avatar: 'MK' },
    { id: 4, name: 'Sonia Bel Haj', age: 27, gender: 'Female', phone: '+216 20 777 888', email: 'sonia@email.com', bloodType: 'AB+', doctor: 'Dr. Karray', lastVisit: '2026-05-30', status: 'Discharged', avatar: 'SB' },
    { id: 5, name: 'Yassine Chahed', age: 39, gender: 'Male', phone: '+216 50 999 000', email: 'yassine@email.com', bloodType: 'O-', doctor: 'Dr. Mansouri', lastVisit: '2026-06-02', status: 'Active', avatar: 'YC' },
    { id: 6, name: 'Leila Hammami', age: 51, gender: 'Female', phone: '+216 23 112 233', email: 'leila@email.com', bloodType: 'A-', doctor: 'Dr. Belhaj', lastVisit: '2026-06-04', status: 'Admitted', avatar: 'LH' },
    { id: 7, name: 'Karim Jaziri', age: 63, gender: 'Male', phone: '+216 44 334 445', email: 'karim@email.com', bloodType: 'B+', doctor: 'Dr. Karray', lastVisit: '2026-05-28', status: 'Discharged', avatar: 'KJ' },
    { id: 8, name: 'Nadia Saidi', age: 35, gender: 'Female', phone: '+216 55 556 677', email: 'nadia@email.com', bloodType: 'O+', doctor: 'Dr. Mansouri', lastVisit: '2026-06-03', status: 'Active', avatar: 'NS' },
  ];

  get filtered(): Patient[] {
    return this.patients.filter(p => {
      const matchesSearch = p.name.toLowerCase().includes(this.search.toLowerCase()) ||
                            p.doctor.toLowerCase().includes(this.search.toLowerCase());
      const matchesStatus = this.filterStatus === 'All' || p.status === this.filterStatus;
      return matchesSearch && matchesStatus;
    });
  }

  statusClass(status: string): string {
    const map: Record<string, string> = {
      Active: 'bg-green-100 text-green-700',
      Admitted: 'bg-blue-100 text-blue-700',
      Discharged: 'bg-gray-100 text-gray-500',
    };
    return map[status] ?? '';
  }

  openPatient(p: Patient) {
    this.selectedPatient = p;
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.selectedPatient = null;
  }
}

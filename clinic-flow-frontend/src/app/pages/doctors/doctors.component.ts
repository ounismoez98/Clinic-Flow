import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface Doctor {
  id: number;
  name: string;
  specialty: string;
  phone: string;
  email: string;
  experience: number;
  patientsToday: number;
  rating: number;
  status: 'Available' | 'In Consultation' | 'Off Duty';
  avatar: string;
  schedule: string;
}

@Component({
  selector: 'app-doctors',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './doctors.component.html',
  styleUrl: './doctors.component.scss'
})
export class DoctorsComponent {
  search = '';
  filterStatus = 'All';
  viewMode: 'table' | 'grid' = 'grid';
  showModal = false;
  selectedDoctor: Doctor | null = null;

  statusOptions = ['All', 'Available', 'In Consultation', 'Off Duty'];

  specialties = ['All', 'Cardiology', 'Neurology', 'Pediatrics', 'Orthopedics', 'Dermatology'];
  filterSpecialty = 'All';

  stats = [
    { label: 'Total Doctors', value: '284', delta: '+4', positive: true, color: 'text-blue-600 bg-blue-50', icon: 'M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z' },
    { label: 'Available Now', value: '47', delta: '', positive: true, color: 'text-green-600 bg-green-50', icon: 'M9 12.75 11.25 15 15 9.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z' },
    { label: 'In Consultation', value: '23', delta: '', positive: true, color: 'text-orange-600 bg-orange-50', icon: 'M15.75 6a3.75 3.75 0 1 1-7.5 0 3.75 3.75 0 0 1 7.5 0zM4.501 20.118a7.5 7.5 0 0 1 14.998 0A17.933 17.933 0 0 1 12 21.75c-2.676 0-5.216-.584-7.499-1.632z' },
    { label: 'Avg Rating', value: '4.8', delta: '+0.1', positive: true, color: 'text-yellow-600 bg-yellow-50', icon: 'M11.48 3.499a.562.562 0 0 1 1.04 0l2.125 5.111a.563.563 0 0 0 .475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 0 0-.182.557l1.285 5.385a.562.562 0 0 1-.84.61l-4.725-2.885a.562.562 0 0 0-.586 0L6.982 20.54a.562.562 0 0 1-.84-.61l1.285-5.386a.562.562 0 0 0-.182-.557l-4.204-3.602a.562.562 0 0 1 .321-.988l5.518-.442a.563.563 0 0 0 .475-.345L11.48 3.5z' },
  ];

  doctors: Doctor[] = [
    { id: 1, name: 'Dr. Sami Karray', specialty: 'Cardiology', phone: '+216 22 100 200', email: 'karray@clinic.com', experience: 14, patientsToday: 9, rating: 4.9, status: 'Available', avatar: 'SK', schedule: 'Mon–Fri · 08:00–16:00' },
    { id: 2, name: 'Dr. Rania Mansouri', specialty: 'Neurology', phone: '+216 55 300 400', email: 'mansouri@clinic.com', experience: 10, patientsToday: 7, rating: 4.8, status: 'In Consultation', avatar: 'RM', schedule: 'Mon–Sat · 09:00–17:00' },
    { id: 3, name: 'Dr. Faouzi Belhaj', specialty: 'Orthopedics', phone: '+216 98 500 600', email: 'belhaj@clinic.com', experience: 18, patientsToday: 11, rating: 4.7, status: 'Available', avatar: 'FB', schedule: 'Tue–Sat · 08:30–15:30' },
    { id: 4, name: 'Dr. Meriem Ayari', specialty: 'Pediatrics', phone: '+216 20 700 800', email: 'ayari@clinic.com', experience: 7, patientsToday: 5, rating: 4.9, status: 'Off Duty', avatar: 'MA', schedule: 'Mon–Thu · 10:00–18:00' },
    { id: 5, name: 'Dr. Bilel Chaouachi', specialty: 'Dermatology', phone: '+216 50 900 100', email: 'chaouachi@clinic.com', experience: 11, patientsToday: 8, rating: 4.6, status: 'In Consultation', avatar: 'BC', schedule: 'Wed–Sun · 08:00–16:00' },
    { id: 6, name: 'Dr. Ines Zouari', specialty: 'Cardiology', phone: '+216 23 110 220', email: 'zouari@clinic.com', experience: 9, patientsToday: 6, rating: 4.8, status: 'Available', avatar: 'IZ', schedule: 'Mon–Fri · 09:00–17:00' },
  ];

  get filtered(): Doctor[] {
    return this.doctors.filter(d => {
      const matchesSearch = d.name.toLowerCase().includes(this.search.toLowerCase()) ||
                            d.specialty.toLowerCase().includes(this.search.toLowerCase());
      const matchesStatus = this.filterStatus === 'All' || d.status === this.filterStatus;
      const matchesSpec = this.filterSpecialty === 'All' || d.specialty === this.filterSpecialty;
      return matchesSearch && matchesStatus && matchesSpec;
    });
  }

  statusClass(status: string): string {
    const map: Record<string, string> = {
      'Available': 'bg-green-100 text-green-700',
      'In Consultation': 'bg-blue-100 text-blue-700',
      'Off Duty': 'bg-gray-100 text-gray-500',
    };
    return map[status] ?? '';
  }

  statusDot(status: string): string {
    const map: Record<string, string> = {
      'Available': 'bg-green-500',
      'In Consultation': 'bg-blue-500',
      'Off Duty': 'bg-gray-400',
    };
    return map[status] ?? '';
  }

  stars(rating: number): number[] {
    return Array(5).fill(0);
  }

  openDoctor(d: Doctor) {
    this.selectedDoctor = d;
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.selectedDoctor = null;
  }
}

import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { interval, Subscription } from 'rxjs';
import { NotificationService, AppNotification } from '../../core/notification.service';
import { CurrentUserService, CurrentUser } from '../../auth/current-user.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss'
})
export class ShellComponent implements OnInit, OnDestroy {
  sidebarCollapsed = false;

  // Notifications (bell)
  notifications: AppNotification[] = [];
  unread = 0;
  showNotifications = false;
  private pollSub?: Subscription;

  // The logged-in user, read from the Keycloak token.
  currentUser: CurrentUser = { name: 'User', username: '', email: '', role: 'USER', initials: 'U' };

  constructor(
    private notificationService: NotificationService,
    private currentUserService: CurrentUserService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.currentUserService.get();
    this.refreshUnread();
    // Poll the unread badge every 15s so new events appear without a page reload.
    this.pollSub = interval(15000).subscribe(() => this.refreshUnread());
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  refreshUnread(): void {
    this.notificationService.unreadCount().subscribe({
      next: (c) => (this.unread = c),
      error: () => {},   // not logged in / gateway down -> just leave badge as-is
    });
  }

  toggleNotifications(): void {
    this.showNotifications = !this.showNotifications;
    if (this.showNotifications) {
      this.notificationService.getAll().subscribe({
        next: (list) => (this.notifications = list ?? []),
        error: () => (this.notifications = []),
      });
    }
  }

  markRead(n: AppNotification): void {
    if (n.read) return;
    this.notificationService.markRead(n.id).subscribe(() => {
      n.read = true;
      this.refreshUnread();
    });
  }

  markAllRead(): void {
    this.notificationService.markAllRead().subscribe(() => {
      this.notifications.forEach((n) => (n.read = true));
      this.unread = 0;
    });
  }

  navGroups = [
    {
      label: 'Clinical',
      items: [
        { label: 'Patients', icon: 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z', route: '/dashboard/patients', badge: null },
        { label: 'Doctors', icon: 'M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z', route: '/dashboard/doctors', badge: null },
        { label: 'Appointments', icon: 'M8 2v4M16 2v4M3 10h18M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z', route: '/dashboard/appointments', badge: '6' },
      ]
    },
    {
      label: 'Services',
      items: [
        { label: 'Prescriptions', icon: 'M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2M9 5a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2M9 5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2', route: '/dashboard/prescriptions', badge: null },
        { label: 'Laboratory', icon: 'M19.428 15.428a2 2 0 0 0-1.022-.547l-2.387-.477a6 6 0 0 0-3.86.517l-.318.158a6 6 0 0 1-3.86.517L6.05 15.21a2 2 0 0 0-1.806.547M8 4h8l-1 1v5.172a2 2 0 0 0 .586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 0 0 9 10.172V5L8 4z', route: '/dashboard/laboratory', badge: null },
        { label: 'Pharmacy', icon: 'M19 7H5a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2zM16 3H8l-1 4h10l-1-4z', route: '/dashboard/pharmacy', badge: null },
      ]
    },
    {
      label: 'Finance',
      items: [
        { label: 'Billing', icon: 'M9 14l6-6m-5.5.5h.01m4.99 5h.01M19 21V5a2 2 0 0 0-2-2H7a2 2 0 0 0-2 2v16l3.5-2 3.5 2 3.5-2 3.5 2z', route: '/dashboard/billing', badge: null },
        { label: 'Factures', icon: 'M9 14l6-6m-5.5.5h.01m4.99 5h.01M19 21V5a2 2 0 0 0-2-2H7a2 2 0 0 0-2 2v16l3.5-2 3.5 2 3.5-2 3.5 2z', route: '/dashboard/factures', badge: null },
      ]
    }
  ];

  toggleSidebar() {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }

  logout() {
    this.currentUserService.logout();
  }
}

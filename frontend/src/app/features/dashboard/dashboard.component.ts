import { Component, inject, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ApiService } from '../../core/api.service';
import { DashboardResponse, WorkflowStatus } from '../../core/models';

interface MetricCard {
  label: string;
  value: number;
  hint: string;
  icon: string;
  tone: string;
}

@Component({
  selector: 'ff-dashboard',
  standalone: true,
  imports: [MatCardModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  private readonly api = inject(ApiService);

  readonly dashboard = signal<DashboardResponse | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.getDashboard().subscribe({
      next: (dashboard) => {
        this.dashboard.set(dashboard);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Dashboard metrics could not be loaded.');
        this.loading.set(false);
      }
    });
  }

  cards(data: DashboardResponse): MetricCard[] {
    return [
      { label: 'Total Requests', value: data.totalRequests, hint: 'Visible workflow requests', icon: 'inventory_2', tone: 'primary' },
      { label: 'Pending Requests', value: data.pendingRequests, hint: 'Assigned to you', icon: 'pending_actions', tone: 'warning' },
      { label: 'In Progress', value: data.inProgressRequests, hint: 'Submitted through HR approved', icon: 'timeline', tone: 'accent' },
      { label: 'Completed', value: data.completedRequests, hint: 'Closed successfully', icon: 'task_alt', tone: 'success' },
      { label: 'Rejected', value: data.rejectedRequests, hint: 'Closed with rejection', icon: 'block', tone: 'danger' }
    ];
  }

  statusClass(status: WorkflowStatus): string {
    return `status-${status.toLowerCase().replaceAll('_', '-')}`;
  }

  percent(value: number, max: number): number {
    if (max <= 0) {
      return 0;
    }
    return Math.max(4, Math.round((value / max) * 100));
  }

  maxMonthTotal(data: DashboardResponse): number {
    return Math.max(1, ...data.requestsByMonth.map((metric) => metric.total));
  }

  formatMonth(month: string): string {
    const [year, monthIndex] = month.split('-').map(Number);
    return new Date(year, monthIndex - 1, 1).toLocaleDateString(undefined, { month: 'short', year: '2-digit' });
  }
}

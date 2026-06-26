import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { debounceTime } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { WorkflowStatus, WorkflowSummary } from '../../core/models';

@Component({
  selector: 'ff-workflow-list',
  standalone: true,
  imports: [
    DatePipe,
    RouterLink,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatSelectModule,
    MatSortModule,
    MatTableModule
  ],
  templateUrl: './workflow-list.component.html',
  styleUrl: './workflow-list.component.scss'
})
export class WorkflowListComponent {
  private readonly api = inject(ApiService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  readonly statuses: WorkflowStatus[] = ['DRAFT', 'SUBMITTED', 'MANAGER_APPROVED', 'HR_APPROVED', 'COMPLETED', 'REJECTED'];
  readonly displayedColumns = ['id', 'title', 'status', 'createdBy', 'assignedTo', 'updatedAt'];
  readonly workflows = signal<WorkflowSummary[]>([]);
  readonly total = signal(0);
  readonly loading = signal(true);
  readonly creating = signal(false);
  readonly error = signal<string | null>(null);
  readonly showCreate = signal(false);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(10);
  readonly sortActive = signal('updatedAt');
  readonly sortDirection = signal<'asc' | 'desc'>('desc');

  readonly filterForm = this.fb.nonNullable.group({
    search: [''],
    status: ['' as WorkflowStatus | '']
  });

  readonly createForm = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(180)]],
    description: ['', [Validators.maxLength(4000)]]
  });

  constructor() {
    this.filterForm.valueChanges.pipe(debounceTime(250)).subscribe(() => {
      this.pageIndex.set(0);
      this.load();
    });
    this.load();
  }

  load(): void {
    const filters = this.filterForm.getRawValue();
    this.loading.set(true);
    this.error.set(null);
    this.api.listWorkflows({
      page: this.pageIndex(),
      size: this.pageSize(),
      sort: this.sortActive(),
      direction: this.sortDirection(),
      status: filters.status,
      search: filters.search.trim()
    }).subscribe({
      next: (page) => {
        this.workflows.set(page.content);
        this.total.set(page.totalElements);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Workflow requests could not be loaded.');
        this.loading.set(false);
      }
    });
  }

  pageChanged(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
    this.load();
  }

  sortChanged(sort: Sort): void {
    this.sortActive.set(sort.active || 'updatedAt');
    this.sortDirection.set((sort.direction || 'desc') as 'asc' | 'desc');
    this.load();
  }

  createDraft(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    this.creating.set(true);
    this.error.set(null);
    const value = this.createForm.getRawValue();
    this.api.createWorkflow({
      title: value.title,
      description: value.description || null
    }).subscribe({
      next: (workflow) => {
        this.creating.set(false);
        this.createForm.reset();
        this.showCreate.set(false);
        void this.router.navigate(['/workflows', workflow.id]);
      },
      error: () => {
        this.error.set('Draft request could not be created.');
        this.creating.set(false);
      }
    });
  }

  statusClass(status: WorkflowStatus): string {
    return `status-${status.toLowerCase().replaceAll('_', '-')}`;
  }

  statusLabel(status: WorkflowStatus): string {
    return status
      .toLowerCase()
      .split('_')
      .map((part) => part[0].toUpperCase() + part.slice(1))
      .join(' ');
  }
}

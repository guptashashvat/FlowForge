import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Observable } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { AuditEntry, AvailableWorkflowAction, WorkflowDetail, WorkflowStatus } from '../../core/models';

@Component({
  selector: 'ff-workflow-detail',
  standalone: true,
  imports: [
    DatePipe,
    RouterLink,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatDividerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './workflow-detail.component.html',
  styleUrl: './workflow-detail.component.scss'
})
export class WorkflowDetailComponent {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);
  private readonly requestId = Number(this.route.snapshot.paramMap.get('id'));

  readonly workflow = signal<WorkflowDetail | null>(null);
  readonly loading = signal(true);
  readonly loadingAction = signal<AvailableWorkflowAction | 'SAVE_DRAFT' | null>(null);
  readonly editMode = signal(false);
  readonly error = signal<string | null>(null);

  readonly editForm = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.maxLength(180)]],
    description: ['', [Validators.maxLength(4000)]]
  });

  readonly actionForm = this.fb.nonNullable.group({
    comments: ['', [Validators.maxLength(2000)]]
  });

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.getWorkflow(this.requestId).subscribe({
      next: (workflow) => {
        this.setWorkflow(workflow);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Workflow request could not be loaded.');
        this.loading.set(false);
      }
    });
  }

  can(action: AvailableWorkflowAction): boolean {
    return this.workflow()?.availableActions.includes(action) ?? false;
  }

  startEdit(): void {
    const workflow = this.workflow();
    if (!workflow) {
      return;
    }
    this.editForm.setValue({
      title: workflow.title,
      description: workflow.description ?? ''
    });
    this.editMode.set(true);
  }

  saveDraft(): void {
    if (this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }

    this.loadingAction.set('SAVE_DRAFT');
    const value = this.editForm.getRawValue();
    this.api.updateWorkflow(this.requestId, {
      title: value.title,
      description: value.description || null
    }).subscribe(this.actionObserver('Draft updated.', () => this.editMode.set(false)));
  }

  perform(action: AvailableWorkflowAction): void {
    let request$: Observable<WorkflowDetail>;
    const comments = this.actionForm.getRawValue().comments || null;
    this.loadingAction.set(action);

    switch (action) {
      case 'SUBMIT':
        request$ = this.api.submitWorkflow(this.requestId, { comments });
        break;
      case 'APPROVE':
        request$ = this.api.approveWorkflow(this.requestId, { comments });
        break;
      case 'REJECT':
        request$ = this.api.rejectWorkflow(this.requestId, { comments });
        break;
      case 'COMPLETE':
        request$ = this.api.completeWorkflow(this.requestId, { comments });
        break;
      case 'COMMENT':
        if (!comments) {
          this.error.set('Enter a comment before adding it to the audit trail.');
          this.loadingAction.set(null);
          return;
        }
        request$ = this.api.commentWorkflow(this.requestId, { comments });
        break;
      default:
        this.loadingAction.set(null);
        return;
    }

    request$.subscribe(this.actionObserver('Action completed.'));
  }

  statusClass(status: WorkflowStatus): string {
    return `status-${status.toLowerCase().replaceAll('_', '-')}`;
  }

  actionLabel(action: AvailableWorkflowAction): string {
    const workflow = this.workflow();
    if (action === 'APPROVE' && workflow?.status === 'SUBMITTED') {
      return 'Manager Approve';
    }
    if (action === 'APPROVE' && workflow?.status === 'MANAGER_APPROVED') {
      return 'HR Approve';
    }
    return action
      .toLowerCase()
      .split('_')
      .map((part) => part[0].toUpperCase() + part.slice(1))
      .join(' ');
  }

  auditLabel(audit: AuditEntry): string {
    return audit.action
      .toLowerCase()
      .split('_')
      .map((part) => part[0].toUpperCase() + part.slice(1))
      .join(' ');
  }

  transitionText(audit: AuditEntry): string {
    if (!audit.previousStatus || audit.previousStatus === audit.newStatus) {
      return audit.newStatus.replaceAll('_', ' ');
    }
    return `${audit.previousStatus.replaceAll('_', ' ')} to ${audit.newStatus.replaceAll('_', ' ')}`;
  }

  private actionObserver(successMessage: string, afterSuccess?: () => void) {
    return {
      next: (workflow: WorkflowDetail) => {
        this.setWorkflow(workflow);
        this.error.set(null);
        this.actionForm.reset();
        this.loadingAction.set(null);
        afterSuccess?.();
      },
      error: () => {
        this.error.set(successMessage === 'Action completed.' ? 'The workflow action could not be completed.' : 'The draft could not be saved.');
        this.loadingAction.set(null);
      }
    };
  }

  private setWorkflow(workflow: WorkflowDetail): void {
    this.workflow.set(workflow);
    this.editForm.setValue({
      title: workflow.title,
      description: workflow.description ?? ''
    });
  }
}

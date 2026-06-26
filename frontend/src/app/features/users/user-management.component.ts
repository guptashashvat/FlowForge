import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleChange, MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatTableModule } from '@angular/material/table';
import { ApiService } from '../../core/api.service';
import { RoleName, UserResponse } from '../../core/models';

@Component({
  selector: 'ff-user-management',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatTableModule
  ],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.scss'
})
export class UserManagementComponent {
  private readonly api = inject(ApiService);
  private readonly fb = inject(FormBuilder);

  readonly roles: RoleName[] = ['EMPLOYEE', 'MANAGER', 'HR_ADMIN'];
  readonly displayedColumns = ['user', 'roles', 'active', 'createdAt'];
  readonly users = signal<UserResponse[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  readonly createForm = this.fb.nonNullable.group({
    fullName: ['', [Validators.required, Validators.maxLength(160)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['FlowForge@123', [Validators.required, Validators.minLength(8)]],
    roles: [['EMPLOYEE'] as RoleName[], [Validators.required]]
  });

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.listUsers().subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Users could not be loaded. HR Admin access is required.');
        this.loading.set(false);
      }
    });
  }

  createUser(): void {
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.error.set(null);
    this.api.createUser(this.createForm.getRawValue()).subscribe({
      next: () => {
        this.saving.set(false);
        this.createForm.reset({
          fullName: '',
          email: '',
          password: 'FlowForge@123',
          roles: ['EMPLOYEE']
        });
        this.load();
      },
      error: () => {
        this.error.set('User could not be created. Check for duplicate email addresses.');
        this.saving.set(false);
      }
    });
  }

  setActive(user: UserResponse, event: MatSlideToggleChange): void {
    this.api.setUserActive(user.id, event.checked).subscribe({
      next: (updated) => this.replaceUser(updated),
      error: () => this.error.set('User status could not be updated.')
    });
  }

  assignRoles(user: UserResponse, roles: RoleName[]): void {
    if (roles.length === 0) {
      this.error.set('A user must have at least one role.');
      return;
    }
    this.api.assignRoles(user.id, roles).subscribe({
      next: (updated) => this.replaceUser(updated),
      error: () => this.error.set('Roles could not be updated.')
    });
  }

  roleLabel(role: RoleName): string {
    return role
      .toLowerCase()
      .split('_')
      .map((part) => part[0].toUpperCase() + part.slice(1))
      .join(' ');
  }

  private replaceUser(updated: UserResponse): void {
    this.users.update((users) => users.map((user) => user.id === updated.id ? updated : user));
  }
}

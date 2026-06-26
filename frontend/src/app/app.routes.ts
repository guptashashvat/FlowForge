import { Routes } from '@angular/router';
import { AppShellComponent } from './layout/app-shell.component';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login.component').then((m) => m.LoginComponent)
  },
  {
    path: '',
    component: AppShellComponent,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent)
      },
      {
        path: 'workflows',
        loadComponent: () => import('./features/workflows/workflow-list.component').then((m) => m.WorkflowListComponent)
      },
      {
        path: 'workflows/:id',
        loadComponent: () => import('./features/workflows/workflow-detail.component').then((m) => m.WorkflowDetailComponent)
      },
      {
        path: 'users',
        loadComponent: () => import('./features/users/user-management.component').then((m) => m.UserManagementComponent)
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'dashboard'
      }
    ]
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];

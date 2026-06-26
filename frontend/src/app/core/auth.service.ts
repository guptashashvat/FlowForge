import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, LoginRequest, RoleName, UserResponse } from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly tokenKey = 'flowforge.jwt';
  private readonly tokenState = signal<string | null>(localStorage.getItem(this.tokenKey));
  private readonly userState = signal<UserResponse | null>(null);

  readonly token = computed(() => this.tokenState());
  readonly user = computed(() => this.userState());
  readonly isAuthenticated = computed(() => Boolean(this.tokenState()));

  constructor() {
    if (this.tokenState()) {
      this.refreshCurrentUser().subscribe({ error: () => this.clearSession() });
    }
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, request).pipe(
      tap((response) => this.setSession(response))
    );
  }

  refreshCurrentUser(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${environment.apiUrl}/auth/me`).pipe(
      tap((user) => this.userState.set(user))
    );
  }

  logout(): void {
    this.http.post<void>(`${environment.apiUrl}/auth/logout`, {}).subscribe({ complete: () => undefined });
    this.clearSession();
    void this.router.navigateByUrl('/login');
  }

  hasRole(role: RoleName): boolean {
    return this.userState()?.roles.includes(role) ?? false;
  }

  private setSession(response: AuthResponse): void {
    localStorage.setItem(this.tokenKey, response.token);
    this.tokenState.set(response.token);
    this.userState.set(response.user);
  }

  private clearSession(): void {
    localStorage.removeItem(this.tokenKey);
    this.tokenState.set(null);
    this.userState.set(null);
  }
}

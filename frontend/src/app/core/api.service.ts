import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  CreateUserRequest,
  DashboardResponse,
  PageResponse,
  RoleName,
  UserResponse,
  WorkflowActionRequest,
  WorkflowDetail,
  WorkflowStatus,
  WorkflowSummary,
  WorkflowWriteRequest
} from './models';

export interface WorkflowListParams {
  page: number;
  size: number;
  sort: string;
  direction: 'asc' | 'desc';
  status?: WorkflowStatus | '';
  search?: string;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = environment.apiUrl;

  getDashboard(): Observable<DashboardResponse> {
    return this.http.get<DashboardResponse>(`${this.apiUrl}/dashboard`);
  }

  listWorkflows(params: WorkflowListParams): Observable<PageResponse<WorkflowSummary>> {
    let query = new HttpParams()
      .set('page', params.page)
      .set('size', params.size)
      .set('sort', params.sort)
      .set('direction', params.direction);

    if (params.status) {
      query = query.set('status', params.status);
    }
    if (params.search) {
      query = query.set('search', params.search);
    }

    return this.http.get<PageResponse<WorkflowSummary>>(`${this.apiUrl}/workflows`, { params: query });
  }

  getWorkflow(id: number): Observable<WorkflowDetail> {
    return this.http.get<WorkflowDetail>(`${this.apiUrl}/workflows/${id}`);
  }

  createWorkflow(request: WorkflowWriteRequest): Observable<WorkflowDetail> {
    return this.http.post<WorkflowDetail>(`${this.apiUrl}/workflows`, request);
  }

  updateWorkflow(id: number, request: WorkflowWriteRequest): Observable<WorkflowDetail> {
    return this.http.put<WorkflowDetail>(`${this.apiUrl}/workflows/${id}`, request);
  }

  submitWorkflow(id: number, request: WorkflowActionRequest): Observable<WorkflowDetail> {
    return this.http.post<WorkflowDetail>(`${this.apiUrl}/workflows/${id}/submit`, request);
  }

  approveWorkflow(id: number, request: WorkflowActionRequest): Observable<WorkflowDetail> {
    return this.http.post<WorkflowDetail>(`${this.apiUrl}/workflows/${id}/approve`, request);
  }

  rejectWorkflow(id: number, request: WorkflowActionRequest): Observable<WorkflowDetail> {
    return this.http.post<WorkflowDetail>(`${this.apiUrl}/workflows/${id}/reject`, request);
  }

  completeWorkflow(id: number, request: WorkflowActionRequest): Observable<WorkflowDetail> {
    return this.http.post<WorkflowDetail>(`${this.apiUrl}/workflows/${id}/complete`, request);
  }

  commentWorkflow(id: number, request: WorkflowActionRequest): Observable<WorkflowDetail> {
    return this.http.post<WorkflowDetail>(`${this.apiUrl}/workflows/${id}/comments`, request);
  }

  listUsers(): Observable<UserResponse[]> {
    return this.http.get<UserResponse[]>(`${this.apiUrl}/users`);
  }

  createUser(request: CreateUserRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(`${this.apiUrl}/users`, request);
  }

  setUserActive(id: number, active: boolean): Observable<UserResponse> {
    return this.http.patch<UserResponse>(`${this.apiUrl}/users/${id}/status`, { active });
  }

  assignRoles(id: number, roles: RoleName[]): Observable<UserResponse> {
    return this.http.patch<UserResponse>(`${this.apiUrl}/users/${id}/roles`, { roles });
  }
}

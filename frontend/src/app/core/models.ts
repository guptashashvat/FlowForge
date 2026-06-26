export type RoleName = 'EMPLOYEE' | 'MANAGER' | 'HR_ADMIN';

export type WorkflowStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'MANAGER_APPROVED'
  | 'HR_APPROVED'
  | 'COMPLETED'
  | 'REJECTED';

export type WorkflowAction =
  | 'CREATE'
  | 'UPDATE'
  | 'SUBMIT'
  | 'MANAGER_APPROVE'
  | 'HR_APPROVE'
  | 'COMPLETE'
  | 'REJECT'
  | 'COMMENT';

export type AvailableWorkflowAction = 'EDIT_DRAFT' | 'SUBMIT' | 'APPROVE' | 'REJECT' | 'COMPLETE' | 'COMMENT';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  tokenType: 'Bearer';
  user: UserResponse;
}

export interface UserResponse {
  id: number;
  email: string;
  fullName: string;
  active: boolean;
  roles: RoleName[];
  createdAt: string;
  updatedAt: string;
}

export interface CreateUserRequest {
  email: string;
  fullName: string;
  password: string;
  roles: RoleName[];
}

export interface WorkflowSummary {
  id: number;
  title: string;
  status: WorkflowStatus;
  statusLabel: string;
  createdBy: UserResponse;
  assignedTo: UserResponse | null;
  createdAt: string;
  updatedAt: string;
}

export interface WorkflowDetail extends WorkflowSummary {
  description: string | null;
  availableActions: AvailableWorkflowAction[];
  auditHistory: AuditEntry[];
}

export interface AuditEntry {
  id: number;
  timestamp: string;
  user: UserResponse;
  action: WorkflowAction;
  previousStatus: WorkflowStatus | null;
  newStatus: WorkflowStatus;
  comments: string | null;
}

export interface WorkflowWriteRequest {
  title: string;
  description?: string | null;
  assignedToId?: number | null;
}

export interface WorkflowActionRequest {
  comments?: string | null;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface DashboardResponse {
  totalRequests: number;
  pendingRequests: number;
  inProgressRequests: number;
  completedRequests: number;
  rejectedRequests: number;
  requestsByStatus: StatusMetric[];
  requestsByMonth: MonthlyMetric[];
}

export interface StatusMetric {
  status: WorkflowStatus;
  label: string;
  total: number;
}

export interface MonthlyMetric {
  month: string;
  total: number;
}

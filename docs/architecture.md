# FlowForge Architecture

FlowForge is structured as a reusable workflow foundation: role-based users perform state transitions, every transition produces an immutable audit entry, and the UI consumes typed REST contracts.

```mermaid
flowchart LR
    Browser["Angular 20 SPA\nStandalone components, Signals, Material"]
    Auth["Auth Module\nJWT session"]
    WorkflowUI["Workflow Screens\nList, Detail, Audit Timeline"]
    UserUI["User Management\nHR Admin"]

    Api["Spring Boot REST API"]
    Security["Spring Security\nJWT Filter + RBAC"]
    Services["Application Services\nWorkflowService, UserService, DashboardService"]
    Domain["Domain Model\nUsers, Roles, Requests, Audits"]
    Repos["Spring Data JPA Repositories"]
    Db[("PostgreSQL\nFlyway schema")]

    Browser --> Auth
    Browser --> WorkflowUI
    Browser --> UserUI
    Auth --> Api
    WorkflowUI --> Api
    UserUI --> Api
    Api --> Security
    Security --> Services
    Services --> Domain
    Services --> Repos
    Repos --> Db
```

## Backend Boundaries

- `domain`: workflow statuses, actions, roles, and JPA-backed business entities.
- `application`: use-case services for authentication, users, dashboards, and workflow transitions.
- `repository`: Spring Data persistence contracts.
- `security`: JWT generation, authentication filter, and `UserPrincipal`.
- `web`: controllers, DTOs, and API error handling.
- `config`: security configuration and seed data.

## Workflow State Machine

```mermaid
stateDiagram-v2
    [*] --> DRAFT
    DRAFT --> SUBMITTED: Employee submit
    SUBMITTED --> MANAGER_APPROVED: Manager approve
    SUBMITTED --> REJECTED: Manager reject
    MANAGER_APPROVED --> HR_APPROVED: HR approve
    MANAGER_APPROVED --> REJECTED: HR reject
    HR_APPROVED --> COMPLETED: HR complete
    HR_APPROVED --> REJECTED: HR reject
    COMPLETED --> [*]
    REJECTED --> [*]
```

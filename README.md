# FlowForge

FlowForge is a production-oriented enterprise workflow and approval management platform. It demonstrates reusable approval architecture for business applications such as HRMS, ATS, CRM, ERP, inspection management, and internal workflow automation.

## Stack

- Frontend: Angular 20, TypeScript, Angular Material, standalone components, Signals
- Backend: Java 17, Spring Boot 3, Spring Security, JWT, Spring Data JPA
- Database: PostgreSQL with Flyway migrations
- Infrastructure: Docker Compose, backend container, frontend container, PostgreSQL container

## Core Features

- JWT login/logout and role-based authorization
- User management for HR Admins
- Employee onboarding workflow with Draft, Submitted, Manager Approved, HR Approved, Completed, and Rejected states
- Manager and HR approval/rejection/comment actions
- Immutable audit trail for every workflow action
- Dashboard KPI cards and charts
- Searchable, sortable, paginated workflow list with status filtering
- Workflow detail screen with request metadata, available actions, and audit timeline

## Seeded Users

All seeded accounts use the password `FlowForge@123`.

| Role | Email |
| --- | --- |
| Employee | `employee@flowforge.com` |
| Manager | `manager@flowforge.com` |
| HR Admin | `hr@flowforge.com` |

## Run With Docker

```bash
docker compose up --build
```

- Frontend: `http://localhost:4200`
- Backend API: `http://localhost:8080/api`
- PostgreSQL: `localhost:5432`

## Run Locally

Start PostgreSQL first, then run the backend:

```bash
cd backend
mvn spring-boot:run
```

Run the Angular app:

```bash
cd frontend
npm install
npm start
```

## API Overview

- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET /api/dashboard`
- `GET /api/workflows`
- `POST /api/workflows`
- `GET /api/workflows/{id}`
- `PUT /api/workflows/{id}`
- `POST /api/workflows/{id}/submit`
- `POST /api/workflows/{id}/approve`
- `POST /api/workflows/{id}/reject`
- `POST /api/workflows/{id}/complete`
- `POST /api/workflows/{id}/comments`
- `GET /api/users`
- `POST /api/users`
- `PATCH /api/users/{id}/status`
- `PATCH /api/users/{id}/roles`

## Project Structure

```text
backend/
  src/main/java/com/flowforge/
    application/       Use-case services
    config/            Security and seed data
    domain/            Entities and workflow model
    repository/        Spring Data JPA contracts
    security/          JWT and authenticated principal
    web/               Controllers, DTOs, errors
  src/main/resources/db/migration/
frontend/
  src/app/
    core/              Auth, API services, guards, models
    features/          Login, dashboard, workflows, users
    layout/            Authenticated shell
docs/
  architecture.md
  schema.sql
  screenshots-guide.md
```

## Documentation

- Architecture diagram: [docs/architecture.md](docs/architecture.md)
- Database schema: [docs/schema.sql](docs/schema.sql)
- Screenshot guide: [docs/screenshots-guide.md](docs/screenshots-guide.md)

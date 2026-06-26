# Portfolio Screenshots Guide

Run the platform, then capture screenshots at 1440x1000 or 1600x1000 for a portfolio case study.

## Start The App

```bash
docker compose up --build
```

Frontend: `http://localhost:4200`

Backend API: `http://localhost:8080/api`

## Seeded Accounts

All seeded users use the password `FlowForge@123`.

| Role | Email |
| --- | --- |
| Employee | `employee@flowforge.com` |
| Manager | `manager@flowforge.com` |
| HR Admin | `hr@flowforge.com` |

## Screenshot Set

1. Login Screen
   - URL: `http://localhost:4200/login`
   - Show the FlowForge sign-in panel and seeded account buttons.

2. Dashboard
   - Sign in as `hr@flowforge.com`.
   - URL: `http://localhost:4200/dashboard`
   - Capture KPI cards plus both charts.

3. Workflow List
   - URL: `http://localhost:4200/workflows`
   - Use the status filter and search field if you want a more curated view.

4. Workflow Detail
   - Open a workflow from the list.
   - For manager actions, sign in as `manager@flowforge.com` and open a `Submitted` request.
   - For HR actions, sign in as `hr@flowforge.com` and open a `Manager Approved` or `HR Approved` request.

5. Audit Trail
   - On any workflow detail page, scroll to the Audit Trail section.
   - Use the completed sample workflow for the richest timeline.

6. User Management
   - Sign in as `hr@flowforge.com`.
   - URL: `http://localhost:4200/users`
   - Capture the create-user form and user table with role assignment controls.

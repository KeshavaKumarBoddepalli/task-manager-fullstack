# Task Manager Fullstack

A task management application with an Angular frontend, Spring Boot REST API, and Supabase PostgreSQL database.

## Project structure

```text
frontend/    Angular application
backend/     Spring Boot API
README.md
.gitignore
```

## Requirements

- Java 21
- Node.js and npm
- Supabase PostgreSQL project with the `public.tasks` table

## Backend

The backend uses Maven and Spring Boot. Configure these environment variables in the terminal before starting it:

- `DATABASE_URL`: JDBC URL for the Supabase session pooler, for example `jdbc:postgresql://<pooler-host>:5432/postgres?sslmode=require`
- `DATABASE_USERNAME`: the pooler username from Supabase
- `DATABASE_PASSWORD`: the database password
- `APP_CORS_ALLOWED_ORIGINS`: optional; defaults to `http://localhost:4200`

Never commit database credentials or put them in the Angular application.

From PowerShell:

```powershell
cd backend
$env:DATABASE_URL = 'jdbc:postgresql://YOUR_POOLER_HOST:5432/postgres?sslmode=require'
$env:DATABASE_USERNAME = 'YOUR_POOLER_USERNAME'
$secret = Read-Host "Database password" -AsSecureString
$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secret)
try {
    $env:DATABASE_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
}
finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
}
.\mvnw.cmd spring-boot:run
```

Enter credentials only in your local terminal or your hosting provider's private environment-variable settings. Do not save real values in this file. To run backend tests:

```powershell
cd backend
.\mvnw.cmd test
```

Tests use an in-memory H2 database and do not modify Supabase.

## Frontend

The development environment calls `http://localhost:8080/api`. From a second terminal:

```powershell
cd frontend
npm install
npm start
```

Open `http://localhost:4200`. To run frontend tests and create a production build:

```powershell
npm test -- --watch=false
npm run build
```

The production API URL is configured separately in `frontend/src/environments/environment.production.ts`. Replace its placeholder with the deployed backend URL when the backend is deployed.

## REST API

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/tasks` | List tasks |
| `GET` | `/api/tasks?status=PENDING&search=report` | Filter by status and search title |
| `GET` | `/api/tasks/{id}` | Get a task |
| `POST` | `/api/tasks` | Create a task |
| `PUT` | `/api/tasks/{id}` | Update a task |
| `PATCH` | `/api/tasks/{id}/complete` | Mark a task completed |
| `DELETE` | `/api/tasks/{id}` | Delete a task |

Task statuses are `PENDING`, `IN_PROGRESS`, and `COMPLETED`.

## Deployment

The planned deployment targets are Supabase (PostgreSQL), Render (Spring Boot), and Vercel (Angular). Free-plan availability and limits can change; check each provider's current terms before deployment.

The backend's `Dockerfile` uses a Java 21 build stage and a Java 21 runtime image. Render builds it from the `backend/` directory, and the application listens on Render's `PORT` environment variable.

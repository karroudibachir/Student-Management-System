# StudentSpace — Student Management System

A responsive student directory and dashboard built with Angular 20, Spring Boot 3, and MySQL 8. The API keeps student records behind validated DTOs; Flyway owns the schema and sample data.

![Dashboard screenshot placeholder](docs/dashboard.png)

## Features

- Dashboard with enrollment KPIs, GPA, study area distribution, and recent students.
- Search, major/status/year filters, sortable pagination, and responsive student directory.
- Add, view, edit, and delete student records with client and server validation.
- Dark and light themes, saved in local storage and initialized from the OS preference.
- Spring Data pagination and filtering, OpenAPI docs, health checks, and consistent API errors.

## Architecture

```text
Browser ── Angular standalone SPA ── /api/v1 ── Spring MVC controller
  │              │                                  │
  └── Nginx      └── StudentService                 ▼
       │                                      Student service
       └──────────────── reverse proxy ──────────────┤
                                                     ▼
                                             JPA repository
                                                     │
                                                 MySQL 8
                                           Flyway schema + seeds
```

## Technology

- Frontend: Angular 20, standalone components, signals, strict TypeScript, SCSS, Reactive Forms, Router.
- Backend: Java 17, Spring Boot 3.3, Spring Web, Spring Data JPA, Bean Validation, Actuator, springdoc-openapi.
- Database: MySQL 8 in deployment, H2 for automated tests, Flyway migrations.
- Runtime: Docker Compose, Nginx, GitHub Actions, Render blueprint.

## API

Base URL: `/api/v1`. List results use Spring's `Page` JSON shape; `page` is zero-based.

| Method | Path | Description |
| --- | --- | --- |
| GET | `/students?page=0&size=10&q=amira&major=Biology&status=ACTIVE&year=2025` | Search and filter students |
| POST | `/students` | Create student; returns 201 and `Location` |
| GET | `/students/{id}` | Read a student |
| PUT | `/students/{id}` | Replace student details |
| DELETE | `/students/{id}` | Delete student; returns 204 |
| GET | `/students/stats` | Dashboard counts and average GPA |
| GET | `/students/majors` | Distinct study areas |
| GET | `/actuator/health` | Service health |
| GET | `/swagger-ui.html` | Interactive OpenAPI documentation |

`Student` records have a generated numeric key, a unique `STU-{enrollment year}-{id}` code and unique email. Personal and academic fields are stored with UTC creation and update timestamps. Flyway V1 creates the table and indexes; V2 inserts 16 example students.

## Quick start

### Full stack with Docker

Copy `.env.example` to `.env`, change the example passwords, then run:

```sh
docker compose up -d --build
```

Open [http://localhost:8080](http://localhost:8080). The frontend container serves the SPA and proxies `/api` to the backend. MySQL's named volume persists data. The first startup may take a few minutes while images build and migrations run.

### Local development

Requirements: Java 17, Maven 3.9+, Node 22, npm, and a running MySQL 8 database. Set `DB_URL`, `DB_USER`, and `DB_PASSWORD` in the environment before starting the API. For example, use the local URL in `.env.example` and set credentials for your own MySQL instance. Production credentials must come from the hosting provider's secret manager.

```sh
cd backend
mvn spring-boot:run
```

In another terminal:

```sh
cd frontend
npm ci
npm start
```

The Angular dev server is at [http://localhost:4200](http://localhost:4200); its proxy forwards `/api` to port 8081.

## Configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_URL` | local MySQL URL | JDBC connection string |
| `DB_USER` | `student` | Database user |
| `DB_PASSWORD` | `student` | Database password (set a secret in production) |
| `MYSQL_DATABASE` | `student_management` | Compose database name |
| `MYSQL_ROOT_PASSWORD` | Compose development fallback | MySQL root password; replace for deployment |
| `CORS_ORIGIN` | `http://localhost:4200` | Allowed browser origin for API calls |
| `SERVER_PORT` | `8081` | Backend port |
| `APP_PORT` | `8080` | Host port for frontend |
| `DB_POOL_SIZE` | `10` | Hikari maximum pool size |

The `.env.example` values are for local development only. Configure production secrets in the hosting provider's secret manager.

## Build and test

```sh
cd backend && mvn test
cd frontend && npm test -- --watch=false --browsers=ChromeHeadless
npm run build
```

Backend tests run against H2 (`test` profile). Frontend tests use Karma and ChromeHeadless. Automated coverage currently focuses on the backend service; extend the Angular component and HTTP tests as the app grows.

## Deploy to Render

The included `render.yaml` describes separate Docker web services for the API and frontend. Provision a managed MySQL database, configure the API's `DB_URL`, `DB_USER`, and `DB_PASSWORD`, then set `CORS_ORIGIN` to the deployed frontend URL. The free instance tier may sleep while idle; provision persistent managed SQL separately since an app service's filesystem is ephemeral. Add `RENDER_DEPLOY_HOOK_URL` to GitHub repository secrets if CI should trigger deployment.

## Manual acceptance checklist

- [ ] Add a student with required details and see it in the directory.
- [ ] Open a profile, edit a field, save, and verify the updated value.
- [ ] Delete a student after confirming the prompt.
- [ ] Search by name, email, and student code; filter by major, status, and year.
- [ ] Change page size and move between pages.
- [ ] Toggle the theme, refresh, and check that the preference persists.
- [ ] Check the sidebar, table cards, form, and profile on a narrow mobile viewport.

## Repository layout

```text
backend/                    Spring Boot REST API, migrations, and tests
frontend/                   Angular SPA, Nginx config, and container build
.github/workflows/ci.yml     CI build and test jobs
docker-compose.yml           Local multi-service stack
render.yaml                 Render Blueprint
.env.example                Local configuration template
```

# StudentSpace — Student Management System

A responsive student directory and dashboard built with Angular 20, Spring Boot 3, and MySQL 8. The API keeps student records behind validated DTOs; Flyway owns the schema and sample data.

![Dashboard screenshot placeholder](docs/dashboard.png)

## Features

- Dashboard with enrollment KPIs, GPA, study area distribution, and recent students.
- Search, major/status/year filters, sortable pagination, and responsive student directory.
- Add, view, edit, and delete student records with client and server validation.
- Dark and light themes, saved in local storage and initialized from the OS preference.
- OAuth 2.0 / OpenID Connect sign-in with Keycloak, Authorization Code + PKCE, and short-lived bearer JWTs.
- Database-managed workspace roles (`ADMIN`, `STAFF`, `VIEWER`) with API-enforced permissions and an administrator role-management page.
- Spring Data pagination and filtering, OpenAPI docs, health checks, and consistent API errors.

## Architecture

```text
Browser ── Angular standalone SPA ── /api/v1 ── Spring MVC controller
  │       │      │                                  │
  │       └──── Keycloak (OIDC / PKCE)               │
  └── Nginx      └── StudentService                 ▼
       │                                      Student service
       └──────────────── reverse proxy ──────────────┤
                                                     ▼
                                             JPA repository
                                                     │
                                                 MySQL 8
                                           Flyway schema + seeds
  API ── validates JWT signature, issuer, and expiry against Keycloak JWKS
```

## Technology

- Frontend: Angular 20, standalone components, signals, strict TypeScript, SCSS, Reactive Forms, Router.
- Backend: Java 17, Spring Boot 3.3, Spring Web, Spring Data JPA, Bean Validation, Actuator, springdoc-openapi.
- Authentication: Keycloak OIDC for user sign-in; Spring Security validates JWTs. StudentSpace roles are stored in MySQL and are independent of identity-provider roles.
- Database: MySQL 8 in deployment, H2 for automated tests, Flyway migrations.
- Runtime: Docker Compose, Nginx, GitHub Actions.

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
| GET | `/auth/me` | Get or provision the signed-in workspace user and roles |
| GET | `/admin/users` | List signed-in workspace members (ADMIN) |
| PUT | `/admin/users/{subject}/roles` | Set workspace roles (ADMIN) |
| GET | `/actuator/health` | Service health |
| GET | `/swagger-ui.html` | Interactive OpenAPI documentation |

`Student` records have a generated numeric key, a unique `STU-{enrollment year}-{id}` code and unique email. Personal and academic fields are stored with UTC creation and update timestamps. Flyway V1 creates the table and indexes; V2 inserts 16 example students.

## Quick start

### Full stack with Docker

Copy `.env.example` to `.env`, replace every example password, set `SMS_BOOTSTRAP_ADMIN_EMAIL` to the email of the first administrator, then run:

```sh
docker compose up -d --build
```

Open [http://localhost:8080](http://localhost:8080). The frontend container serves the SPA and proxies `/api` to the backend. MySQL's named volume persists data. The first startup may take a few minutes while images build and migrations run.

The compose stack starts a local Keycloak development server at [http://localhost:8180](http://localhost:8180) and imports the `student-management` realm. Sign in through StudentSpace using an account created in the Keycloak Admin Console; its local admin console credentials are the `KEYCLOAK_ADMIN_USER` and `KEYCLOAK_ADMIN_PASSWORD` values in `.env`. The first matching sign-in email is provisioned with `ADMIN`; all other first-time users start as `VIEWER`. Set `SMS_BOOTSTRAP_ADMIN_EMAIL` only for initial bootstrap, then clear it and restart the API after the admin has signed in. An administrator can promote members at **People & permissions**. Users appear in that list after their first successful sign-in.

Local Keycloak uses `start-dev` and is intended for development. For a deployed environment, use a separately managed HTTPS Keycloak/OIDC realm. Configure the public SPA client `sms-frontend` with Standard Flow enabled, PKCE method `S256`, and the exact frontend callback URI (`https://<frontend-host>/login`) and web origin (`https://<frontend-host>`). Set the issuer URL and JWKS URL to that realm's endpoints. The issuer must match the `iss` claim in access tokens. The browser's OIDC URL must be reachable by end users; the API's JWKS URL must be reachable by the backend.

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
| `OIDC_AUTH_URL` | `http://localhost:8180` | Identity provider base URL used by the browser adapter |
| `OIDC_REALM` | `student-management` | Keycloak realm name |
| `OIDC_CLIENT_ID` | `sms-frontend` | Public OIDC client id |
| `OIDC_ISSUER` | `http://localhost:8180/realms/student-management` | Expected JWT issuer (must equal token `iss`) |
| `OIDC_JWKS_URI` | local realm cert endpoint | Backend JWT signing-key endpoint |
| `SMS_BOOTSTRAP_ADMIN_EMAIL` | empty | One-time email to provision as first workspace ADMIN |
| `KEYCLOAK_ADMIN_USER` / `KEYCLOAK_ADMIN_PASSWORD` | required in Compose | Local Keycloak console bootstrap credentials |
| `BACKEND_ORIGIN` | `http://backend:8081` | API origin proxied by Nginx |

The `.env.example` values are for local development only. Configure production secrets in the hosting provider's secret manager.

## Build and test

```sh
cd backend && mvn verify
cd frontend && npm test -- --watch=false --browsers=ChromeHeadless
npm run build
```

Backend tests cover service rules with Mockito, JWT-to-role authority mapping, REST status and validation responses with MockMvc, and student/workspace-role persistence on H2. `mvn verify` enforces at least 80% line coverage for `StudentServiceImpl`. Frontend HTTP service, theme persistence, and form validation tests run with Karma and ChromeHeadless.

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
.env.example                Local configuration template
```

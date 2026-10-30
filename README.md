# StudentSpace — Student Management System

A responsive student directory and dashboard built with Angular 20, Spring Boot 3, and MySQL 8. The API keeps student records behind validated DTOs; Flyway owns the schema and sample data.

## Features

- Dashboard with enrollment KPIs, GPA, study area distribution, and recent students.
- Search, major/status/year filters, sortable pagination, and responsive student directory.
- Add, view, edit, and delete student records with client and server validation.
- Dark and light themes, saved in local storage and initialized from the OS preference.
- Email and password sign-in with signed, short-lived bearer JWTs.
- Database-managed `ADMIN` and `STUDENT` roles, enforced by the API.
- Student accounts can update personal details only on their own profile; admins manage all student records and accounts.
- Spring Data pagination and filtering, OpenAPI docs, health checks, and consistent API errors.

## Architecture

```text
Browser ── Angular standalone SPA ── /api/v1 ── Spring Security
  │                │   email/password → signed JWT  │
  └── Nginx        └── Bearer JWT → REST API        ▼
       │                                      Student service
       └──────────────── reverse proxy ──────────────┤
                                                     ▼
                                             JPA repository
                                                     │
                                                 MySQL 8
                                           Flyway schema + seeds
  API ── validates JWT signature, issuer, and expiry using the app secret
```

## Technology

- Frontend: Angular 20, standalone components, signals, strict TypeScript, SCSS, Reactive Forms, Router.
- Backend: Java 17, Spring Boot 3.3, Spring Web, Spring Data JPA, Bean Validation, Actuator, springdoc-openapi.
- Authentication: Spring Security issues and validates HMAC-signed JWTs; passwords are BCrypt-hashed. Roles live in MySQL.
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
| POST | `/auth/login` | Sign in with email and password; returns a bearer JWT |
| GET | `/auth/me` | Get the signed-in account and role |
| GET | `/admin/users` | List accounts (ADMIN) |
| POST | `/admin/users/student-accounts` | Create a login for an existing student record (ADMIN) |
| POST | `/admin/users/admin-accounts` | Create another admin login (ADMIN) |
| DELETE | `/admin/users/{subject}` | Remove a student login (ADMIN) |
| GET | `/students/me` | Read the signed-in student's own profile |
| PUT | `/students/me` | Update that student's personal/contact fields only |
| GET | `/actuator/health` | Service health |
| GET | `/swagger-ui.html` | Interactive OpenAPI documentation |

`Student` records have a generated numeric key, a unique `STU-{enrollment year}-{id}` code and unique email. Personal and academic fields are stored with UTC creation and update timestamps. Flyway V1 creates the table and indexes; V2 inserts 16 example students.

## Quick start

### Full stack with Docker

Copy `.env.example` to `.env`, replace the example passwords and `JWT_SECRET` with strong random values, then run:

```sh
docker compose up -d --build
```

Open [http://localhost:8080](http://localhost:8080). The frontend container serves the SPA and proxies `/api` to the backend. MySQL's named volume persists data. The first startup may take a few minutes while images build and migrations run.

At startup, the API creates the first admin account from `SMS_BOOTSTRAP_ADMIN_EMAIL` and `SMS_BOOTSTRAP_ADMIN_PASSWORD`. Sign in at [http://localhost:8080/login](http://localhost:8080/login). Keep those bootstrap values in `.env`; if the email already has an account, startup does not replace its password.

Admins create student records first, then open **Accounts** and create a student login using the same email. The admin chooses an initial password and shares it directly with the student; the app does not send email. Students sign in and use **My profile** to change their name, phone, date of birth, gender, address, and photo URL. Their email and academic data remain admin-managed. Student accounts cannot browse other student records or use the admin dashboard. Passwords are stored as BCrypt hashes; JWTs expire after eight hours.

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
| `JWT_SECRET` | local-only development value | HMAC JWT signing secret; use at least 32 random bytes in deployment |
| `SMS_BOOTSTRAP_ADMIN_EMAIL` | empty outside Compose | Initial admin account email |
| `SMS_BOOTSTRAP_ADMIN_PASSWORD` | empty outside Compose | Initial admin password, 12–72 characters |
| `BACKEND_ORIGIN` | `http://backend:8081` | API origin proxied by Nginx |

The `.env.example` values are for local development only. Configure production secrets in the hosting provider's secret manager.

## Build and test

```sh
cd backend && mvn verify
cd frontend && npm test -- --watch=false --browsers=ChromeHeadless
npm run build
```

Backend tests cover service rules with Mockito, JWT-to-role authority mapping, REST authorization and validation with MockMvc, and student/account persistence on H2. `mvn verify` enforces at least 80% line coverage for `StudentServiceImpl`. Frontend HTTP service, theme persistence, and form validation tests run with Karma and ChromeHeadless.
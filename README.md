# Community Food Sharing System

A two-application project that helps donors offer safe surplus food and lets volunteers coordinate collection and community distribution. The frontend is static HTML, CSS, Bootstrap and vanilla JavaScript; it talks to a separate Spring Boot REST API. MySQL stores accounts, donations, distribution records and notifications.

## Features

- Donor and volunteer self-registration, BCrypt password hashing, JWT login, stateless authentication and backend-enforced roles.
- Donor listings, live donor dashboard, cancellation of available donations and personal donation history.
- Volunteer availability board, race-safe acceptance, pickup, distribution details and completion workflow.
- Admin user search and activation, volunteer directory, donation status filters, dashboard statistics and impact reports.
- Per-user notifications and editable account contact details.
- Jakarta validation, JSON API errors, configurable CORS and a Java 21 multi-stage Docker build.

## Technology

- Frontend: HTML5, CSS3, vanilla JavaScript, Bootstrap 5, Bootstrap Icons and Fetch API.
- Backend: Java 21, Spring Boot 3.4, Spring Web, Spring Data JPA, Spring Security, Jakarta Validation and Maven.
- Authentication: signed JWT bearer tokens, BCrypt and stateless Spring Security.
- Database: MySQL 8 or compatible MySQL server.

## Project structure

```text
community-food-sharing-system/
├── frontend/                 # Independent static web application
├── backend/                  # Spring Boot REST application
├── .gitignore
└── README.md
```

The frontend is not served by Spring Boot and contains no server templates. Its API origin is configured in `frontend/js/config.js`.

## Prerequisites

- Java 21 JDK
- Apache Maven 3.9 or newer (`mvn` on PATH)
- MySQL 8 running locally
- VS Code Live Server extension, or another static HTTP server

Check the tools:

```powershell
java -version
mvn -version
mysql --version
```

## Database setup

Create the database and a dedicated local account. The default configuration uses `root` with an empty password; set `DB_USERNAME` and `DB_PASSWORD` if your local installation differs.

```sql
CREATE DATABASE community_food_sharing CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Spring Data JPA creates and updates the tables (`users`, `food_donations`, `distributions`, and `notifications`) on startup. For a deployed environment, replace `spring.jpa.hibernate.ddl-auto=update` with a migration strategy such as Flyway before production use.

## Backend setup

From the project root, set a JWT signing secret of at least 32 characters. This PowerShell command creates a fresh secret for the current terminal session:

```powershell
$env:JWT_SECRET = [Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(48))
```

Set database credentials when needed:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/community_food_sharing?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your-local-mysql-password"
```

Create the first administrator by setting credentials before starting the backend. The account is inserted only when that email is not already present. The password must be at least 12 characters; it is BCrypt-hashed before storage.

```powershell
$env:ADMIN_EMAIL = "admin@foodshare.com"
$env:ADMIN_PASSWORD = "choose-a-strong-local-password"
$env:ADMIN_NAME = "Food Share Administrator"
$env:ADMIN_PHONE = "+15550101000"
```

Build and run:

```powershell
cd backend
mvn clean package
mvn spring-boot:run
```

The API runs at `http://localhost:8080`. Check it with `http://localhost:8080/api/public/health`.

## Frontend setup

Open the `frontend` folder in VS Code and use **Live Server: Open with Live Server** on `index.html`. The default URL is `http://127.0.0.1:5500`. Alternatively, from the project root:

```powershell
py -m http.server 5500 --directory frontend
```

Then open `http://127.0.0.1:5500`. Add any custom frontend origin to `CORS_ALLOWED_ORIGINS` on the backend. The defaults include `localhost` and `127.0.0.1` on ports 5500 and 5501.

## Authentication and authorization

Login returns a signed JWT, role and display name. The frontend stores the token in `localStorage` for this college-project demo and sends it as `Authorization: Bearer <token>`. Use HTTPS and an HttpOnly, Secure, SameSite cookie-based design instead of browser storage for a production deployment. The backend validates token signature and expiry on every protected request, loads the account from MySQL, and enforces its persisted role. Frontend role checks are only navigation convenience and are not the security boundary. Admin accounts cannot self-register.

Donation acceptance locks the donation row while checking and assigning it, so concurrent volunteers cannot both accept one donation. The assigned volunteer alone can advance it through `ACCEPTED → PICKED_UP → DISTRIBUTED → COMPLETED`; invalid transitions return HTTP 409. Donors can only fetch their own donations.

## Environment variables

| Variable | Default | Purpose |
| --- | --- | --- |
| `PORT` | `8080` | HTTP port |
| `DB_URL` | local MySQL URL | JDBC connection URL |
| `DB_USERNAME` | `root` | Database user |
| `DB_PASSWORD` | empty | Database password |
| `JWT_SECRET` | required | Signing secret, at least 32 bytes |
| `JWT_EXPIRATION` | `86400000` | Token lifetime in milliseconds |
| `CORS_ALLOWED_ORIGINS` | localhost/127.0.0.1 ports 5500, 5501 | Comma-separated browser origins |
| `ADMIN_EMAIL` | unset | Optional initial administrator email |
| `ADMIN_PASSWORD` | unset | Optional initial administrator password (12+ chars) |
| `ADMIN_NAME` | `System Administrator` | Initial administrator name |
| `ADMIN_PHONE` | empty | Initial administrator phone |
| `SHOW_SQL` | `false` | Log generated SQL when set to `true` |

Do not commit real passwords, JWT secrets or a populated `.env` file.

## Main API endpoints

All endpoints are rooted at `http://localhost:8080/api`. Except registration, login and `/public/health`, calls require a bearer token. Roles are enforced by Spring Security.

| Method | Endpoint | Access |
| --- | --- | --- |
| `POST` | `/auth/register` | Public; DONOR or VOLUNTEER only |
| `POST` | `/auth/login` | Public |
| `GET` | `/public/health` | Public |
| `GET` | `/profile` | Any authenticated user |
| `PUT` | `/profile` | Any authenticated user |
| `GET` | `/donor/dashboard` | DONOR |
| `POST` | `/donor/donations` | DONOR |
| `GET` | `/donor/donations` | DONOR; own donations only |
| `GET` | `/donor/donations/{id}` | DONOR; own donation only |
| `PUT` | `/donor/donations/{id}/cancel` | DONOR; own available donation |
| `GET` | `/volunteer/dashboard` | VOLUNTEER |
| `GET` | `/volunteer/donations/available` | VOLUNTEER |
| `GET` | `/volunteer/donations/my-pickups` | VOLUNTEER |
| `GET` | `/volunteer/donations/completed` | VOLUNTEER |
| `PUT` | `/volunteer/donations/{id}/accept` | VOLUNTEER |
| `PUT` | `/volunteer/donations/{id}/pickup` | Assigned VOLUNTEER |
| `PUT` | `/volunteer/donations/{id}/distribute` | Assigned VOLUNTEER |
| `PUT` | `/volunteer/donations/{id}/complete` | Assigned VOLUNTEER |
| `GET` | `/admin/dashboard` | ADMIN |
| `GET` | `/admin/users?role=VOLUNTEER&q=name` | ADMIN |
| `PUT` | `/admin/users/{id}/status` | ADMIN; JSON `{"active":false}` |
| `GET` | `/admin/donations?status=COMPLETED` | ADMIN; omit status for all |
| `GET` | `/admin/donations/{id}` | ADMIN |
| `GET` | `/notifications` | Any authenticated user; own only |
| `PUT` | `/notifications/{id}/read` | Any authenticated user; own only |

Errors use JSON with `status`, `message` and `timestamp`. Common statuses include `400` validation error, `401` missing/invalid token, `403` wrong role, `404` missing resource and `409` invalid transition or duplicate email.

## Test the main workflow

1. Start MySQL and the backend with `JWT_SECRET` set.
2. Open the frontend and register one donor and one volunteer.
3. Sign in as donor, create a donation with a future expiry and pickup date, and confirm it appears as `AVAILABLE`.
4. Sign in as volunteer, accept it, mark it picked up, enter distribution details and complete it.
5. Sign in as donor and check the completed listing; sign in as admin and check its distribution record and people-served total.
6. Try a wrong-role API call, a second acceptance attempt and an invalid status transition; expect `403` or `409`.

Login JSON is `{"email":"person@example.com","password":"..."}`. Registration JSON includes `fullName`, `email`, `phone`, `password` and a `role` of `DONOR` or `VOLUNTEER`. Never send donor or volunteer IDs when creating or accepting a donation; the backend resolves identity from the token.

## Docker

Build from the project root:

```powershell
docker build -t community-food-sharing-api ./backend
```

Run it on a Docker network with a reachable MySQL service. Supply secrets at runtime, not in the image:

```powershell
docker run --rm -p 8080:8080 `
  -e PORT=8080 `
  -e DB_URL="jdbc:mysql://host.docker.internal:3306/community_food_sharing?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" `
  -e DB_USERNAME=root `
  -e DB_PASSWORD="your-local-mysql-password" `
  -e JWT_SECRET="replace-with-a-random-secret-of-at-least-32-characters" `
  -e ADMIN_EMAIL="admin@foodshare.com" `
  -e ADMIN_PASSWORD="choose-a-strong-local-password" `
  community-food-sharing-api
```

## Deployment notes

Deploy the backend as a Java 21 container and MySQL as a separate managed service. Provide the environment variables through the platform's secret manager, set the production frontend origin in `CORS_ALLOWED_ORIGINS`, and serve the `frontend` folder from a static hosting provider over HTTPS. Use database migrations, backups, HTTPS, secure token storage, rate limiting and operational monitoring before handling real community data.
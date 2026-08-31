# Restaurant Ordering System

A full-stack restaurant ordering application built with **Spring Boot 4.1.1**, **Angular 22**, **PostgreSQL** and **ActiveMQ Artemis**.

## Features

- JWT authentication with role-based access control
- Roles: `ADMIN`, `WAITER`, `CHEF`, `CUSTOMER`
- Public customer registration
- Responsive restaurant menu with images, search and category filters
- Shopping cart with quantity controls and order placement
- **Maximum 5 units of each meal per order**, enforced in both the Angular cart and backend validation
- Customer order history and live status visibility
- Role-specific order workflow and cancellation rules
- JMS order events through ActiveMQ Artemis
- XML receipt export for individual orders
- Administrator XML restaurant report export
- **Human-readable XML reader**: receipt/report XML is parsed in Angular and shown as normal receipt/report UI
- Admin dashboard with statistics plus meals, categories, users and orders management
- PostgreSQL persistence with JPA/Hibernate
- Docker Compose full-stack environment
- Backend unit tests, Angular tests, JavaDoc, build and API smoke tests in CI
- Environment-based secret handling with Git-ignored local credentials
- Security hardening for dependency and Docker findings discovered by Trivy

## Role permissions

| Role | Permissions |
| --- | --- |
| Customer | Register, log in, browse/search/filter menu, create orders with up to 5 units of each meal, view own orders, cancel own order before `READY`, view readable receipt, download XML receipt |
| Chef | View restaurant orders, `NEW -> PREPARING -> READY` |
| Waiter | View restaurant orders, cancel before `READY`, `READY -> SERVED -> PAID` |
| Admin | Full management access, valid order transitions, readable XML report, XML report download |

Valid order lifecycle:

```text
NEW -> PREPARING -> READY -> SERVED -> PAID
```

`NEW` and `PREPARING` may also be cancelled where allowed by backend role and workflow rules.

## Order quantity rule

Each order line must contain between **1 and 5 units** of a meal.

The rule is enforced at two levels:

- **Frontend:** `CartService` stops `Add to Order` and `+` from increasing an item beyond 5, and the controls are disabled when the item reaches the limit.
- **Backend:** `OrderItemRequest` uses Bean Validation with `@Min(1)` and `@Max(5)`, so a manually crafted REST request such as `quantity: 6` is rejected even if the frontend is bypassed.

This is intentional defense in depth: frontend validation improves usability, while backend validation is the authoritative protection.

## Technology stack

### Backend

- Java 21
- Spring Boot 4.1.1
- Spring MVC / REST Controllers
- Spring Data JPA / Hibernate
- Spring Security + OAuth2 Resource Server JWT
- Jakarta Bean Validation
- Spring JMS
- ActiveMQ Artemis
- PostgreSQL
- Maven
- JUnit 5 / Mockito

### Frontend

- Angular 22
- TypeScript
- Angular signals
- Angular Router
- Angular HTTP client/interceptor
- DOMParser for client-side XML reading
- Vitest / Angular testing utilities
- Nginx unprivileged image for the production Docker runtime

### Course technologies demonstrated

1. **REST Web Services** - Angular calls Spring MVC endpoints using HTTP methods, JSON payloads, status codes and XML responses.
2. **JPA / Hibernate** - entities, repositories and relationships persist restaurant data in PostgreSQL.
3. **JMS** - Spring JMS publishes order events through ActiveMQ Artemis and a listener consumes them asynchronously.

The project also uses **XML** as an additional course-related format. The backend generates XML receipts/reports, and the Angular frontend reads those XML documents and displays them as normal receipt/report screens.

## Quick start - one command

Requirements: Docker Desktop with Docker Compose and `openssl`.

```bash
bash start.sh
```

On first run, `start.sh` creates a local `.env` file containing randomly generated PostgreSQL, ActiveMQ Artemis, JWT and demo-account passwords. The file is ignored by Git and must never be committed.

Open:

```text
Frontend: http://localhost:4200
Backend:  http://localhost:8080
Artemis:  http://localhost:8161
```

Stop the full stack:

```bash
bash stop.sh
```

## Development accounts

Demo accounts are enabled only when `APP_SEED_DATA=true`. Their passwords come from the local `.env` file, not from source code.

| Role | Email | Password source |
| --- | --- | --- |
| Admin | `admin@restaurant.com` | `SEED_ADMIN_PASSWORD` |
| Waiter | `waiter1@restaurant.com` | `SEED_WAITER_PASSWORD` |
| Chef | `chef1@restaurant.com` | `SEED_CHEF_PASSWORD` |
| Customer | `customer1@restaurant.com` | `SEED_CUSTOMER_PASSWORD` |

To see the current customer password locally:

```bash
grep SEED_CUSTOMER_PASSWORD .env
```

Old fixed demo passwords such as `Customer123` are intentionally no longer used.

## Important API endpoints

### Authentication

```text
POST /api/auth/login
POST /api/auth/register
GET  /api/auth/me
```

### Orders

```text
GET    /api/orders
GET    /api/orders/{id}
POST   /api/orders
PATCH  /api/orders/{id}/status?value=PREPARING
DELETE /api/orders/{id}
```

Example order item request:

```json
{
  "mealId": 5,
  "quantity": 3
}
```

`quantity` must be from `1` through `5`.

### XML endpoints

```text
GET /api/orders/{id}/receipt.xml
GET /api/orders/report.xml
```

The endpoints still return XML. The frontend provides two choices:

- **View Receipt / View XML Report** - Angular fetches the XML as a Blob, parses it with DOMParser, and displays a readable receipt/report.
- **Download XML / Download XML Report** - downloads the raw XML file.

## Human-readable XML reader

The XML reader flow is:

```text
Spring Boot generates XML
        ↓
Angular receives XML Blob
        ↓
DOMParser parses the XML
        ↓
Angular extracts values
        ↓
User sees a normal receipt/report UI
```

This lets a non-technical user read the receipt or report without seeing XML tags, while still proving that XML is generated and consumed by the application.

## Project structure

```text
restaurant-ordering-system/
├── .github/workflows/ci.yml
├── .env.example
├── src/main/java/                Spring Boot application
├── src/test/java/                Backend tests
├── frontend/                     Angular application
├── Dockerfile                    Backend image
├── compose.yaml                  Full Docker stack
├── start.sh
├── stop.sh
├── TESTING.md
├── PRESENTATION.md
├── pom.xml
└── README.md
```

## Security and secret handling

- No committed database password is required by the application.
- No committed ActiveMQ Artemis password is required by the application.
- No committed JWT signing secret is used.
- Demo-account passwords are supplied through environment variables.
- `.env` and `.env.*` are ignored by Git; `.env.example` is safe to commit.
- `start.sh` generates random local development secrets with `openssl`.
- GitHub Actions uses temporary/per-run test credentials.
- Historical secret-scanner alerts may still reference old commits even after the current source is cleaned.

## Trivy security scan

Install Trivy on macOS:

```bash
brew install trivy
```

Run:

```bash
trivy fs \
  --scanners vuln,secret,misconfig \
  --severity HIGH,CRITICAL \
  .
```

Use the latest local scan output as the source of truth before claiming the final security status.

## Tests

Backend:

```bash
./mvnw clean test
./mvnw javadoc:javadoc
```

Frontend:

```bash
cd frontend
npm ci
npm audit
npm test -- --watch=false
npm run build
cd ..
```

The cart tests verify that repeated `add()` and `increase()` operations stop at quantity 5. Backend request validation independently rejects quantities greater than 5.

GitHub Actions runs backend tests, JavaDoc generation, frontend tests, Angular production build and a backend smoke test against PostgreSQL and ActiveMQ Artemis.

## Presentation guide

The recommended presentation flow is documented in `PRESENTATION.md`: application screens, edge cases, code flow, course technologies, lessons learned, difficulties and future improvements. The quantity-limit edge case is included so it can be demonstrated directly during the presentation.

## Docker images

Build backend image manually:

```bash
docker build -t restaurant-backend .
```

Build frontend image manually:

```bash
docker build -t restaurant-frontend ./frontend
```

Both runtime sides follow non-root execution practices: the backend runs as a non-root user and the frontend uses the Nginx unprivileged runtime image on internal port `8080`.

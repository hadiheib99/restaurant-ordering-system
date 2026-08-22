# Restaurant Ordering System

A full-stack restaurant ordering application built with Spring Boot, Angular, PostgreSQL and ActiveMQ Artemis.

## Features

- JWT authentication with role-based access control
- Roles: `ADMIN`, `WAITER`, `CHEF`, `CUSTOMER`
- Public customer registration
- Responsive restaurant menu with meal images, search and category filters
- Shopping cart with quantity controls and order placement
- Customer order history and live status visibility
- Role-specific order workflow
- JMS order events through ActiveMQ Artemis
- Admin dashboard with restaurant statistics plus meals, categories, users and orders management
- PostgreSQL persistence with JPA/Hibernate
- Dockerized backend and frontend
- Docker Compose full-stack environment
- Backend unit tests, Angular tests and API smoke tests in CI

## Role permissions

| Role | Permissions |
| --- | --- |
| Customer | Register, log in, browse/search/filter menu, create orders, view own orders |
| Chef | View restaurant orders, `NEW -> PREPARING -> READY` |
| Waiter | View restaurant orders, `READY -> SERVED -> PAID` |
| Admin | Full management access and all valid order status transitions |

Valid order lifecycle:

```text
NEW -> PREPARING -> READY -> SERVED -> PAID
```

`NEW` and `PREPARING` may also be cancelled where allowed by the backend workflow.

## Technology stack

### Backend

- Java 21
- Spring Boot 4.1
- Spring MVC
- Spring Data JPA / Hibernate
- Spring Security
- OAuth2 Resource Server JWT
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
- Vitest / Angular testing utilities
- Nginx for the production Docker image

## Project structure

```text
restaurant-ordering-system/
├── .github/workflows/ci.yml
├── src/main/java/                 Spring Boot application
├── src/test/java/                 Backend tests
├── frontend/                      Angular application
│   ├── src/app/
│   ├── public/images/             Local UI image assets
│   ├── Dockerfile
│   └── nginx.conf
├── Dockerfile                     Backend image
├── compose.yaml                   Full Docker stack
├── start.sh                       One-command full-stack startup
├── stop.sh                        One-command shutdown
├── TESTING.md                     Testing guide
├── PRESENTATION.md                Teacher/presentation explanation guide
├── pom.xml
└── README.md
```

## Quick start — one command

Requirements: Docker Desktop with Docker Compose.

From the project root:

```bash
bash start.sh
```

The script builds and starts PostgreSQL, ActiveMQ Artemis, Spring Boot and the Angular/Nginx frontend, then waits until the application is reachable.

Open:

```text
Frontend: http://localhost:4200
Backend:  http://localhost:8080
Artemis:  http://localhost:8161
```

Stop the complete project with:

```bash
bash stop.sh
```

You can also use Docker Compose directly:

```bash
docker compose up --build -d
docker compose ps
docker compose down
```

Delete the PostgreSQL volume as well only when you intentionally want a fresh database:

```bash
docker compose down -v
```

## Local development

### Requirements

- Java 21
- Docker Desktop
- Node.js 24
- npm

### Infrastructure only

If you want to run Spring Boot and Angular directly from your machine, start only PostgreSQL and Artemis:

```bash
docker compose up -d postgres artemis
```

### Backend

```bash
./mvnw spring-boot:run
```

Backend URL:

```text
http://localhost:8080
```

### Frontend

In another terminal:

```bash
cd frontend
npm ci
npm start
```

Frontend URL:

```text
http://localhost:4200
```

## Development accounts

When `APP_SEED_DATA=true` (default), missing development users are created automatically.

| Role | Email | Password |
| --- | --- | --- |
| Admin | `admin@restaurant.com` | `Admin123` |
| Waiter | `waiter1@restaurant.com` | `Waiter123` |
| Chef | `chef1@restaurant.com` | `Chef123` |
| Customer | `customer1@restaurant.com` | `Customer123` |

Existing users are never overwritten. If an account already exists in your local database, its existing password remains unchanged.

## Customer registration

A new customer can open `/register` and provide:

- First name
- Last name
- Username
- Email
- Phone
- Password

Self-registration always creates a `CUSTOMER` account. After successful registration, the returned JWT is stored by the Angular application and the customer is redirected to the menu.

## Important API endpoints

### Authentication

```text
POST /api/auth/login
POST /api/auth/register
GET  /api/auth/me
```

### Meals

```text
GET    /api/meals
POST   /api/meals
PUT    /api/meals/{id}
DELETE /api/meals/{id}
```

### Categories

```text
GET    /api/categories
POST   /api/categories
PUT    /api/categories/{id}
DELETE /api/categories/{id}
```

### Users

```text
GET    /api/users
POST   /api/users
PUT    /api/users/{id}
PATCH  /api/users/{id}/enabled?value=true
DELETE /api/users/{id}
```

### Orders

```text
GET    /api/orders
POST   /api/orders
PATCH  /api/orders/{id}/status?value=PREPARING
DELETE /api/orders/{id}
```

Protected endpoints use:

```text
Authorization: Bearer <JWT>
```

## Messaging

When an order is created or its status changes, Spring Boot publishes an `OrderMessage` through ActiveMQ Artemis. The kitchen listener consumes the message and logs the order event.

Default Artemis development credentials:

```text
admin / admin
```

## Configuration

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/restaurant_db` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `postgres` |
| `ARTEMIS_BROKER_URL` | `tcp://localhost:61616` |
| `ARTEMIS_USER` | `admin` |
| `ARTEMIS_PASSWORD` | `admin` |
| `JWT_ISSUER` | `restaurant-ordering-system` |
| `JWT_SECRET` | development secret |
| `JWT_EXPIRATION_MINUTES` | `60` |
| `APP_SEED_DATA` | `true` |

Use a strong unique `JWT_SECRET` and non-development credentials for deployment.

## Tests

For a complete explanation of the test suite and presentation workflow, see **`TESTING.md`**.

### Backend unit tests

```bash
./mvnw test
```

The backend suite covers authentication, category, meal, user and order-workflow behavior, including role-specific permissions.

### Frontend tests

```bash
cd frontend
npm test -- --watch=false
```

Frontend tests cover core cart behavior, authentication/JWT behavior and API services.

### Production frontend build

```bash
cd frontend
npm run build
```

## Continuous integration

GitHub Actions runs on pull requests and pushes to `master`. The pipeline executes:

1. Backend unit tests
2. Frontend unit tests
3. Angular production build
4. End-to-end backend smoke test against PostgreSQL and ActiveMQ Artemis

The smoke test starts the application, authenticates a seeded customer and creates a real order through the REST API.

## Presentation / teacher guide

A structured explanation of the architecture, JWT flow, database model, JMS usage, role permissions, demo sequence and common presentation questions is included in:

```text
PRESENTATION.md
```

This is the recommended document to review before presenting the project.

## Docker images

Build the backend image manually:

```bash
docker build -t restaurant-backend .
```

Build the frontend image manually:

```bash
docker build -t restaurant-frontend ./frontend
```

The provided multi-stage Dockerfiles keep build tools out of the final runtime images. The backend runs as a non-root user and the frontend production build is served through Nginx.

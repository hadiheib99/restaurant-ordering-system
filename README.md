# Restaurant Ordering System

Full-stack restaurant ordering application built with Spring Boot, Angular, PostgreSQL and ActiveMQ Artemis.

## Features

- JWT authentication with role-based access control
- Roles: `ADMIN`, `WAITER`, `CHEF`, `CUSTOMER`
- Public restaurant menu API
- Customer Angular menu and shopping cart
- Customer order placement using the logged-in account
- Customer order history
- Waiter/chef/admin order workflow
- Order status transitions: `NEW -> PREPARING -> READY -> SERVED -> PAID`
- JMS order events through ActiveMQ Artemis
- Admin dashboard
- Admin meal CRUD and availability management
- Admin category CRUD
- Admin user CRUD, role changes and enable/disable controls
- PostgreSQL persistence with JPA/Hibernate
- Docker Compose setup for PostgreSQL and Artemis
- CI build checks for Spring Boot and Angular

## Technology stack

### Backend

- Java 21
- Spring Boot 4.1
- Spring MVC
- Spring Data JPA / Hibernate
- Spring Security
- OAuth2 Resource Server JWT support
- Spring JMS
- ActiveMQ Artemis
- PostgreSQL
- Maven

### Frontend

- Angular 22
- TypeScript
- Angular signals
- Angular Router
- Angular HTTP client and interceptor

## Project structure

```text
restaurant-ordering-system/
├── src/                         Spring Boot backend
├── frontend/                    Angular frontend
├── compose.yaml                 PostgreSQL + Artemis
├── pom.xml
└── README.md
```

## Quick start

### Requirements

Install:

- Java 21
- Docker Desktop
- Node.js 24
- npm

Angular CLI can be run through the locally installed frontend dependencies with `npx ng` if a global `ng` command is unavailable.

### 1. Start PostgreSQL and Artemis

From the project root:

```bash
docker compose up -d
```

Check the containers:

```bash
docker compose ps
```

Services:

- PostgreSQL: `localhost:5432`
- ActiveMQ Artemis broker: `localhost:61616`
- Artemis web console: `http://localhost:8161`

Development Artemis credentials:

```text
admin / admin
```

### 2. Start the Spring Boot backend

```bash
./mvnw spring-boot:run
```

Backend URL:

```text
http://localhost:8080
```

### 3. Start Angular

In another terminal:

```bash
cd frontend
npm install
npm start
```

Frontend URL:

```text
http://localhost:4200
```

## Development accounts

When `APP_SEED_DATA=true` (the default), missing development accounts are created automatically.

| Role | Email | Password |
| --- | --- | --- |
| Admin | `admin@restaurant.com` | `Admin123` |
| Waiter | `waiter1@restaurant.com` | `Waiter123` |
| Chef | `chef1@restaurant.com` | `Chef123` |
| Customer | `customer1@restaurant.com` | `Customer123` |

Existing users are never overwritten by the seed initializer. If your database already contains an account with one of these emails/usernames, its current password remains unchanged.

To disable development seed data:

```bash
export APP_SEED_DATA=false
```

## Role workflows

### Customer

1. Log in.
2. Browse `/menu`.
3. Add meals to the cart.
4. Place an order.
5. View personal orders at `/orders`.

Customers can only create and view their own orders.

### Waiter / Chef

After login they are sent to `/orders`, where restaurant orders can be viewed and progressed through their valid statuses.

### Admin

After login the admin is sent to `/admin` and can manage:

- Meals
- Categories
- Users
- Orders

## Important API endpoints

### Authentication

```text
POST /api/auth/login
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

Protected API calls use:

```text
Authorization: Bearer <JWT>
```

The Angular HTTP interceptor automatically attaches the stored token to protected requests. The login endpoint intentionally does not receive an old JWT.

## Order messaging

When an order is created or its status changes, Spring Boot publishes an `OrderMessage` to the Artemis kitchen queue. The kitchen listener consumes the event and logs the order information.

Default broker configuration:

```text
tcp://localhost:61616
```

## Configuration

The backend supports environment overrides:

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

For a deployed environment, set a strong unique `JWT_SECRET` and do not use the development database/broker credentials.

## Build checks

Backend compile:

```bash
./mvnw -DskipTests compile
```

Frontend production build:

```bash
cd frontend
npm ci
npm run build
```

The GitHub Actions workflow runs both checks for pull requests and pushes to `master`.

## Stop local infrastructure

```bash
docker compose down
```

To also delete the local PostgreSQL volume:

```bash
docker compose down -v
```

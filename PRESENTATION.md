# Project Presentation Guide

Use this guide to explain the Restaurant Ordering System clearly in a university presentation.

## 1. One-minute introduction

> My project is a full-stack Restaurant Ordering System. The backend is built with Spring Boot and Java, the frontend is Angular, PostgreSQL stores the data, and ActiveMQ Artemis is used for asynchronous order events. The system supports four roles: customer, chef, waiter and admin. Each role has different permissions, and JWT authentication protects the API.

## 2. The problem the system solves

The application models the main workflow of a restaurant:

- Customers browse a menu and place orders.
- Chefs see incoming orders and prepare them.
- Waiters serve completed orders and mark payment.
- Admins manage the menu, categories, users and orders.

The main order lifecycle is:

```text
NEW -> PREPARING -> READY -> SERVED -> PAID
```

## 3. Architecture

Explain the project as four main parts:

```text
Angular Frontend
      |
      | HTTP + JWT
      v
Spring Boot REST API
      |
      +---- PostgreSQL (persistent data)
      |
      +---- ActiveMQ Artemis (order events)
```

### Frontend

Angular provides the screens, routing, authentication state, menu filtering, shopping cart, order pages and admin dashboard.

### Backend

Spring Boot exposes REST endpoints, performs validation and business logic, enforces role permissions, issues and validates JWTs, stores data with JPA/Hibernate, and publishes JMS events.

### Database

PostgreSQL stores users, categories, meals, orders and order items with relationships between the entities.

### Messaging

When an order is created or its status changes, the backend publishes an order event to ActiveMQ Artemis. A listener consumes the event. This demonstrates asynchronous messaging in addition to normal REST communication.

## 4. Role permissions

| Role | Main permissions |
| --- | --- |
| CUSTOMER | Register/login, browse/search/filter menu, create orders, see own order history/status |
| CHEF | See restaurant orders and move `NEW -> PREPARING -> READY` |
| WAITER | See restaurant orders and move `READY -> SERVED -> PAID` |
| ADMIN | Full valid order workflow plus users, categories and meals management |

Emphasize that permissions are enforced in the backend, not only by hiding frontend buttons.

## 5. JWT authentication

Explain it in simple steps:

1. The user sends email and password to `/api/auth/login`.
2. Spring Security verifies the credentials.
3. The server returns a signed JWT containing the user's identity/role.
4. Angular stores the token.
5. An HTTP interceptor sends `Authorization: Bearer <token>` with protected API requests.
6. Spring Security validates the JWT and applies role authorization.

## 6. Database model

Important entities:

- `User`
- `Category`
- `Meal`
- `RestaurantOrder`
- `OrderItem`

Relationships to mention:

- A category contains many meals.
- An order belongs to a customer.
- An order can be assigned to a waiter.
- An order contains multiple order items.
- Each order item references one meal and stores quantity/unit price/subtotal.

## 7. Demo order

A good presentation sequence is:

1. Start everything with `bash start.sh`.
2. Log in as a customer.
3. Show menu images, search and category filters.
4. Add two meals to the cart and place an order.
5. Show the customer's order as `NEW`.
6. Log in as chef and move it to `PREPARING`, then `READY`.
7. Log in as waiter and move it to `SERVED`, then `PAID`.
8. Log in as customer again and show the final status.
9. Log in as admin and show dashboard statistics and management pages.

This demo proves authentication, authorization, REST API communication, database persistence, order business logic and UI behavior.

## 8. Tests

Say:

> I added automated tests on both backend and frontend, and GitHub Actions runs them automatically. The pipeline also builds the production Angular application and runs a backend smoke test with PostgreSQL and ActiveMQ Artemis.

Show:

- `src/test/java/` for backend JUnit/Mockito tests
- Angular `*.spec.ts` tests
- `.github/workflows/ci.yml`
- A successful run in the GitHub **Actions** tab

For exact commands, see `TESTING.md`.

## 9. Docker

The complete stack can be started with:

```bash
bash start.sh
```

This uses Docker Compose to run:

- PostgreSQL
- ActiveMQ Artemis
- Spring Boot backend
- Angular/Nginx frontend

Stop it with:

```bash
bash stop.sh
```

Explain that Docker makes the environment reproducible for another developer or the instructor.

## 10. Questions you may be asked

### Why did you use JWT?

Because the frontend and backend are separate applications. JWT provides stateless authentication for REST requests and allows role information to be applied to protected endpoints.

### Why did you use JMS/Artemis if REST already works?

REST handles direct client/server requests. JMS demonstrates asynchronous messaging: the order can be published as an event without requiring the sender to synchronously call every consumer.

### Why PostgreSQL?

The application has relational data with users, meals, categories, orders and order items, so a relational database and JPA mappings fit the domain well.

### Why Angular signals?

They provide simple reactive state for menu data, cart-related UI state, filtering and loading/error states.

### How are permissions protected?

They are checked on the backend using Spring Security and the authenticated role. The frontend also adjusts the UI for usability, but the backend remains the security boundary.

### What would you add in a production version?

Possible extensions include password reset/email verification, payment-provider integration, migrations with Flyway/Liquibase, production secrets management, hosted image storage, monitoring and a cloud deployment.

## 11. Strong closing sentence

> The project demonstrates a complete multi-role workflow across an Angular frontend, secured Spring Boot backend, relational database and asynchronous messaging system, with automated tests, CI and Dockerized deployment.

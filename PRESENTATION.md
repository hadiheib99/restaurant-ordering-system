# Project Presentation Guide

Use this guide to explain the Restaurant Ordering System clearly in a university presentation.

## 1. One-minute introduction

> My project is a full-stack Restaurant Ordering System. The backend is built with Spring Boot 4.1.1 and Java, the frontend is Angular, PostgreSQL stores the data, and ActiveMQ Artemis is used for asynchronous order events. The system supports four roles: customer, chef, waiter and admin. The project demonstrates REST Web Services, JPA/Hibernate and JMS from the course. XML is used for receipts and reports, and the latest version also includes a human-readable XML reader so users can view XML data inside the application without seeing raw XML code.

## 2. Application demo order

Show the project by screens and roles first.

### Customer flow

1. Log in as customer using the password printed by `bash start.sh`.
2. Show Menu with images, search and category filters.
3. Add meals to the cart and change quantities.
4. Place an order.
5. Show the success message and the order in `My Orders`.
6. Click **View Receipt** to show the readable receipt parsed from XML.
7. Optionally click **Download XML** to show that the raw XML is still available.

### Chef flow

1. Log in as chef.
2. Show the order list.
3. Move the order `NEW -> PREPARING`.
4. Move the order `PREPARING -> READY`.

### Waiter flow

1. Log in as waiter.
2. Move the order `READY -> SERVED`.
3. Move the order `SERVED -> PAID`.

### Admin flow

1. Log in as admin.
2. Show Dashboard statistics.
3. Show Meals, Categories, Users and Orders management.
4. Click **View XML Report** to show the readable report parsed from XML.
5. Optionally click **Download XML Report**.

This demo proves authentication, authorization, REST communication, JPA persistence, JMS messaging, XML export/reading and the business workflow.

## 3. Edge cases to demonstrate

Prepare these cases because the instructor may try to break the system:

- Login with wrong password.
- Login with non-existing account.
- Register with missing fields.
- Register with duplicate email.
- Register with duplicate username.
- Customer trying to access `/admin`.
- Customer trying to cancel an order after `READY`.
- Chef trying to do waiter-only actions.
- Waiter trying to jump from `NEW` directly to `PAID`.
- Ordering unavailable meal or invalid quantity.
- Admin trying to create a duplicate category.

Good sentence to say:

> The frontend hides invalid actions for usability, but the real protection is in the backend through Spring Security and service-level business rules.

## 4. Architecture

```text
Angular Frontend
      |
      | REST / HTTP + JWT
      v
Spring Boot REST API
      |
      +---- PostgreSQL through JPA/Hibernate
      |
      +---- ActiveMQ Artemis through JMS
      |
      +---- XML receipts/reports
```

Explain the code as layers:

```text
Angular Component
      ↓
Angular Service
      ↓
REST Controller
      ↓
Service Layer
      ↓
Repository
      ↓
PostgreSQL
```

## 5. Technologies from the course

### REST Web Services

Angular communicates with Spring Boot through endpoints such as:

```text
POST  /api/auth/login
GET   /api/meals
POST  /api/orders
PATCH /api/orders/{id}/status?value=READY
GET   /api/orders/{id}/receipt.xml
```

Important files:

```text
OrderController.java
MealController.java
CategoryController.java
UserController.java
AuthController.java
frontend/src/app/core/services/order.ts
```

### JPA / Hibernate

Entities are mapped to PostgreSQL tables:

```text
User
Category
Meal
RestaurantOrder
OrderItem
```

Important point:

> `OrderItem` stores the unit price at order time. If a meal price changes later, old receipts still show the original price.

Important files:

```text
src/main/java/com/restaurant/ordering/model/
src/main/java/com/restaurant/ordering/repository/
```

### JMS / ActiveMQ Artemis

When an order is created or its status changes:

```text
OrderServiceImpl
      ↓
OrderProducer
      ↓
ActiveMQ Artemis Queue
      ↓
KitchenListener
```

Good explanation:

> REST is synchronous request/response communication. JMS is asynchronous messaging through a broker.

Important files:

```text
OrderProducer.java
KitchenListener.java
OrderMessage.java
OrderServiceImpl.java
```

## 6. XML export and XML reader

The backend provides XML endpoints:

```text
GET /api/orders/{id}/receipt.xml
GET /api/orders/report.xml
```

The latest frontend has a reader-friendly XML viewer:

```text
Spring generates XML
        ↓
Angular receives XML Blob
        ↓
DOMParser reads the XML
        ↓
Angular extracts fields
        ↓
User sees a normal receipt/report
```

Use this sentence:

> The system still uses XML as the data format, but the user does not need to understand XML because Angular parses it and displays it as a receipt or report.

Important files:

```text
frontend/src/app/pages/orders/orders.ts
frontend/src/app/pages/orders/orders.html
frontend/src/app/pages/orders/orders.scss
frontend/src/app/core/services/order.ts
src/main/java/com/restaurant/ordering/service/OrderServiceImpl.java
src/main/java/com/restaurant/ordering/controller/OrderController.java
```

## 7. Main code flow: create order

The best code flow to explain is the customer placing an order:

```text
Customer clicks Place Order
      ↓
Angular Menu / Cart
      ↓
OrderService frontend
      ↓
POST /api/orders
      ↓
OrderController
      ↓
OrderServiceImpl
      ↓
UserRepository / MealRepository / OrderRepository
      ↓
PostgreSQL
      ↓
OrderProducer
      ↓
ActiveMQ Artemis
      ↓
KitchenListener
      ↓
OrderResponse returns to Angular
```

In `OrderServiceImpl.createOrder`, explain:

- Find the customer.
- Validate that the user is a customer.
- Make sure a customer creates only their own order.
- Find each meal.
- Check meal availability.
- Capture current unit price.
- Calculate subtotal and total.
- Save the order.
- Publish a JMS order event.
- Return an `OrderResponse` DTO.

## 8. Security flow

Authentication answers: **Who are you?**
Authorization answers: **What are you allowed to do?**

JWT flow:

```text
email + password
      ↓
POST /api/auth/login
      ↓
Spring validates credentials
      ↓
JWT token returned
      ↓
Angular stores token
      ↓
Interceptor sends Authorization: Bearer <token>
      ↓
Backend validates role and permissions
```

Important file:

```text
SecurityConfig.java
```

## 9. Security hardening

Explain briefly:

- `.env` is ignored by Git.
- `start.sh` generates random local credentials.
- Demo passwords are no longer hardcoded.
- GitHub Actions uses temporary run credentials.
- GitGuardian historical alerts can still point to old commits.
- Trivy was used for dependency, secret and Docker/configuration scanning.
- Maven dependency findings were fixed by upgrading Spring Boot to 4.1.1.
- Frontend Docker runtime was moved to unprivileged Nginx on internal port `8080`.

Do not claim a final `0 HIGH / 0 CRITICAL` unless you rerun Trivy after pulling the latest `master`.

## 10. Tests and CI

Say:

> I added automated tests on both backend and frontend, and GitHub Actions runs them automatically. The pipeline also validates JavaDoc, builds the Angular production application and runs a backend smoke test with PostgreSQL and ActiveMQ Artemis.

Show:

```text
src/test/java/
frontend/src/**/*.spec.ts
.github/workflows/ci.yml
```

The frontend tests include XML reader behavior: opening the reader, parsing receipt XML and closing the reader.

## 11. Final verification commands

```bash
git switch master
git pull origin master

./mvnw clean test
./mvnw javadoc:javadoc

cd frontend
npm ci
npm audit
npm test -- --watch=false
npm run build
cd ..

trivy fs \
  --scanners vuln,secret,misconfig \
  --severity HIGH,CRITICAL \
  .

bash stop.sh
bash start.sh
```

## 12. What I learned

Good answer:

> I learned how separate technologies connect into one complete application: Angular, REST, Spring Boot, JWT security, JPA/Hibernate, PostgreSQL, JMS, Docker and CI. I also learned how to generate XML on the backend and parse it on the frontend to present it in a user-friendly way.

## 13. Difficulties

Mention:

- Synchronizing order statuses with roles.
- Understanding authentication versus authorization.
- Integrating JMS with ActiveMQ Artemis.
- Docker networking between containers.
- Keeping demo credentials safe without making the project hard to run.
- Handling dependency vulnerabilities found by Trivy.
- Keeping Angular DTOs and backend DTOs aligned.
- Presenting XML in a way normal users can read.

## 14. Future improvements

- Real-time updates with WebSocket or SSE.
- Real payment integration.
- Password reset.
- Email/SMS notifications.
- Audit log for admin actions.
- Flyway/Liquibase database migrations.
- More end-to-end tests.
- Deployment to a cloud environment.

## 15. Short answers to expected questions

### Which three technologies from the course did you use?

> REST Web Services, JPA/Hibernate and JMS. XML is an additional data-format feature for receipts and reports.

### Is XML a Java EE technology?

> XML itself is a data format, not a Java EE API. In my project it is used for receipts and reports, and Angular also parses it to show a readable view.

### Why use JMS if you already have REST?

> REST is direct request/response communication. JMS is useful for asynchronous events, such as notifying the kitchen when a new order is created.

### Why store `unitPrice` in `OrderItem`?

> To preserve the historical price. If the admin changes a meal price tomorrow, old receipts still show the original price paid at order time.

### Where is the real security?

> In the backend. Angular hides buttons for usability, but Spring Security and service-level checks enforce the actual permissions.

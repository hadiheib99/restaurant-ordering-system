# Project Presentation Guide

Use this guide to explain the Restaurant Ordering System clearly in a university presentation.

## 1. One-minute introduction

> My project is a full-stack Restaurant Ordering System. The backend is built with Spring Boot 4.1.1 and Java, the frontend is Angular, PostgreSQL stores the data, and ActiveMQ Artemis is used for asynchronous order events. The system supports four roles: customer, chef, waiter and admin. The project demonstrates REST Web Services, JPA/Hibernate and JMS from the course. XML is used for receipts and reports, and the latest version also includes a human-readable XML reader so users can view XML data inside the application without seeing raw XML code. I also added validation that limits each meal to a maximum of five units per order, enforced in both the frontend and backend.

## 2. Application demo order

Show the project by screens and roles first.

### Customer flow

1. Log in as customer using the password printed by `bash start.sh`.
2. Show Menu with images, search and category filters.
3. Add meals to the cart and change quantities.
4. Demonstrate the quantity limit: increase one meal to `5` and show that `Add to Order` / `+` can no longer increase it.
5. Place an order.
6. Show the success message and the order in `My Orders`.
7. Click **View Receipt** to show the readable receipt parsed from XML.
8. Optionally click **Download XML** to show that the raw XML is still available.

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

This demo proves authentication, authorization, REST communication, JPA persistence, JMS messaging, XML export/reading, validation and the business workflow.

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
- Ordering unavailable meal.
- Sending invalid quantity below 1.
- Trying to order more than 5 units of the same meal.
- Admin trying to create a duplicate category.

For the maximum-5 case, explain both layers:

```text
Normal user
   ↓
Angular cart prevents quantity > 5

Manual / crafted API request
   ↓
POST /api/orders with quantity: 6
   ↓
OrderItemRequest @Max(5)
   ↓
Backend rejects the request
```

Good sentence to say:

> The frontend prevents invalid actions for usability, but I never rely only on the frontend. The backend applies the same important business validation, so bypassing the Angular UI does not bypass the rule.

## 4. Architecture

```text
Angular Frontend
      |
      | REST / HTTP + JWT
      v
Spring Boot REST API
      |
      +---- Bean Validation / business rules
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
DTO validation
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
CartService (quantity capped at 5)
      ↓
OrderService frontend
      ↓
POST /api/orders
      ↓
OrderController
      ↓
OrderRequest / OrderItemRequest validation
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

In the frontend, show:

```text
frontend/src/app/core/services/cart.ts
frontend/src/app/pages/menu/menu.html
```

Explain that `CartService.add()` and `increase()` stop at 5, and the buttons are disabled when the current quantity reaches the maximum.

In the backend, show:

```text
src/main/java/com/restaurant/ordering/dto/OrderItemRequest.java
```

Explain:

```java
@Min(value = 1, message = "Quantity must be at least 1")
@Max(value = 5, message = "Quantity must not exceed 5")
```

This means the REST API itself rejects quantities outside `1..5`.

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

## 8. Why validate on both frontend and backend?

This is a good question to prepare for.

Frontend validation:

- gives immediate feedback
- disables impossible controls
- improves user experience

Backend validation:

- cannot be trusted to the browser
- protects the API from manually crafted requests
- is the authoritative rule

Good answer:

> I limit the cart to five in Angular for UX, but I also use `@Max(5)` in the backend DTO because a user can bypass the frontend and call the REST API directly. The backend must enforce the real rule.

## 9. Security flow

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

## 10. Security hardening

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

## 11. Tests and CI

Say:

> I added automated tests on both backend and frontend, and GitHub Actions runs them automatically. The pipeline also validates JavaDoc, builds the Angular production application and runs a backend smoke test with PostgreSQL and ActiveMQ Artemis.

Show:

```text
src/test/java/
frontend/src/**/*.spec.ts
.github/workflows/ci.yml
```

Relevant frontend tests include:

- XML reader behavior
- adding/increasing cart quantities
- quantity never going above 5

The backend independently validates order item quantity using Bean Validation.

## 12. Final verification commands

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

Before presentation, manually verify this edge case too:

```text
Add one meal -> 1 -> 2 -> 3 -> 4 -> 5
At 5, the plus/add control is disabled and quantity remains 5.
```

## 13. What I learned

Good answer:

> I learned how separate technologies connect into one complete application: Angular, REST, Spring Boot, JWT security, JPA/Hibernate, PostgreSQL, JMS, Docker and CI. I also learned how to generate XML on the backend and parse it on the frontend to present it in a user-friendly way. Another important lesson was that important validation must exist in the backend even when the frontend already prevents the invalid action.

## 14. Difficulties

Mention:

- Synchronizing order statuses with roles.
- Understanding authentication versus authorization.
- Integrating JMS with ActiveMQ Artemis.
- Docker networking between containers.
- Keeping demo credentials safe without making the project hard to run.
- Handling dependency vulnerabilities found by Trivy.
- Keeping Angular DTOs and backend DTOs aligned.
- Presenting XML in a way normal users can read.
- Keeping business validation consistent between the UI and API.

## 15. Future improvements

- Real-time updates with WebSocket or SSE.
- Real payment integration.
- Password reset.
- Email/SMS notifications.
- Audit log for admin actions.
- Flyway/Liquibase database migrations.
- More end-to-end tests.
- Deployment to a cloud environment.

## 16. Short answers to expected questions

### Which three technologies from the course did you use?

> REST Web Services, JPA/Hibernate and JMS. XML is an additional data-format feature for receipts and reports.

### Is XML a Java EE technology?

> XML itself is a data format, not a Java EE API. In my project it is used for receipts and reports, and Angular also parses it to show a readable view.

### Why use JMS if you already have REST?

> REST is direct request/response communication. JMS is useful for asynchronous events, such as notifying the kitchen when a new order is created.

### Why store `unitPrice` in `OrderItem`?

> To preserve the historical price. If the admin changes a meal price tomorrow, old receipts still show the original price paid at order time.

### Where is the real security?

> In the backend. Angular hides or disables controls for usability, but Spring Security, Bean Validation and service-level checks enforce the actual rules.

### Why limit each meal to 5 in two places?

> Angular enforces it for user experience, but the backend uses `@Max(5)` because the frontend can be bypassed. A direct API request with quantity 6 must still be rejected.

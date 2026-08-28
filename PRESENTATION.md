# Project Presentation Guide

Use this guide to explain the Restaurant Ordering System clearly in a university presentation.

## 1. One-minute introduction

> My project is a full-stack Restaurant Ordering System. The backend is built with Spring Boot and Java, the frontend is Angular, PostgreSQL stores the data, and ActiveMQ Artemis is used for asynchronous order events. The system supports four roles: customer, chef, waiter and admin. Each role has different permissions, and JWT authentication protects the API. The project demonstrates REST Web Services, JPA/Hibernate and JMS from the course, and it also exports receipts and reports as XML.

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

`NEW` and `PREPARING` orders may also be cancelled where allowed by the backend role and workflow rules.

## 3. Architecture

Explain the project as four main parts:

```text
Angular Frontend
      |
      | HTTP + JWT
      v
Spring Boot REST API
      |
      +---- PostgreSQL (persistent data through JPA/Hibernate)
      |
      +---- ActiveMQ Artemis (JMS order events)
      |
      +---- XML receipts/reports returned to the frontend
```

### Frontend

Angular provides the screens, routing, authentication state, menu filtering, shopping cart, order pages, XML downloads and admin dashboard.

### Backend

Spring Boot exposes REST endpoints, performs validation and business logic, enforces role permissions, issues and validates JWTs, stores data with JPA/Hibernate, publishes JMS events and generates XML receipts/reports.

### Database

PostgreSQL stores users, categories, meals, orders and order items with relationships between the entities.

### Messaging

When an order is created or its status changes, the backend publishes an order event to ActiveMQ Artemis. A listener consumes the event. This demonstrates asynchronous messaging in addition to normal REST communication.

## 4. Technologies from the course

The three main course technologies demonstrated by the project are:

### REST Web Services

The Angular frontend communicates with the Spring Boot backend through REST endpoints such as:

```text
POST  /api/auth/login
GET   /api/meals
POST  /api/orders
PATCH /api/orders/{id}/status?value=READY
```

The REST layer is implemented with Spring MVC controllers. Spring is allowed for the course project, so the important idea to explain is the REST client/server communication: HTTP requests, URLs, methods, JSON request/response bodies and status codes.

Important files to show:

```text
src/main/java/com/restaurant/ordering/controller/AuthController.java
src/main/java/com/restaurant/ordering/controller/OrderController.java
src/main/java/com/restaurant/ordering/controller/MealController.java
src/main/java/com/restaurant/ordering/controller/CategoryController.java
src/main/java/com/restaurant/ordering/controller/UserController.java
```

### JPA / Hibernate

JPA/Hibernate maps Java entities to PostgreSQL tables and handles persistence and relationships.

Important entities:

```text
User
Category
Meal
RestaurantOrder
OrderItem
```

Important files to show:

```text
src/main/java/com/restaurant/ordering/model/
src/main/java/com/restaurant/ordering/repository/
```

Examples of relationships to explain:

- A category has many meals.
- An order belongs to a customer.
- An order can have a waiter.
- An order contains multiple order items.
- Each order item references one meal.

### JMS / ActiveMQ Artemis

JMS is used for asynchronous order events. When an order is created or its status changes, `OrderServiceImpl` creates an `OrderMessage`, and `OrderProducer` publishes it to ActiveMQ Artemis. `KitchenListener` consumes the event.

Important files to show:

```text
src/main/java/com/restaurant/ordering/messaging/producer/OrderProducer.java
src/main/java/com/restaurant/ordering/messaging/listener/KitchenListener.java
src/main/java/com/restaurant/ordering/messaging/dto/OrderMessage.java
src/main/java/com/restaurant/ordering/service/OrderServiceImpl.java
```

A good explanation is:

> REST is synchronous client/server communication. JMS is asynchronous messaging. The order operation does not need to directly call the kitchen consumer; it publishes an event through the broker.

## 5. XML receipts and reports

XML is an additional data-format feature in the project. It is not counted as one of the three main Java EE/course technologies above, but it demonstrates the XML material from the course and fulfills the feature described in the project proposal.

The backend provides two XML endpoints:

```text
GET /api/orders/{id}/receipt.xml
GET /api/orders/report.xml
```

### Order receipt XML

A customer or authorized staff member can download an XML receipt for an order. The receipt contains the order status, customer, creation time, individual items, quantity, unit price, subtotal and total price.

Example:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<receipt orderId="15">
    <status>PAID</status>
    <customer>John Smith</customer>
    <items>
        <item>
            <meal>Margherita Pizza</meal>
            <quantity>2</quantity>
            <unitPrice>40.00</unitPrice>
            <subtotal>80.00</subtotal>
        </item>
    </items>
    <totalPrice>80.00</totalPrice>
</receipt>
```

### Administrator report XML

Only an administrator may export the complete restaurant XML report. It contains:

- total order count
- paid revenue
- number of orders in each status
- a compact list of restaurant orders

### Where XML is implemented

The main implementation is in:

```text
src/main/java/com/restaurant/ordering/service/OrderServiceImpl.java
```

Important methods:

```text
exportReceiptXml(...)
exportReportXml()
receiptXml(...)
xmlEscape(...)
```

The REST endpoints are in:

```text
src/main/java/com/restaurant/ordering/controller/OrderController.java
```

The Angular download calls are in:

```text
frontend/src/app/core/services/order.ts
frontend/src/app/pages/orders/orders.ts
```

During the presentation, download one receipt and the administrator report and open one of the XML files to show its structure.

## 6. Role permissions

| Role | Main permissions |
| --- | --- |
| CUSTOMER | Register/login, browse/search/filter menu, create orders, see own order history/status, cancel own order before READY, download own receipt |
| CHEF | See restaurant orders and move `NEW -> PREPARING -> READY` |
| WAITER | See restaurant orders, cancel before READY, and move `READY -> SERVED -> PAID` |
| ADMIN | Full valid order workflow plus users, categories and meals management and XML report export |

Emphasize that permissions are enforced in the backend, not only by hiding frontend buttons.

## 7. JWT authentication

Explain it in simple steps:

1. The user sends email and password to `/api/auth/login`.
2. Spring Security verifies the credentials.
3. The server returns a signed JWT containing the user's identity/role.
4. Angular stores the token.
5. An HTTP interceptor sends `Authorization: Bearer <token>` with protected API requests.
6. Spring Security validates the JWT and applies role authorization.

## 8. Database model

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

A useful detail to explain is that `OrderItem` stores the unit price at the time of the order. This preserves historical receipt prices even if the meal price changes later.

## 9. Recommended live demo

A strong presentation sequence is:

1. Start everything with `bash start.sh`.
2. Log in as a customer.
3. Show menu images, search and category filters.
4. Add two meals to the cart and place an order.
5. Show the success message and the customer's order as `NEW`.
6. Download the order's XML receipt.
7. Log in as chef and move it to `PREPARING`, then `READY`.
8. Log in as waiter and move it to `SERVED`, then `PAID`.
9. Log in as customer again and show the final status.
10. Log in as admin and show dashboard statistics and management pages.
11. Export the administrator XML report and open it.
12. Optionally show the ActiveMQ Artemis console or application log containing a JMS order event.

This demo proves authentication, authorization, REST API communication, JPA/database persistence, JMS messaging, XML export, order business logic and UI behavior.

## 10. Tests

Say:

> I added automated tests on both backend and frontend, and GitHub Actions runs them automatically. The pipeline also validates JavaDoc, builds the production Angular application and runs a backend smoke test with PostgreSQL and ActiveMQ Artemis.

Show:

- `src/test/java/` for backend JUnit/Mockito tests
- Angular `*.spec.ts` tests
- `.github/workflows/ci.yml`
- A successful run in the GitHub **Actions** tab

The backend tests include order role/workflow rules and XML-specific tests:

- customer can export own XML receipt
- admin can export XML report
- non-admin cannot export XML report

For exact commands, see `TESTING.md`.

## 11. JavaDoc and source documentation

The backend source contains JavaDoc for classes, interfaces and methods, including parameters, return values and exceptions where appropriate. Angular uses TSDoc/JSDoc-style source comments.

Generate the backend documentation with:

```bash
./mvnw javadoc:javadoc
```

Then open on macOS:

```bash
open target/reports/apidocs/index.html
```

This is useful to show the instructor because it demonstrates that the API documentation is generated directly from the source comments.

## 12. Docker

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

## 13. Final verification before presentation

Run these commands from a clean project checkout before presenting:

```bash
git pull origin master
./mvnw clean test
./mvnw javadoc:javadoc

cd frontend
npm ci
npm test -- --watch=false
npm run build
cd ..

bash stop.sh
bash start.sh
```

Expected result:

- 32 backend tests pass
- JavaDoc finishes with `BUILD SUCCESS`
- 26 Angular tests pass
- Angular production build completes
- the Dockerized application starts successfully

A non-failing Angular stylesheet budget warning does not prevent the production build from succeeding.

## 14. Questions you may be asked

### Which three technologies from the course did you use?

> REST Web Services, JPA/Hibernate and JMS. REST is implemented with Spring MVC, JPA/Hibernate persists the relational data in PostgreSQL, and JMS with ActiveMQ Artemis handles asynchronous order events. XML is an additional feature for receipts and reports.

### Why did you use Spring for REST?

> The course allows Spring. I used Spring MVC to implement the REST architecture: controllers expose HTTP endpoints, Angular calls them, and the server returns HTTP responses and JSON/XML representations.

### Is XML a Java EE technology?

> XML itself is a data format, not a Java EE API. I use it as an additional course-related feature for receipts and reports. My three main technologies are REST, JPA and JMS.

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

### Where is XML created?

The XML strings are generated in `OrderServiceImpl`. `OrderController` exposes them with the `application/xml` media type, and the Angular order service/page downloads them as files.

### What would you add in a production version?

Possible extensions include password reset/email verification, payment-provider integration, migrations with Flyway/Liquibase, production secrets management, hosted image storage, monitoring and a cloud deployment.

## 15. Strong closing sentence

> The project demonstrates a complete multi-role restaurant workflow across an Angular frontend, secured Spring Boot REST backend, JPA/Hibernate relational persistence, JMS asynchronous messaging and XML exports, with automated tests, CI, generated JavaDoc and Dockerized deployment.

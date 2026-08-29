# Project Presentation Guide

Use this guide to explain the Restaurant Ordering System clearly in a university presentation.

## 1. One-minute introduction

> My project is a full-stack Restaurant Ordering System. The backend is built with Spring Boot 4.1.1 and Java, the frontend is Angular, PostgreSQL stores the data, and ActiveMQ Artemis is used for asynchronous order events. The system supports four roles: customer, chef, waiter and admin. Each role has different permissions, and JWT authentication protects the API. The project demonstrates REST Web Services, JPA/Hibernate and JMS from the course, and it also exports receipts and reports as XML. I also added automated tests, CI, Docker and environment-based secret handling so reusable passwords and JWT signing secrets are not committed to the repository.

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

`NEW` and `PREPARING` orders may also be cancelled where allowed by backend role and workflow rules.

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

The REST layer is implemented with Spring MVC controllers. Spring is allowed for the course project, so explain the REST client/server communication: HTTP requests, URLs, methods, JSON request/response bodies and status codes.

Important files to show:

```text
src/main/java/com/restaurant/ordering/auth/controller/AuthController.java
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

Only an administrator may export the complete restaurant XML report. It contains total order count, paid revenue, status counts and a compact list of restaurant orders.

### Where XML is implemented

Main implementation:

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

REST endpoints:

```text
src/main/java/com/restaurant/ordering/controller/OrderController.java
```

Angular download code:

```text
frontend/src/app/core/services/order.ts
frontend/src/app/pages/orders/orders.ts
```

During the presentation, download one receipt and the administrator report and open one XML file to show its structure.

## 6. Role permissions

| Role | Main permissions |
| --- | --- |
| CUSTOMER | Register/login, browse/search/filter menu, create orders, see own order history/status, cancel own order before `READY`, download own receipt |
| CHEF | See restaurant orders and move `NEW -> PREPARING -> READY` |
| WAITER | See restaurant orders, cancel before `READY`, and move `READY -> SERVED -> PAID` |
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

Important file to show for URL/role rules:

```text
src/main/java/com/restaurant/ordering/security/config/SecurityConfig.java
```

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

## 9. Latest security hardening

The project no longer stores reusable demo passwords or a reusable JWT signing key in the current repository source.

### Secret handling changes

- `application.properties` reads sensitive values from environment variables.
- `.env` is ignored by Git.
- `.env.example` is a safe template and contains no real credentials.
- `start.sh` generates random local PostgreSQL, Artemis, JWT and demo-account passwords with `openssl` when `.env` does not exist.
- Demo accounts are seeded only when `APP_SEED_DATA=true`.
- Demo-account passwords come from `SEED_*_PASSWORD` environment values.
- The seed initializer refreshes demo users so the database password matches the current local `.env` value.
- GitHub Actions uses temporary/per-run credentials for its smoke test rather than a reusable customer password in the workflow.

Important files to show:

```text
src/main/resources/application.properties
src/main/java/com/restaurant/ordering/config/DevDataInitializer.java
compose.yaml
start.sh
.gitignore
.env.example
.github/workflows/ci.yml
```

### Why the old customer password no longer works

The old fixed demo password was intentionally removed. After `bash start.sh`, use the credentials printed by the script or inspect the local ignored `.env` file. For example:

```bash
grep SEED_CUSTOMER_PASSWORD .env
```

Do not put the generated value in a slide, README or Git commit.

### GitGuardian historical alerts

A secret-scanning tool can continue to show an alert marked as coming from a historical commit even after the current source is cleaned.

A good explanation is:

> The scanner found an old development credential in Git history. The current application no longer uses that hardcoded value. Current secrets are generated locally or at CI runtime and are kept out of source control.

Do not claim that a historical credential is harmless unless you have confirmed it was only a development/test credential and was never used for a real external service.

## 10. Trivy security scan and fixes

Trivy was used to scan dependencies, potential secrets and Docker/configuration problems.

Install on macOS:

```bash
brew install trivy
```

Run from the repository root:

```bash
trivy fs \
  --scanners vuln,secret,misconfig \
  --severity HIGH,CRITICAL \
  .
```

The first final-preparation scan reported:

- frontend `package-lock.json`: `0` HIGH/CRITICAL vulnerabilities
- no `CRITICAL` findings
- no current secret finding shown in the report summary
- `5 HIGH` Maven dependency findings in `pom.xml`
- `1 HIGH` frontend Docker misconfiguration because the Nginx runtime was running without an explicit non-root setup

Those findings were then addressed in PR #18:

- Spring Boot `4.1.0 -> 4.1.1`, which updates the affected managed dependencies
- frontend runtime changed to `nginxinc/nginx-unprivileged:alpine`
- frontend internal port changed from `80` to `8080`
- Docker Compose mapping changed from `4200:80` to `4200:8080`
- the normal GitHub CI passed after the remediation change

Important files to show:

```text
pom.xml
frontend/Dockerfile
frontend/nginx.conf
compose.yaml
```

The correct thing to say in the presentation is:

> I scanned the project with Trivy, fixed the high-severity dependency and frontend-container findings, and rerun the normal CI successfully. I use the latest Trivy output as the source of truth for the final security status.

Do not say `0 HIGH / 0 CRITICAL` unless you have actually rerun Trivy after pulling the latest `master` and the output shows that result.

## 11. Recommended live demo

A strong presentation sequence is:

1. Run `git pull origin master`.
2. Start everything with `bash start.sh`.
3. Copy the customer password printed by `start.sh` — do not use the old fixed password.
4. Log in as a customer.
5. Show menu images, search and category filters.
6. Add two meals to the cart and place an order.
7. Show the success message and the customer's order as `NEW`.
8. Download the order's XML receipt.
9. Log in as chef and move it to `PREPARING`, then `READY`.
10. Log in as waiter and move it to `SERVED`, then `PAID`.
11. Log in as customer again and show the final status.
12. Log in as admin and show dashboard statistics and management pages.
13. Export the administrator XML report and open it.
14. Optionally show the ActiveMQ Artemis console or application log containing a JMS order event.

This demo proves authentication, authorization, REST communication, JPA/database persistence, JMS messaging, XML export, order business logic and UI behavior.

## 12. Tests and CI

Say:

> I added automated tests on both backend and frontend, and GitHub Actions runs them automatically. The pipeline also validates JavaDoc, builds the Angular production application and runs a backend smoke test with PostgreSQL and ActiveMQ Artemis. The smoke test uses runtime-generated credentials rather than a password committed to the repository.

Show:

- `src/test/java/` for backend JUnit/Mockito tests
- Angular `*.spec.ts` tests
- `.github/workflows/ci.yml`
- A successful run in the GitHub **Actions** tab

The backend tests include order role/workflow rules and XML-specific tests:

- customer can export own XML receipt
- admin can export XML report
- non-admin cannot export XML report

The Trivy-remediation branch also completed the normal GitHub CI successfully before it was merged.

For exact commands, see `TESTING.md`.

## 13. JavaDoc and source documentation

The backend source contains JavaDoc for classes, interfaces and methods, including parameters, return values and exceptions where appropriate. Angular uses TSDoc/JSDoc-style source comments.

Generate backend documentation with:

```bash
./mvnw javadoc:javadoc
```

Then open on macOS:

```bash
open target/reports/apidocs/index.html
```

This demonstrates that the API documentation is generated directly from the source comments.

## 14. Docker

The complete stack can be started with:

```bash
bash start.sh
```

Docker Compose runs:

- PostgreSQL
- ActiveMQ Artemis
- Spring Boot backend
- Angular/Nginx frontend

Stop it with:

```bash
bash stop.sh
```

Explain that Docker makes the environment reproducible for another developer or the instructor.

Security-related Docker point:

- the backend runtime already uses non-root execution
- the frontend now uses `nginxinc/nginx-unprivileged:alpine`
- Nginx listens internally on `8080`
- Compose maps `localhost:4200` to container port `8080`

So the external browser URL did not change even though the container was hardened.

## 15. Final verification before presentation

Run these commands from a clean project checkout:

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

Expected functional result from the last verified test/build flow:

- 32 backend tests pass
- JavaDoc finishes with `BUILD SUCCESS`
- 26 Angular tests pass
- Angular production build completes
- `npm audit` has previously been reduced to `0 vulnerabilities`
- the Dockerized application starts successfully
- GitHub CI is green

For the final security result, read the actual current Trivy output after pulling `master`. The code changes for the earlier Trivy findings are already merged, but the latest local rerun should be used before claiming a specific final count.

A non-failing Angular stylesheet budget warning does not prevent the production build from succeeding.

## 16. Questions you may be asked

### Which three technologies from the course did you use?

> REST Web Services, JPA/Hibernate and JMS. REST is implemented with Spring MVC, JPA/Hibernate persists the relational data in PostgreSQL, and JMS with ActiveMQ Artemis handles asynchronous order events. XML is an additional feature for receipts and reports.

### Why did you use Spring for REST?

> The course allows Spring. I used Spring MVC to implement the REST architecture: controllers expose HTTP endpoints, Angular calls them, and the server returns HTTP responses and JSON/XML representations.

### Is XML a Java EE technology?

> XML itself is a data format, not a Java EE API. I use it as an additional course-related feature for receipts and reports. My three main technologies are REST, JPA and JMS.

### Why did you use JWT?

> Because the frontend and backend are separate applications. JWT provides stateless authentication for REST requests and allows role information to be applied to protected endpoints.

### Why did you use JMS/Artemis if REST already works?

> REST handles direct client/server requests. JMS demonstrates asynchronous messaging: the order can be published as an event without requiring the sender to synchronously call every consumer.

### Why PostgreSQL?

> The application has relational data with users, meals, categories, orders and order items, so a relational database and JPA mappings fit the domain well.

### How are permissions protected?

> They are checked on the backend using Spring Security and the authenticated role. The frontend also adjusts the UI for usability, but the backend remains the security boundary.

### Where is XML created?

> The XML strings are generated in `OrderServiceImpl`. `OrderController` exposes them with the `application/xml` media type, and the Angular order service/page downloads them as files.

### How do you protect secrets?

> I do not keep reusable passwords or the JWT signing key in the current source. Local values are generated into a Git-ignored `.env` file, and the CI smoke test uses temporary credentials generated or derived for each run.

### Why does `Customer123` no longer work?

> It was an old hardcoded development password. It was removed during security hardening. The local demo password is now generated into `.env` and `start.sh` prints the current credential after startup.

### Why can GitGuardian still show an alert after the fix?

> Git history is immutable by default, so scanners can detect a value in an old commit even after the current file is clean. The important distinction is whether the value is still active. Current application credentials are no longer stored that way.

### Did you perform a security scan?

> Yes. I used Trivy to scan dependencies, secrets and Docker/configuration. It found high-severity Maven and frontend-container findings. I updated Spring Boot and changed the frontend to an unprivileged Nginx runtime, then verified the normal CI still passed.

### Why did you change the frontend container port to 8080?

> The unprivileged Nginx image should not bind to privileged port 80 as root. It listens on 8080 inside the container, while Docker Compose still exposes the site as `localhost:4200`.

### What would you add in a production version?

Possible extensions include password reset/email verification, payment-provider integration, migrations with Flyway/Liquibase, a managed secrets service, HTTPS and reverse-proxy hardening, rate limiting, production monitoring, cloud deployment and automated security scanning in CI.

## 17. Strong closing sentence

> The project demonstrates a complete multi-role restaurant workflow across an Angular frontend, secured Spring Boot REST backend, JPA/Hibernate relational persistence, JMS asynchronous messaging and XML exports, with automated tests, CI, generated JavaDoc, Dockerized deployment, environment-based secret handling and security hardening based on automated scan results.

# Testing Guide

This project contains automated backend, frontend and smoke tests. The same checks run in GitHub Actions on pull requests and pushes to `master`.

## Run all backend tests

```bash
./mvnw test
```

Backend tests are located under:

```text
src/test/java/com/restaurant/ordering/
```

They cover authentication, categories, meals, users and role-specific order workflow behavior.

## Run all frontend tests

```bash
cd frontend
npm ci
npm test -- --watch=false
```

Frontend tests are located next to the Angular code under `frontend/src/app/` and cover cart behavior, authentication/JWT handling, API services and the auth interceptor.

## Run the production frontend build

```bash
cd frontend
npm run build
```

## Run the full application smoke test manually

Start the project:

```bash
bash start.sh
```

Then verify these flows in the browser:

1. Customer logs in or registers.
2. Customer browses the menu, searches/filters meals, adds items to the cart and creates an order.
3. Chef moves `NEW -> PREPARING -> READY`.
4. Waiter moves `READY -> SERVED -> PAID`.
5. Customer sees the updated order status.
6. Admin can manage meals, categories and users and can perform all valid order transitions.
7. Admin dashboard statistics load correctly.

Stop the project:

```bash
bash stop.sh
```

## GitHub Actions

The CI workflow is stored at:

```text
.github/workflows/ci.yml
```

It runs:

1. Backend unit tests
2. Frontend unit tests
3. Angular production build
4. Backend smoke test with PostgreSQL and ActiveMQ Artemis

To show the tests during a presentation, open the repository's **Actions** tab and show a green CI run, then open `src/test/java` and the Angular `*.spec.ts` files to explain what is being tested.

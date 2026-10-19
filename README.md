# T-Shop

[![CI](https://github.com/chemha/T-Shop/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/chemha/T-Shop/actions/workflows/ci.yml)

REST API for an online store: catalogue, shopping cart, checkout and order management, built with
**Java 25**, **Spring Boot 4** and **PostgreSQL**.

**[Live demo](https://t-shop.chemha.dev)** · [API docs (Swagger UI)](https://t-shop.chemha.dev/swagger-ui.html)

This is a rewrite of my final project for a JavaSchool course (selected as the best project of the
cohort), originally a Spring Boot 2.7 + Thymeleaf + MySQL app. The original
is kept under the [`v1`](https://github.com/chemha/T-Shop/tree/v1) tag. This version keeps the
domain and fixes what the first one got wrong: [what changed and why](#what-changed-from-v1).

> **About this rewrite.** v1 is my original course project. v2 was rebuilt with the help of an
> AI coding assistant: I set the goals and made the main choices (API-first design, PostgreSQL,
> letting visitors fill a cart before logging in), tested the result and caught bugs along the
> way. The reasoning behind each design decision is in the [ADRs](docs/adr/README.md).

## Live demo

The shop runs at **<https://t-shop.chemha.dev>** and its API docs at
**<https://t-shop.chemha.dev/swagger-ui.html>**. Log in with any of these accounts:

| Email | Password | Role |
|---|---|---|
| `admin@admin.admin` | `admin` | Admin: manages products, categories, customers and orders |
| `test@test.test` | `test` | Customer |
| `john.doe@example.com` (and the other sample customers) | `password` | Customer |

Feel free to change anything, including as admin: the demo resets to its sample data every night.
It is self-hosted on a small home server, so it may occasionally be offline.

## Try it

**Option A: everything in Docker** (only Docker needed)

```bash
docker compose -f compose.demo.yaml up -d --build
```

Open the shop at **<http://localhost:8080>** and the API docs at
**<http://localhost:8080/swagger-ui.html>**. This is how the demo runs on a server: one port, the
web served by nginx, which forwards the API to the backend. `docker compose -f compose.demo.yaml
down` stops it, and the next `up` starts again from fresh demo data.

**On a small server** (e.g. a homelab container with 1 GB of RAM): CI publishes both images to
GitHub Container Registry on every push to `main`, so the server only downloads and runs them.
The demo uses about 370 MB of memory.

```bash
curl -fsSLO https://raw.githubusercontent.com/chemha/T-Shop/main/compose.server.yaml
docker compose -f compose.server.yaml up -d
```

Update with `docker compose -f compose.server.yaml pull` followed by the same `up -d`.

**Option B: run from source** (JDK 25 and Docker)

```bash
./mvnw spring-boot:run    # Windows: mvnw.cmd spring-boot:run
```

PostgreSQL starts automatically from [`compose.yaml`](compose.yaml) and Flyway creates the schema
and demo data. The API runs at <http://localhost:9000/swagger-ui.html>; for the web, see
[Web client](#web-client).

Local runs have the same [demo accounts](#live-demo). In Swagger UI, call `POST /auth/login` with
one of them, click **Authorize** and paste the `accessToken`.

A full walkthrough (log in, fill the cart, check out, confirm the order as admin) is in
[`docs/http/tshop.http`](docs/http/tshop.http), runnable from VS Code or IntelliJ.

### Web client

A small React client in [`frontend/`](frontend) covers the whole shop: catalogue with search
and filters, a cart that works before logging in, login and registration, checkout and order
history, plus admin pages for orders, customers, products and categories. With the backend running
(option B above) and Node 24:

```bash
cd frontend
npm install
npm run dev    # http://localhost:5173
```

Its TypeScript types are generated from the same `openapi.yaml`, so a contract change that breaks
the client fails its type check in CI. Vite forwards `/api` to the backend, so no CORS setup is
needed.

## Features

**Visitors:** browse and search the catalogue, fill a cart and see it without an account. Logging
in is only needed to place the order, and the guest cart is then merged into the customer's cart.

**Customers:** register and log in, manage their profile, password and addresses, keep a cart
saved on the server, place orders and cancel them while pending.

**Admins:** manage categories and products, list customers, change roles, follow orders through
their lifecycle (`PENDING → CONFIRMED → SHIPPED → DELIVERED`, or `CANCELLED`).

**Under the hood**

- Checkout in a single transaction with **optimistic locking**: concurrent purchases never oversell.
- **Stateless JWT** authentication (RS256), deny-by-default authorization, no IDOR: `/me` endpoints
  only ever act on the caller's data.
- **Contract-first**: the API is defined in [`openapi.yaml`](src/main/resources/static/openapi.yaml);
  controllers implement interfaces generated from it.
- Errors as **RFC 9457 problem details**, including validation, 401 and 403.
- Orders keep a **snapshot** of prices and shipping address.
- **No N+1 queries**: verified by tests that count SQL statements.

## Tech stack

| Area | Technology |
|---|---|
| Language | Java 25 (records, pattern matching, exhaustive switches, virtual threads) |
| Framework | Spring Boot 4.1: Web MVC, Data JPA (Hibernate 7), Security 7, Validation, Actuator |
| Database | PostgreSQL 17, Flyway migrations |
| API | OpenAPI 3.1, OpenAPI Generator, springdoc (Swagger UI) |
| Security | Spring Security OAuth2 Resource Server, JWT (Nimbus), BCrypt |
| Mapping | MapStruct |
| Tests | JUnit, AssertJ, MockMvcTester, Testcontainers |
| Build and delivery | Maven, Docker (multi-stage, layered jar), Docker Compose, GitHub Actions |
| Web client | React 19, TypeScript, Vite, React Router, TanStack Query, openapi-fetch, Vitest |

## Architecture

```mermaid
flowchart LR
    client([HTTP client]) --> security
    subgraph app [Spring Boot application]
        security[Security filter<br/>JWT validation, roles] --> controller
        controller[Controllers<br/>implement generated API interfaces] --> service
        service[Services<br/>transactions, business rules] --> repository[Spring Data repositories]
        controller -.-> mapper[MapStruct mappers<br/>entity → DTO]
        advice[GlobalExceptionHandler<br/>RFC 9457 problem details]
    end
    repository --> db[(PostgreSQL)]
    contract[[openapi.yaml]] -. generates interfaces and DTOs .-> controller
```

The code is organised **by feature**, not by technical layer:

```
src/main/java/org/tshop
├── auth        Login, JWT issuing and validation, security rules
├── customer    Customers, profile, addresses
├── catalog     Categories, products, search
├── cart        Customer cart, guest cart merge
├── order       Checkout, order lifecycle
├── admin       Customer and order management
└── shared      Error handling, mapping helpers
```

Entities from different features reference each other by id, every association is lazy, and
`open-in-view` is off, so all database access is explicit and happens inside a service.

### Data model

The schema is created by Flyway from
[`V1__create_schema.sql`](src/main/resources/db/migration/V1__create_schema.sql). The v1 model
(conceptual, logical and physical diagrams) is kept in [`docs/v1`](docs/v1) for comparison.

```mermaid
erDiagram
    customer ||--o{ address : "has"
    customer ||--o{ cart_item : "fills"
    customer ||--o{ orders : "places"
    category ||--o{ product : "groups"
    product ||--o{ cart_item : "is in"
    orders ||--|{ order_item : "contains"
    product |o--o{ order_item : "was sold as"

    customer {
        bigint id PK
        varchar first_name
        varchar last_name
        date birth_date
        varchar email UK "stored in lower case"
        varchar password_hash "BCrypt"
        varchar role "CUSTOMER or ADMIN"
        timestamptz created_at
    }
    address {
        bigint id PK
        bigint customer_id FK
        varchar country
        varchar postal_code
        varchar street
        varchar house_number
        varchar apartment
    }
    category {
        bigint id PK
        varchar name UK
    }
    product {
        bigint id PK
        bigint category_id FK
        varchar name
        numeric price "NUMERIC(10,2)"
        varchar brand
        varchar color
        numeric weight_kg
        numeric volume_l
        integer stock "never below 0"
        bigint version "optimistic locking"
        timestamptz created_at
        timestamptz updated_at
    }
    cart_item {
        bigint customer_id PK, FK
        bigint product_id PK, FK
        integer quantity
        timestamptz added_at
    }
    orders {
        bigint id PK
        bigint customer_id FK
        varchar status
        varchar payment_method
        varchar payment_status
        varchar delivery_method
        varchar shipping_country "copied from the address"
        varchar shipping_postal_code
        varchar shipping_street
        varchar shipping_house_number
        varchar shipping_apartment
        numeric total
        bigint version "optimistic locking"
        timestamptz created_at
        timestamptz updated_at
    }
    order_item {
        bigint id PK
        bigint order_id FK
        bigint product_id FK "null if the product is deleted"
        varchar product_name "copied at checkout"
        numeric unit_price "copied at checkout"
        integer quantity
    }
```

Compared with v1: `Good` became `order_item`, the cart moved from the HTTP session to
`cart_item`, prices are `NUMERIC` instead of `FLOAT`, the admin flag became a `role`, and orders
copy the shipping address and the product name and price at checkout.

### Order lifecycle

```mermaid
stateDiagram-v2
    direction LR
    [*] --> PENDING : checkout
    PENDING --> CONFIRMED : card payment becomes PAID
    CONFIRMED --> SHIPPED
    SHIPPED --> DELIVERED : cash payment becomes PAID
    PENDING --> CANCELLED : customer or admin
    CONFIRMED --> CANCELLED : admin only
    DELIVERED --> [*]
    CANCELLED --> [*]
```

Cancelling returns the stock, and a paid order is marked `REFUNDED`. Any other transition is
rejected with 409.

### Design decisions

Each one is explained, with the alternatives and trade-offs, in an
[Architecture Decision Record](docs/adr/README.md):

1. [Contract-first API with OpenAPI](docs/adr/0001-contract-first-api.md)
2. [Stateless JWT authentication](docs/adr/0002-stateless-jwt-authentication.md), including its known limitations
3. [Optimistic locking for stock, with retries](docs/adr/0003-optimistic-locking-for-stock.md)
4. [Orders keep a snapshot of prices and address](docs/adr/0004-orders-keep-a-snapshot.md)
5. [Package by feature, references between aggregates by id](docs/adr/0005-package-by-feature.md)
6. [Guest cart in the browser, merged at login](docs/adr/0006-guest-cart-in-the-browser.md)

## What changed from v1

| v1 | v2 | Why |
|---|---|---|
| Stock checked, then subtracted in separate steps | One transaction, `@Version` and retries | Two buyers could get the last unit and leave negative stock |
| Order created in the controller, step by step | `OrderService.placeOrder`, all or nothing | A failure halfway left partial orders |
| Prices as `Float` | `NUMERIC` / `BigDecimal` | Floating point can't represent cents exactly |
| Order lines pointed to the live product | Name and price copied into the order | Changing a price rewrote past orders |
| Cart of JPA entities in the HTTP session | Cart table, priced on every read; guest cart kept by the client and merged at login | Stale prices and detached entities, without losing the guest cart |
| Any logged-in user could reach any address by id | Lookups by id **and** owner | Insecure direct object reference (IDOR) |
| Every error was a 400 with the internal message | RFC 9457 with the right status | Correct semantics, no internal details leaked |
| Status values as free text | Enums and a state machine | Invalid transitions are impossible |
| Schema from a SQL dump | Flyway migrations | Versioned, reproducible schema |
| 2 unit tests | 100+ unit, persistence, API and concurrency tests on real PostgreSQL | Confidence to change the code |

## Tests

```bash
./mvnw verify    # Docker must be running
```

- **Unit tests** for the domain rules: stock, totals, the order state machine.
- **Persistence tests** (`@DataJpaTest`) on PostgreSQL via Testcontainers: queries, constraints,
  optimistic locking, number of SQL statements per request.
- **API tests** through the whole stack with `MockMvcTester`: every endpoint, validation errors,
  ownership checks, and attacks on authentication (forged, tampered, expired and foreign tokens).
- **Concurrency tests**: simultaneous checkouts of the same product on virtual threads.

## Configuration

Locally nothing needs configuring. The `prod` profile reads everything from the environment:

| Variable | Description |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | PostgreSQL connection, e.g. `jdbc:postgresql://host:5432/tshop` |
| `JWT_PRIVATE_KEY`, `JWT_PUBLIC_KEY` | RSA key pair in PEM format. The app won't start without them |

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl rsa -in private.pem -pubout -out public.pem
export JWT_PRIVATE_KEY="$(cat private.pem)" JWT_PUBLIC_KEY="$(cat public.pem)"
```

In production the demo data isn't loaded, logs are JSON (ECS format), and
`/actuator/health/liveness` and `/actuator/health/readiness` are available for the orchestrator.

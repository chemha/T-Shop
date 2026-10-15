# T-Shop

A small online shop built with **Spring Boot**, **Thymeleaf** and **MySQL** as the final project of a JavaSchool course. It was built in under a month and was selected as the best project of the cohort.

Customers can browse the catalogue, fill a cart, manage their addresses and place orders. Administrators manage clients, categories, products and order statuses.

## Features

**Customers**
- Register, log in and edit their profile and password
- Browse the product catalogue
- Session-based shopping cart with stock checks
- Manage delivery addresses
- Place orders (payment and delivery method) and see their order history

**Administrators**
- Create, edit and delete products
- Manage categories
- Manage clients (including creating other admins)
- See every order and update its status

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 2.7 (Web MVC, Data JPA, Security) |
| Views | Thymeleaf, Bootstrap 5, jQuery, DataTables |
| Database | MySQL 8 |
| API contract | OpenAPI 3 (`api.yaml`) + OpenAPI Generator |
| Mapping | MapStruct, Lombok |
| Tests | JUnit 5, Mockito |
| Build | Maven (wrapper included) |

## Architecture

The REST API is designed **contract-first**: [`src/main/resources/api.yaml`](src/main/resources/api.yaml) describes every resource, and at build time the OpenAPI Generator creates the API interfaces (`org.tshop.api`) and DTOs (`org.tshop.dto`) in `target/generated-sources`. The controllers implement those interfaces.

```
src/main/java/org/tshop
├── configuration   Spring MVC, Spring Security, exception handler
├── controllers     MVC pages (Thymeleaf) + REST endpoints from the generated interfaces
├── services        Business logic
├── repositories    Spring Data JPA repositories
├── mappers         MapStruct mappers between entities and DTOs
└── entities        JPA entities
```

The pages are rendered with Thymeleaf. The management screens (clients, categories, products and addresses) call the REST endpoints with jQuery/AJAX.

### Data model

Six tables: `client`, `clientaddress`, `category`, `product`, `order` and `good` (the order lines). The full schema and sample data are in [`docs/t-shop.sql`](docs/t-shop.sql). The MySQL Workbench model is in `docs/Physical schema.mwb`, and the original assignment is in `docs/FinalTask_2.docx`.

## Getting started

### Requirements
- JDK 17
- MySQL 8 (local or Docker)

### 1. Database

**Option A: Docker**

```bash
docker run -d --name tshop-mysql -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 \
  -v "$(pwd)/docs/t-shop.sql:/docker-entrypoint-initdb.d/t-shop.sql:ro" \
  mysql:8.0 --lower-case-table-names=1
```

**Option B: local MySQL**

Import `docs/t-shop.sql`. It creates the `t-shop` schema with sample data.

> **Note:** The SQL script creates tables with capitalised names (`Client`, `Product`…), but the entities use lowercase names. On Windows this works out of the box. On Linux/macOS, start MySQL with `lower_case_table_names=1`, as in the Docker command above.

### 2. Configuration

The connection settings are in [`application.properties`](src/main/resources/application.properties). Environment variables override the defaults:

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/t-shop` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | `root` |

### 3. Run

```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

Open <http://localhost:9000>.

### Demo accounts

| Email | Password | Role |
|---|---|---|
| `admin@admin.admin` | `admin` | Admin |
| `test@test.test` | `test` | Customer |
| `john.doe@example.com` (and the other sample clients) | `password` | Customer |

## Tests

```bash
./mvnw test
```

## REST API

Every resource exposes CRUD endpoints as defined in `api.yaml`:
`/clients`, `/client-addresses`, `/categories`, `/products`, `/orders`, `/goods`.
Listing endpoints are paginated with `pageNumber` (from 1) and `pageSize`.

## Access control

Security is handled by **Spring Security** ([`SecurityConfiguration`](src/main/java/org/tshop/configuration/SecurityConfiguration.java)):
- **Form login** at `/clients/login` (the email is the username). Passwords are hashed with BCrypt, and clients with the `admin` flag get the `ROLE_ADMIN` role.
- After login, [`LoginSuccessHandler`](src/main/java/org/tshop/services/security/LoginSuccessHandler.java) stores `userId`, `admin` and `userName` in the HTTP session, which the templates and controllers use.
- **CSRF protection** is enabled. Thymeleaf adds the token to forms (`th:action`), and [`js/csrf.js`](src/main/resources/static/js/csrf.js) sends it in a header with every jQuery AJAX request (read from the `XSRF-TOKEN` cookie).
- **Logout** is a `POST` to `/clients/logout`.

| Access | Routes |
|---|---|
| Public | home, catalogue, login, registration (`POST /clients`), `GET /products/**` |
| Logged-in customer | cart, profile, own orders, addresses |
| Admin | client, category and order management, product changes, `/goods` |

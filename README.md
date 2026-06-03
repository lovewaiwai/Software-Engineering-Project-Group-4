# SwapCampus

SwapCampus is a campus second-hand marketplace scaffold for parallel backend, frontend, database, deployment, and testing work.

## Tech Stack

- Backend: Spring Boot 3, Spring Security, JWT, MyBatis-Plus, SQL Server driver, Spring WebSocket, Swagger/OpenAPI.
- Frontend: Vue 3, Vite, TypeScript, Element Plus, Pinia, Vue Router, Axios.
- Database: SQL Server 2022 Developer/Express.
- Object storage: MinIO, configured for local Docker.
- Deployment: Docker Compose with SQL Server and MinIO by default; backend and frontend are available through the `app` profile.

## Structure

```text
backend/              Spring Boot modular monolith scaffold
frontend/             Vue 3 SPA scaffold
db/migrations/        SQL Server initialization scripts
infra/                Environment examples and deployment notes
tests/                API, E2E, and performance test assets
team_docs/            Existing project design documents
docker-compose.yml    Local infrastructure composition
```

## Local Start

Start infrastructure:

```powershell
docker compose up -d sqlserver minio
```

SQL Server uses database `SwapCampus`. Run `db/migrations/V001__init.sql` in SSMS or `sqlcmd` after the database is available.

MinIO console: `http://localhost:9001`

## Backend

Configuration is in `backend/src/main/resources/application.yml` and can be overridden with environment variables:

```powershell
$env:DB_URL="jdbc:sqlserver://localhost:1433;databaseName=SwapCampus;encrypt=true;trustServerCertificate=true"
$env:DB_USERNAME="sa"
$env:DB_PASSWORD="YourStrong!Passw0rd"
```

Run or build:

```powershell
cd backend
mvn spring-boot:run
mvn test
```

Swagger UI is reserved at `http://localhost:8080/swagger-ui.html`. WebSocket chat is reserved at `/ws/chat`.

## Frontend

```powershell
cd frontend
npm install
npm run dev
npm run build
```

The current app provides route placeholders for login, verification, product, order, chat, profile, points, and admin workflows.

## Module Development

Backend packages follow `com.swapcampus.<module>`:

```text
auth user product search recommend order payment delivery chat review report admin ai audit common
```

Each business module starts with:

```text
controller/ service/ service/impl/ mapper/ entity/ dto/ vo/ enums/
```

Use controller classes for HTTP boundary code, service interfaces for cross-module contracts, mapper classes for SQL Server persistence, and adapters for external services. Keep new API names aligned with `team_docs/D4-D5_架构详细数据库接口设计.md`.

## Mock Adapters

- `MockPaymentAdapter`: creates mock payment orders, payment URLs, query results, and refunds.
- `MockLockerAdapter`: reserves a mock locker box and simulates store/pickup confirmation.
- `MockAiSuggestAdapter`: returns rule-based category, tag, and price suggestions.

These mocks are intentional extension points. Later agents can add real payment, locker, or AI providers by implementing the same adapter interfaces without changing the order/product flow.

## TODO

- Implement real auth register/login, JWT filter, and role checks.
- Replace placeholder entities with concrete table entities from `V001__init.sql`.
- Implement module services and controllers according to the D4-D5 API draft.
- Add seed data, API smoke tests, and k6 performance scripts.
- Wire MinIO upload endpoints and full WebSocket chat persistence.

# Pension Planner

A web application for planning and tracking pensions. It currently provides a login/registration flow and a user settings backend, with a pension dashboard planned for later phases.

- **Backend**: Java 25, Spring Boot 3.5, REST API under `/api/v1`, JWT-based auth, SQLite database with Flyway migrations
- **Frontend**: React 19 + TypeScript, Vite, PrimeReact, TanStack Query
- **Deployment**: single Spring Boot fat JAR serving the built frontend as static resources, runnable via Docker Compose

## Project structure

```
pension-planner/
├── backend/                  Spring Boot application (Maven)
│   └── src/main/java/com/pensionplanner/
│       ├── auth/             Login, registration, JWT issuance
│       ├── security/         JWT filter, current-user resolution
│       ├── user/             User entity, profile settings API
│       ├── pension/          Pension entities and repositories (dashboard Phase 2)
│       ├── income/           State pension and other income entities
│       ├── audit/            Audit history entities and service
│       ├── config/           Security, data source, JWT properties
│       └── common/           Error handling, DTOs
│       └── resources/
│           ├── application.yml
│           └── db/migration/ Flyway SQL migrations
├── frontend/                 React SPA (Vite)
│   └── src/
│       ├── api/              Axios client and API functions
│       ├── auth/             Auth context (login/logout), route guard
│       └── pages/            Login, Register, Home
├── .run/                     IntelliJ run configurations
├── docker-compose.yml
└── Dockerfile
```

The frontend calls the backend through Vite's dev-server proxy: `/api` on port 5173 is forwarded to `http://localhost:8080`. The API is stateless — the backend issues a signed JWT on login, stored in an HttpOnly cookie (`pp_jwt`).

## Prerequisites

- Java 25 (JDK)
- Node.js 20+ and npm
- Maven (optional — IntelliJ can manage Maven for you)
- IntelliJ IDEA (recommended)

## Running locally

### Option 1: IntelliJ (recommended)

1. Open the **repository root** (`pension-planner/`) as the project.
2. When IntelliJ prompts, import `backend/pom.xml` as a Maven project (this creates the `pension-planner-backend` module referenced by the run configs).
3. Choose a run configuration from the dropdown (defined in `.run/`):
   - **Pension Planner Backend** — starts the Spring Boot app on `http://localhost:8080`
   - **Pension Planner Frontend (dev)** — starts the Vite dev server on `http://localhost:5173`
   - **Pension Planner (all)** — compound configuration that starts both
4. Open `http://localhost:5173`. Registration is enabled by default (`ALLOW_REGISTRATION=true`), so you can create an account on the sign-in screen.

### Option 2: Command line

Terminal 1 — backend:

```bash
cd backend
DATABASE_PATH=./data/pension.db mvn spring-boot:run
```

Terminal 2 — frontend:

```bash
cd frontend
npm install
npm run dev
```

### Option 3: Docker

```bash
docker compose up --build
```

This builds a single image (multi-stage: frontend build → backend jar → runtime) and serves the full app at `http://localhost:8080`. The SQLite database is persisted in `./data` (bind-mounted as a volume).

To build the image without Compose:

```bash
docker build -t pension-planner:latest .
docker run --rm -p 8080:8080 -v "$PWD/data:/data" pension-planner:latest
```

The container runs as a non-root user (uid 1000, matching the standard first host user) so the bind-mounted `./data` directory is writable. If your host user has a different uid, either chown `./data` to uid 1000 or switch Compose to a named volume.

The image exposes a `/health` endpoint (returns `{"status":"UP"}` when the app and its SQLite database are ready); `docker compose` uses it as the container healthcheck.

## Configuration

All settings are environment variables (defaults shown):

| Variable             | Default         | Description                                                        |
| -------------------- | --------------- | ------------------------------------------------------------------ |
| `DATABASE_PATH`      | `/data/pension.db` | File path of the SQLite database (created automatically)          |
| `SERVER_PORT`        | `8080`          | Backend HTTP port                                                   |
| `ALLOW_REGISTRATION` | `true` (Docker) | When `false`, the register endpoint is disabled                     |
| `JWT_SECRET`         | *(empty)*       | Secret used to sign JWTs. Leave empty for a random per-run secret   |
| `JWT_TTL_SECONDS`    | `604800`        | JWT lifetime in seconds (defaults to 1 week)                        |
| `COOKIE_SECURE`      | `false`         | Set `true` to send the JWT cookie only over HTTPS                   |
| `AUTH_RATE_LIMIT_ENABLED` | `true`     | Rate limiting of `/auth/register` and `/auth/login` (token bucket per IP) |
| `AUTH_RATE_LIMIT_CAPACITY` | `20`       | Max burst of auth requests before a `429 Too Many Requests`          |
| `AUTH_RATE_LIMIT_PER_MINUTE` | `10`     | Sustained auth request allowance per IP (bucket refill rate)        |
| `CORS_ALLOWED_ORIGINS` | *(empty)*    | Comma-separated origins allowed to call the API cross-origin. Empty = cross-origin requests are rejected; the frontend is served same-origin |

Security hardening baked in: every response carries `Content-Security-Policy` (`default-src 'self'`), `Referrer-Policy: no-referrer`, `Permissions-Policy`, `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY` and `Cache-Control: no-store`. The rate limiter honours `X-Forwarded-For`, so requests behind a reverse proxy are keyed by the real client IP.

## GraalVM native image

The backend compiles to a native executable via the GraalVM `native` Maven profile (`mvn -Pnative -DskipTests native:compile`, run from `backend/` with a GraalVM for JDK 25 on `PATH`/`JAVA_HOME`). This produces a single `backend/target/pension-planner` ELF binary (~150 MB) that starts in ~0.4 s with no JVM.

The profile uses `native-maven-plugin` (configuration in `backend/pom.xml`) with the GraalVM reachability metadata repository enabled and `fallback=false`. Runtime hints for reflection/JNI are provided by `NativeRuntimeHints` (`com.pensionplanner.config`) and wired via `@ImportRuntimeHints` on `PensionPlannerApplication`. They cover:

- **JJWT** (`io.jsonwebtoken.*`): the impl/factory classes (`KeysBridge`, `DefaultJwtBuilder`, `DefaultJwtParserBuilder`, claims/header builders, `Standard*Algorithms`/`JwksBridge`, …) that JJWT loads reflectively.
- **SQLite / Flyway / Hibernate**: `META-INF/services/java.sql.Driver` (driver ServiceLoader), the `org/sqlite/native/Linux/*/libsqlitejdbc.so` JNI library (extracted to a temp file at runtime), the `org.sqlite.JDBC` driver class, and the `org.hibernate.community.dialect.SQLiteDialect` (loaded by `Class.forName` from `spring.jpa.properties.hibernate.dialect`).

Two configuration notes that are required for the native build to work:

- **Flyway must match Spring Boot's managed version.** The pom previously pinned `flyway.version=12.6.2`; the AOT-generated `NativeImageResourceProviderCustomizer` compiles against the `Scanner` constructor signature of the Flyway version Boot manages (11.7.2), so a 12.x core yields `NoSuchMethodError` in the native binary. The pin was removed so `flyway-core` follows Spring Boot 3.5 (11.7.2), which includes SQLite support in core — the separate `flyway-database-nc-sqlite` module (12.x only) is not needed.
- JVM run is unaffected; the same binary works for `java -jar`.

Run locally: `server.shutdown: graceful` is set; start with the `DATABASE_PATH` (default `/data/pension.db`), `ALLOW_REGISTRATION`, `COOKIE_SECURE`, `SERVER_PORT` env vars as per the table above.

A native container is also available: `Dockerfile.native` compiles inside `ghcr.io/graalvm/graalvm-community:25` and runs the binary in a `debian:bookworm-slim` image (glibc, no JVM; ~85 MB). Use `docker compose -f docker-compose.yml -f docker-compose.native.yml up -d --build` — it keeps the same env vars, `/data` volume, and `/health` healthcheck. The default `docker compose up` still uses the JVM fat-JAR image.

## Testing

### Backend

- **Stack**: JUnit 5 + Spring Boot Test (`MockMvc` + `spring-security-test`), 110 tests across unit and integration suites.
- **Run**: 
  ```bash
  cd backend && mvn test
  ```
- Any single test: `mvn test -Dtest=AuthRateLimitIntegrationTest`.
- Test config lives in `backend/src/test/`; a test-only `application.properties` disables the auth rate limiter (a shared in-memory database would otherwise let one test class exhaust another's token bucket, causing spurious `429`s).

### Frontend

- **Stack**: Vitest 5 + React Testing Library (`@testing-library/react`, `@testing-library/jest-dom`, `@testing-library/user-event`) with `jsdom`; API calls are intercepted by **MSW** (Mock Service Worker) against an in-memory fixture database, so `npm test` needs no running backend.
- **Run**:
  ```bash
  cd frontend && npm test        # single run
  npm run test:watch             # watch mode
  ```
- Smoke tests live next to the pages they exercise (`frontend/src/pages/*.smoke.test.tsx`) and cover the PLAN phases: dashboard, pension modal create/edit, state pension, other income + statements (table/chart/add/edit/delete), analytics charts, settings and tag creation/assignment.
- Fixtures + MSW handlers: `frontend/src/test/` (`fixtures.ts`, `handlers.ts`, `server.ts`). Handlers keep mutable state that is reset per test; assertions are written against the DOM only.
- For determinism in `jsdom`, PrimeReact's popup-heavy controls are mocked with lightweight equivalents under `frontend/src/test/mocks/` (`Calendar`, `InputNumber`, `Chart`, `MultiSelect`). This is what makes date/number/tag interactions reliable without a browser.

## Debugging

- **Backend**: run **Pension Planner Backend** in IntelliJ and set breakpoints in Java code; they are hit when the API is called from the frontend. The run config includes `--enable-native-access=ALL-UNNAMED` to silence harmless SQLite native-access warnings (add it to any manual `java -jar` launches too).
- **Frontend**: use your browser's DevTools. Vite hot-reloads on save — hard-refresh (`Ctrl+Shift+R`) if changes don't appear.
- **Tests**: see the [Testing](#testing) section for how to run the backend and frontend suites.

## Third-party libraries

### Backend

| Library | Why it's used |
| --- | --- |
| Spring Boot (starter-web) | Provides embedded Tomcat, REST controllers, and JSON (Jackson) serialization for the API. |
| Spring Boot (starter-security) | Handles authentication/authorization; configured for a stateless, JWT-based security filter chain. |
| Spring Boot (starter-data-jpa) | Hibernate ORM integration used by the JPA repositories and entities. |
| Spring Boot (starter-validation) | Bean Validation for request DTOs (e.g., required fields, password length) before they reach services. |
| sqlite-jdbc (org.xerial) | The JDBC driver that lets the app read and write the embedded SQLite database file. |
| hibernate-community-dialects | Supplies Hibernate's `SQLiteDialect` so the ORM can talk to SQLite. |
| Flyway (flyway-core + flyway-database-nc-sqlite) | Version-controlled database migrations (`db/migration/V1__init.sql`); the SQLite community dialect plugin is required for SQLite support. |
| jjwt (api/impl/jackson) | Creates and verifies the signed JWTs used for the stateless session cookie. |
| spring-boot-starter-test / spring-security-test | Test framework (JUnit, MockMvc, security test support) for the unit and integration tests. |

### Frontend

| Library | Why it's used |
| --- | --- |
| react / react-dom | UI framework; renders the SPA and manages component state. |
| react-router-dom | Client-side routing between the Login, Register, and Home pages. |
| @tanstack/react-query | Server-state management: caches API data, handles loading/error states, and keeps the auth user in sync. |
| axios | HTTP client used by `src/api/` to call the backend API with credentials (cookies). |
| primereact | PrimeReact UI component library (buttons, inputs, toolbar, password fields) for a consistent look. |
| primeflex | Utility-first CSS framework (flexbox/grid helpers like `flex flex-column gap-3`) used for layout. |
| primeicons | Icon set used alongside PrimeReact components. |
| vite + @vitejs/plugin-react | Dev server with hot module replacement and the production bundler; the plugin adds React fast refresh. |
| typescript | Static typing for the frontend codebase, enforced via `tsc` in the build. |

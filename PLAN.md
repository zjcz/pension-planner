# Pension Planner — Development Plan

Source of truth: `Pension Planner Functional Specification v9.0`
Target: Self-hosted / Docker, single container (Spring Boot serving REST + compiled React assets, SQLite via host volume mount).

## Decisions (confirmed)

| Topic            | Choice                                                        |
|------------------|---------------------------------------------------------------|
| Backend build    | Maven                                                         |
| Frontend data    | TanStack React Query + axios/fetch wrapper                    |
| Testing          | Backend unit + integration (JUnit 5, MockMvc); minimal FE smoke |
| Phase ordering   | Spec order (see below)                                        |

## Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│  Single Docker image                                │
│                                                     │
│  ┌─────────────────────┐   ┌─────────────────────┐ │
│  │ React (Vite/PrimeReact)│→│ Spring Boot 3.3+    │ │
│  │ compiled static assets │ │ (Java 25, REST)     │ │
│  └─────────────────────┘   └──────────┬──────────┘ │
│                                       │            │
│                              ┌────────▼─────────┐  │
│                              │ SQLite           │  │
│                              │ /data/pension.db │  │
│                              │ (host volume)    │  │
│                              └──────────────────┘  │
└─────────────────────────────────────────────────────┘
```

- **Backend**: Spring Boot 3.3+ (Spring Web, Spring Security, Spring Data JPA or JDBC, Hibernate).
  Entity listeners (e.g. `@PrePersist/@PreUpdate` or a dedicated audit interceptor) implement audit snapshots.
- **Frontend**: React + Vite + PrimeReact (Calendar, DataTable, Dialog, Toolbar, Chart), PrimeFlex grid, PrimeIcons.
- **Auth**: HTTP-only `SameSite=Lax` JWT cookies. Password hashing via BCrypt/Argon2. OIDC-ready design (stateless JWT filter).
- **Multi-tenancy**: `userId` FK on every table; SecurityContext supplies the user; all repository queries scoped by `userId`.
- **DB**: `sqlite-jdbc` (or a community SQLite dialect for Hibernate). Schema managed by Flyway for repeatable migrations.

## Repository Layout

```
pension-planner/
├── backend/
│   ├── pom.xml
│   └── src/main/java/... (com.pensionplanner)
│   └── src/main/resources/
│       ├── application.yml
│       └── db/migration/V1__init.sql ...
│   └── src/test/java/...
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
│       ├── api/ (axios client + React Query hooks)
│       ├── components/
│       ├── pages/
│       ├── router.tsx
│       └── theme.ts (PrimeReact)
├── docker/
│   └── Dockerfile            # multi-stage: node build → jar build → runtime
├── docker-compose.yml        # volume mount for /data
└── PLAN.md
```

## Database Schema Summary (v9.0)

Every table carries `userId`. Audits are point-in-time snapshots with `action` (`CREATE|UPDATE|DELETE`) + `auditTimestamp`.

- **Users**: `userId` PK, `username` unique, `passwordHash`, `createdAt`.
- **UserSettings**: `id` PK, `userId` FK unique, `targetIncome` (Real, null), `retirementDate` (DateTime).
- **Pension**: `pensionId` PK, `userId`, `name` (max 100), `maturityDate`, `notes`, `status`, `statusDate`, `color` (hex, null).
- **PensionStatement**: `statementId` PK, `pensionId` FK, `userId`, `statementDate`, `planValue`, `projectedAnnualAmount`, `yearlyCharges` (null), `transferValue` (null), `amountPaidIn` (null), `statementNotes` (null).
- **StatePension**: `id` PK, `userId`, `name`, `annualAmount`, `notes` (null).
- **OtherIncome**: `id` PK, `userId`, `name`, `annualAmount`, `notes` (null).
- **Audit tables**: `PensionAudit`, `PensionStatementAudit`, `StatePensionAudit`, `OtherIncomeAudit` — mirror the parent row + `auditId`, `action`, `auditTimestamp`.

## Environment Variables

| Variable             | Description                          | Default          |
|----------------------|--------------------------------------|------------------|
| `SERVER_PORT`        | HTTP port for combined service       | `8080`           |
| `DATABASE_PATH`      | Host file path for SQLite            | `/data/pension.db` |
| `ALLOW_REGISTRATION` | Toggle public sign-up                | `true`           |
| `JWT_SECRET`         | JWT signature secret                 | auto-generated   |

---

# Phase 1 — Boilerplate + Authentication

**Goal**: Runnable skeleton; user registration/login/logout with JWT cookie; multi-tenant security foundation; DB schema; audit infrastructure.

### Backend
- [x] Maven single module `backend`; Spring Boot 3.5.16 parent, Java 25 toolchain.
- [x] `application.yml` wiring env vars (`SERVER_PORT`, `DATABASE_PATH`, `ALLOW_REGISTRATION`, `JWT_SECRET`, `COOKIE_SECURE`).
- [x] SQLite datasource + Flyway migration `V1` creating all base + audit tables and indexes (incl. `userId` indexes).
- [x] Entities/repositories: `User`, `UserSettings`, `Pension`, `PensionStatement`, `StatePension`, `OtherIncome` (+ audit entities).
- [x] Audit snapshots on create/update/delete for the 4 audited entities.
- [x] Security: `SecurityFilterChain`, stateless JWT filter reading HTTP-only cookie, BCrypt `PasswordEncoder`, `SecurityContext` userId injection.
- [x] Auth endpoints: `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `POST /api/v1/auth/logout`, `GET /api/v1/auth/me`, `GET /api/v1/auth/config`.
  - Register returns `403` when `ALLOW_REGISTRATION=false`.
- [x] Multi-tenant service layer pattern: every query/find filtered by `userId` from context.
- [x] `GET /api/v1/settings` + `PUT /api/v1/settings` for `UserSettings` (targetIncome, retirementDate).

### Frontend
- [x] Vite + React + TypeScript + PrimeReact setup; PrimeFlex grid; theme import.
- [x] Axios client with cookie credentials.
- [x] React Query `QueryClientProvider`.
- [x] Routes: `/login`, `/register` (hidden when `ALLOW_REGISTRATION=false` — exposed via `GET /api/v1/auth/config`), protected-route guard.
- [x] Auth context/hooks: `useAuth`, `useLogin`, `useRegister`, `useLogout`.

### Tests
- [x] Unit: JWT token/cookie handling, settings service, audit service.
- [x] Integration (MockMvc): register→login→`/me` happy path; register rejected when disabled; cross-user isolation (user B cannot read user A's settings).

### Acceptance
- [x] `docker compose up` builds a single container (Node build → Maven build → JRE runtime); register/login/logout works; cookie is HttpOnly+SameSite=Lax.

### Phase 1 implementation notes
- **Flyway 12.6.2** pinned (Boot 3.5 manages 11.x) with the `flyway-database-nc-sqlite` plugin for SQLite support.
- **Column naming**: camelCase column names from the spec are preserved via `PhysicalNamingStrategyStandardImpl` (Hibernate 6 otherwise rewrites `userId` → `user_id`).
- **Audit strategy**: snapshots written through `AuditService` at the service layer — `recordCreate` (upon creation), `recordUpdate` (called before mutating, matching the spec's "prior to mutation" wording), `recordDelete`. Rows land in the `*_audit` tables.
- **JWT secret**: SHA-256-hashed from `JWT_SECRET`; auto-generated per boot when unset (sessions invalidate on restart until a secret is configured).
- **CSRF**: disabled; mitigated by `SameSite=Lax` cookie. Revisit if stricter controls are needed.
- **Docker**: `Dockerfile` embeds the frontend build into Spring Boot static resources (single-container model); `docker-compose.yml` mounts `./data:/data`.

---

# Phase 2 — Dashboard (`/`)

**Goal**: Aggregate overview and quick actions.

### Backend
- [x] `GET /api/v1/dashboard` aggregate endpoint (all scoped to user):
  - latest `planValue` per active pension (total current portfolio value);
  - total projected annual income (pensions latest `projectedAnnualAmount` + StatePension + OtherIncome);
  - StatePension & OtherIncome summaries;
  - retirement countdown from `UserSettings.retirementDate`.
- [x] `GET /api/v1/pensions` list (already needed for table; color swatch data included).

### Frontend
- [x] Summary `Card` widgets (4 cards: Portfolio Value, Projected Income vs `targetIncome`, State Pension & Other Income, Retirement Countdown).
- [x] Pensions `DataTable` with color swatch, row quick actions (Edit / Delete / Add Statement) opening the phase-3/4 modals.
- [x] Countdown widget rendering into `retirementDate`; progress indicator vs target income.

### Tests
- [x] Backend: dashboard aggregation with empty data, single pension, multiple pensions + StatePension/OtherIncome; correct latest-statement pick.
- [ ] FE smoke: dashboard renders cards and table.

### Acceptance
- [x] Dashboard shows correct totals and countdown; row actions wired to modals.

---

# Phase 3 — Maintain Pension

**Goal**: Full CRUD on `Pension` + audit trail; used from Dashboard modals and Pension Details.

### Backend
- [x] `PensionService` CRUD (scoped by `userId`):
  - `POST /api/v1/pensions`, `GET /api/v1/pensions`, `GET /api/v1/pensions/{id}`, `PUT /api/v1/pensions/{id}`, `DELETE /api/v1/pensions/{id}`.
- [x] Validation: `name` required & ≤100 chars, `maturityDate` required, `status` enum, `color` hex format, `statusDate` defaults to today.
- [x] Audit: snapshots on create/update/delete persisted to `PensionAudit`.
- [x] Deletion policy decision: block or cascade statements (recommend cascade + audit, or soft-delete; confirm in phase review).

### Frontend
- [x] `PensionFormDialog` (InputText, Calendar with `view="month"` + yearNavigator + yearRange e.g. 2026:2080 for maturityDate, ColorPicker, Dropdown for status, InputTextarea for notes).
- [x] Calendar for `statusDate` as standard icon picker defaulting to today.
- [x] React Query mutations `useCreatePension`, `useUpdatePension`, `useDeletePension`; cache invalidation of dashboard + pensions list.

### Tests
- [x] Backend: create/update/delete scoped to user; validation errors; audit rows written with correct action/timestamp.
- [ ] FE smoke: modal create/edit flow.

### Acceptance
- [x] Pensions CRUD works from Dashboard and (once built) Pension Details; audit history populated.

### Phase 3 implementation notes
- **Deletion policy**: cascade + audit (confirmed in phase review). Statements are audit-snapshotted (DELETE) then removed, then the pension is audited and removed.
- **UI home**: Phase 2 (dashboard) deferred, so the CRUD table + dialog live on the Home page, ready to be reused by dashboard modals later.
- **Audit on update**: `recordUpdate` is invoked before mutating, per the Phase 1 convention ("prior to mutation" wording), so UPDATE snapshots capture the pre-change row.
- **Malformed bodies**: `HttpMessageNotReadableException` (e.g. unknown `status` enum) now returns `400` instead of `500`.
- **PensionStatement entity**: aligned with the V2 domain refactor (no `userId` column; DB-level `ON DELETE CASCADE` FKs).
- **Pension provider fields** (V6): added `providerName`, `policyNumber`, `workplaceName` (all nullable `TEXT(100)`) to `pension` and `pension_audit` tables. Stored in entity, DTO, request, audit snapshot, and displayed on the Pension Details overview panel and edit dialog.

---

# Phase 4 — Maintain Statements

**Goal**: CRUD on `PensionStatement` within a pension, plus Pension Details view with performance chart.

### Backend
- [x] `StatementService` CRUD:
  - `POST /api/v1/pensions/{pensionId}/statements`, `GET` list, `GET /{id}`, `PUT /{id}`, `DELETE /{id}`.
- [x] Ownership checks: statement must belong to user AND to the requested pension.
- [x] Audit snapshots to `PensionStatementAudit` on create/update/delete.
- [x] Validation: `statementDate` required (historical/recent), `planValue` + `projectedAnnualAmount` required, optionals (`yearlyCharges`, `transferValue`, `amountPaidIn`, `statementNotes`).

### Frontend
- [x] Pension Details page `/pensions/{id}`: overview panel (name, maturity, notes, status, statusDate, color swatch).
- [x] Performance bar chart: `planValue` vs `statementDate`, grouped by year, colored by pension color (PrimeReact `Chart`).
- [x] Statements `DataTable` with all fields + per-row Edit/Delete.
- [x] `Add Statement` in `Toolbar`; statement dialog uses Calendar with `monthNavigator`, `yearNavigator`, `showButtonBar={true}`.
- [x] React Query hooks for statements; invalidation of pension detail + dashboard.

### Tests
- [x] Backend: statement CRUD scoped to user+pension; cross-pension statement access denied; audit rows.
- [x] FE smoke: statement dialog opens and submits.

### Acceptance
- [x] Statements CRUD works; chart renders from statement history; dashboard totals use latest statement.

---

# Phase 5 — Maintain State Pension

**Goal**: Manage the single State Pension record per user.

### Backend
- [x] `StatePensionService` (one record per user; upsert semantics):
  - `GET /api/v1/state-pension`, `PUT /api/v1/state-pension` (create-or-update).
- [x] Audit snapshots to `StatePensionAudit`.
- [x] Validation: `startAge` 0–67, `yearlyAmount` ≥ 0, `taxRate` 0–100, `takesEffectYear` ≥ current year.

### Frontend
- [x] State Pension card on Dashboard: editable fields (startAge, yearlyAmount, taxRate, takesEffectYear), Save button.
- [x] React Query `useStatePension`, `useUpsertStatePension`; invalidate cache on save.

### Tests
- [x] Backend: upsert creates once, updates thereafter; audit history; user isolation; validation rejects bad ranges.
- [ ] FE smoke: form loads existing record and saves.

### Acceptance
- [ ] State pension editable from dashboard card; reflected in dashboard totals.

---

# Phase 6 — Maintain Other Income

**Goal**: CRUD on `OtherIncome` + chart page.

### Backend
- [x] `OtherIncomeService` CRUD (scoped by `userId`):
  - `GET /api/v1/other-income`, `POST`, `PUT /{id}`, `DELETE /{id}`.
- [x] Audit snapshots to `OtherIncomeAudit`.
- [x] Validation: `name`, `annualAmount` required.

### Frontend
- [x] `/other-income` page: `Toolbar` with Add button, `DataTable` (name, annualAmount, notes) with per-row Edit/Delete, plus bar chart of `annualAmount` vs `name` (dynamic colors).
  - Consolidated onto Dashboard page: Other Income DataTable below Pensions table; "Add Income" button in toolbar; `/other-income` route removed.
- [x] Dialog for create/edit; React Query hooks; invalidate dashboard + analytics.

### Tests
- [x] Backend: CRUD scoped to user; audit rows; validation.
- [ ] FE smoke: table + chart render; add/edit/delete flows.

### Acceptance
- [x] Other income streams fully maintained; chart updates.

---

# Phase 7 — Analytics (`/analytics`)

**Goal**: Projection + comparison charts per spec §4.2.5.

### Backend
- [x] `GET /api/v1/analytics` aggregate endpoint returning per-pension series and totals:
  - statements time series (planValue, amountPaidIn, yearlyCharges);
  - projections to `retirementDate` (extrapolate from latest planValue / projectedAnnualAmount trajectory);
  - StatePension + OtherIncome annual amounts;
  - `targetIncome` from settings.
- [x] Keep projection math server-side so frontend stays presentational.

### Frontend
- [x] Retirement Runway / Growth Projection chart: multi-year forward projection with vertical line at `retirementDate` + milestone markers.
- [x] Growth vs. Cost grouped bar/line: per pension (planValue − amountPaidIn) vs cumulative yearlyCharges.
- [x] Combined Growth vs. Target stacked bar: per-pension segments (stored colors) + StatePension + OtherIncome, with horizontal `targetIncome` overlay line.
- [x] Historical Portfolio Trend: multi-line chart of individual + total planValue over past statement dates.
- [x] Consistent color mapping util shared with Dashboard/Pension views.

### Tests
- [x] Backend: projection math unit tests (simple + compound growth, edge dates); aggregation across pensions.
- [ ] FE smoke: all four charts render with fixture data.

### Acceptance
- [x] Charts reflect DB data and targets; reference line at retirement date.

---

# Phase 8 — Settings, Operations & Polish

**Goal**: Settings page + production hardening + docs.

### Backend
- [x] `GET/PUT /api/v1/settings` finalized (already scaffolded in Phase 1); expose `ALLOW_REGISTRATION` flag for UI.
- [x] **Audit toggle** (`V7__audit_enabled_setting.sql`): `auditEnabled` boolean on `user_settings` (default `true`). `AuditService` receives `userId` as an argument at each record call site and checks the setting before writing to any audit table. When disabled, audit writes are skipped silently.
- [x] **Audit viewing** (`AuditController` + `AuditReadService` + DTOs): read-only `GET /api/v1/audit/pensions/{id}`, `/statements/{id}`, `/other-income/{id}`. Rows are filtered by userId (ownership checked via the owning entity) and returned in `auditTimestamp` **descending** order. Repositories: `findByPensionIdOrderByAuditTimestampDesc`, `findByStatementIdOrderByAuditTimestampDesc`, `findByIdOrderByAuditTimestampDesc`.
- [x] **Audit purge/snapshot on toggle** (`AuditService.purgeUserAudits` / `snapshotUserAudits`): `UserSettingsService.update()` reacts to a transition in `auditEnabled`. Turning it **off** deletes every audit row for the user (`deleteByUserId` on pension/state-pension/other-income audits + `deleteByPensionIdIn` for statement audits, scoped via the user's current pensions). Turning it **on** writes a `CREATE` snapshot for each of the user's existing pensions (plus their statements), state pension and other income. Repos gained `deleteBy*` derived methods; `AuditService` now also depends on the four entity repositories.
- [x] **Ops hardening (partial)**: `/health` endpoint (`HealthController` — checks SQLite connectivity via the Hikari pool, returns `{"status":"UP"}` / `503`; `anyRequest().permitAll()` already exposes it) + `server.shutdown: graceful`. Remaining: rate limiting on auth endpoints, security headers, CORS restriction.
- [x] **Docker multi-stage build** (`Dockerfile`): `node:24-alpine` builds the frontend → `maven:3.9-eclipse-temurin-25` packages the jar with the frontend embedded under `src/main/resources/static` → `eclipse-temurin:25-jre` runtime image (~181 MB). Runs as non-root (uid 1000 = standard first host user, so the bind-mounted `./data` stays writable), `JAVA_OPTS` silences the SQLite native-access warning + uses `UseSerialGC`, `curl` installed for the healthcheck. `.dockerignore` keeps build context slim (`node_modules`/`target`/`dist`/`data` excluded). `docker-compose.yml` ports 8080, mounts `./data:/data`, passes env through (`ALLOW_REGISTRATION`, `JWT_SECRET`, `JWT_TTL_SECONDS` — now a configurable `application.yml` property, `COOKIE_SECURE`), and adds a `/health` healthcheck (30s interval, 15s start period). Verified end-to-end: fresh compose up runs V1–V7 migrations, register/login/create-pension API works, frontend served at `/`, and data persists across `docker compose restart`. `HealthControllerIntegrationTest` added (102 tests pass).
- [x] **GraalVM native-image note** (spec §1.2): README documents the optional `-Pnative` profile; requires generated runtime hints for Hibernate/Flyway/JDBC before it can compile, kept out of the default build.

### Frontend
- [x] `/settings` page: targetIncome + retirementDate (Calendar with `view="month"`, yearNavigator, yearRange 2026:2080).
- [x] **Audit toggle** in `SettingsDialog`: `InputSwitch` for `auditEnabled`, persisted via PUT /settings. Turning the switch **off** shows a `confirmDialog` warning that all audit history will be permanently deleted; only on confirmation does it save (the backend then purges the data).
- [x] **Audit History**: `AuditDialog` (read-only table: Action tag, Timestamp, entity columns). History (`pi-history`) button on the Actions column of the Pensions, Other Income, and Statement tables — hidden when `auditEnabled` is false. Data fetched via `auditApi`/`useAudit` hooks.
- [ ] Auth UX: show/hide registration based on server flag; session expiry handling; consistent empty/loading/error states across pages.

### Tests
- [x] `AuditServiceTest`: existing snapshot tests enable audit; `pensionSkipsAuditWhenDisabled` and `statementSkipsAuditWhenDisabled` verify no writes when setting off; `purgeDeletesAllAuditTypesForUser`, `purgeHandlesUserWithNoPensions`, `snapshotWritesCreateForEveryRecord` cover the toggle transitions.
- [x] `UserSettingsServiceTest`: `updateStoresAuditEnabled` verifies the flag is persisted; `disablingAuditPurgesAuditData`, `enablingAuditSnapshotsRecords`, `noAuditActionWhenSettingUnchanged` verify the transition behaviour.
- [x] `AuditControllerIntegrationTest`: verifies each audit endpoint returns rows in `auditTimestamp` descending order (UPDATE before CREATE), that UPDATE snapshots capture the pre-change value, and that a user cannot read another user's audit records (404).
- [x] `HealthControllerIntegrationTest`: `/health` returns `200 {"status":"UP"}` without authentication.
- [ ] FE smoke: settings save reflects on dashboard.

### Acceptance
- [x] Single container runs end-to-end; settings drive dashboard + analytics; README covers env vars and compose usage (Docker verified with a fresh compose up + restart persistence test).

---

# Phase 9 — Tags

**Goal**: User-defined labels applied to Pensions and Other Income; multi-select in dialogs; displayed in DataTables.

### Backend
- [x] `Tag` entity: `id`, `userId`, `name` (user-scoped; many per user).
- [x] `PensionTag` and `OtherIncomeTag` join tables via Flyway `V5__tags.sql`.
- [x] `TagService`: list for user, create, delete (with cascade cleanup of join records).
- [x] `TagController`: `GET /api/v1/tags`, `POST /api/v1/tags`, `DELETE /api/v1/tags/{id}`.
- [x] `PensionService` / `OtherIncomeService`: `syncTags` on create/update; delete join records on entity delete.
- [x] `PensionDto` / `OtherIncomeDto`: include `List<TagDto> tags`.
- [x] `PensionRequest` / `OtherIncomeRequest`: include `List<Long> tagIds` (nullable).

### Frontend
- [x] `Tag` and `TagRequest` types; `tagsApi` (list, create, delete); `useTags`, `useCreateTag`, `useDeleteTag` hooks.
- [x] `PensionFormDialog`: `MultiSelect` with chip display for tag selection; tags column in pensions DataTable.
- [x] `OtherIncomeDialog`: `MultiSelect` with chip display for tag selection; tags column in other income DataTable.

### Tests
- [x] `TagServiceTest`: list scoped to user, create, delete with join cleanup, not-found / cross-user rejection.
- [x] `TagControllerIntegrationTest`: create/list/delete flow, validation rejects blank name, per-user isolation.
- [x] `PensionServiceTest` / `OtherIncomeServiceTest`: updated for new `tagIds` parameter.
- [ ] FE smoke: tag creation and assignment in pension/income dialogs.

### Acceptance
- [x] Tags can be created and assigned to pensions and other income; tags display as chips in DataTables.
- [x] Deleting a tag removes it from all pension/income assignments.

---

## Cross-cutting considerations

- **Audit review**: Provide read API for audit tables (e.g. `GET /api/v1/audit/{entity}/{id}`) or leave for a later "History" phase — confirm scope in Phase 1.
- **Deletion policy** for pensions with statements (cascade vs block vs soft-delete) — decide during Phase 3.
- **Currency/rounding**: standardize monetary handling (2-dp) across calculations.
- **Migration path to OIDC** (spec §2.2): keep auth abstraction (JwtService) isolated for later provider swap.

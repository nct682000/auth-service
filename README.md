# Auth Service

A production-grade authentication and authorization service built with **Java 21** and **Spring Boot 3.5**. Designed as the central identity provider for microservice architectures — handling everything from token issuance to brute-force protection, with the security depth and engineering quality that real systems require.

---

## Why This Project Exists

Most auth implementations either cut corners on security or over-engineer to the point of being unmaintainable. This service aims to be the reference implementation that sits in between — secure by default, clearly structured, and built to be deployed, not just demonstrated.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (LTS) |
| Framework | Spring Boot 3.5 |
| Security | Spring Security 6 |
| Token | JWT · JJWT 0.13 · HS256 |
| Database | PostgreSQL 17 |
| Cache / Token store | Redis 8 |
| Schema migration | Flyway |
| Containerization | Docker + Docker Compose |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build tool | Maven Wrapper |

---

## Features

### Core Authentication
| Endpoint | Description |
|---|---|
| `POST /register` | Create account — BCrypt hashed password, default role assigned |
| `POST /login` | Authenticate — returns signed access token + refresh token |
| `POST /logout` | Revoke current session's refresh token from Redis |
| `POST /logout-all` | Bump `token_version` — invalidates every active session across all devices |
| `POST /refresh` | Exchange refresh token for a new access token (with rotation) |
| `PUT /change-password` | Requires current password — bumps `token_version` on success (revokes every session) |
| `POST /forgot-password` | Issues a 6-digit OTP keyed by email, BCrypt-hashed in Redis with a 10-min TTL. Always returns 200 to prevent email enumeration |
| `POST /reset-password` | Validates the OTP (single-use), sets the new password, invalidates all sessions |

### User API
| Endpoint | Auth | Description |
|---|---|---|
| `GET /users/me` | Required | Returns the current authenticated user's profile — id, username, email, status, roles, joined date |

### Internationalization (i18n)
All API responses (`message` field) and Bean Validation error messages are fully localized. Language is resolved from the `Accept-Language` request header. Supported locales: **English** (default), **Vietnamese**.

```http
Accept-Language: vi
→ { "message": "Đăng nhập thành công" }

Accept-Language: en
→ { "message": "Login successful" }
```

Unsupported locales fall back to English automatically.

### Authorization — RBAC

Three-level hierarchy: **User → Role → Permission**

```
User ──< user_role >── Role ──< role_permission >── Permission
```

Permissions are embedded in the JWT payload so downstream services can make authorization decisions without a database call.

#### Permission Naming Convention

All permissions follow the `resource:action:scope` format:

| Segment | Values | Meaning |
|---|---|---|
| `resource` | `profile`, `role`, `permission` | What is being acted on |
| `action` | `read`, `write`, `delete`, `manage` | What operation is performed |
| `scope` | `own`, `all` | Own resources only, or all resources |

#### Permission Matrix

| Permission | `user` role | `admin` role | Description |
|---|:---:|:---:|---|
| `profile:read:own` | ✓ | ✓ | Read own profile |
| `profile:write:own` | ✓ | ✓ | Edit own profile |
| `profile:delete:own` | ✓ | ✓ | Delete own account |
| `profile:read:all` | | ✓ | Read any user's profile |
| `profile:write:all` | | ✓ | Edit any user's profile or status |
| `role:manage` | | ✓ | Create, delete, and assign roles |
| `permission:manage` | | ✓ | Create, delete, and assign permissions |

#### Endpoint → Permission Mapping

| Method | Endpoint | Required Permission |
|---|---|---|
| `GET` | `/users/me` | `profile:read:own` |
| `PUT` | `/change-password` | `profile:write:own` |
| `GET` | `/admin/users` | `profile:read:all` |
| `GET` | `/admin/users/{id}` | `profile:read:all` |
| `PATCH` | `/admin/users/{id}/status` | `profile:write:all` |
| `POST` | `/admin/users/{id}/roles` | `profile:write:all` |
| `DELETE` | `/admin/users/{id}/roles/{roleId}` | `profile:write:all` |
| `GET` | `/admin/roles` | `role:manage` |
| `POST` | `/admin/roles` | `role:manage` |
| `DELETE` | `/admin/roles/{id}` | `role:manage` |
| `POST` | `/admin/roles/{id}/permissions` | `role:manage` |
| `DELETE` | `/admin/roles/{id}/permissions/{permId}` | `role:manage` |
| `GET` | `/admin/permissions` | `permission:manage` |
| `POST` | `/admin/permissions` | `permission:manage` |
| `DELETE` | `/admin/permissions/{id}` | `permission:manage` |

Authorization is enforced per-endpoint via `@PreAuthorize("hasAuthority('...')")` — no blanket URL rules in `SecurityConfig`.

### Account Lifecycle

| Status | Meaning |
|---|---|
| `ACTIVE` | Normal, fully operational |
| `LOCKED` | Auto-locked after too many failed login attempts |
| `DISABLED` | Manually disabled by an admin |

### Admin API

Full RBAC management — all endpoints require an `admin` role:

| Group | Endpoints | Description |
|---|---|---|
| User management | `GET /admin/users`, `GET /admin/users/{id}` | List and view user profiles with pagination |
| Account control | `PATCH /admin/users/{id}/status` | Lock, disable, or re-activate accounts |
| Role assignment | `POST/DELETE /admin/users/{id}/roles` | Assign or remove roles from a user |
| Role management | `GET/POST/DELETE /admin/roles` | Create, list, and delete roles |
| Permission assignment | `POST/DELETE /admin/roles/{id}/permissions` | Assign or remove permissions from a role |
| Permission management | `GET/POST/DELETE /admin/permissions` | Create, list, and delete permissions |

---

## Security Design

### Token Strategy
- **Access token** — short-lived (15 min), stateless JWT, validated per request by `JwtAuthenticationFilter`
- **Refresh token** — long-lived (7 days), stored in Redis with TTL, revoked on logout
- **Token versioning** — `token_version` is embedded in every access token. Bumping the version (on logout-all or password change) immediately invalidates all existing tokens, without a blocklist

### Brute-Force Protection
- Failed login attempts tracked in Redis (`login_attempts:{username}`) via atomic `INCR` with a rolling TTL window
- Account auto-locked after a configurable threshold (default: 5 attempts / 15 min) — sets `status=LOCKED` and `locked_until` on the user row
- **Time-based auto-unlock** — at the next login attempt past `locked_until`, the account auto-unlocks. No scheduled job required, identical UX to admin-driven unlock from the user's perspective
- Pre-check happens **before** BCrypt to avoid CPU exhaustion on locked accounts
- The triggering attempt still returns 401 `INVALID_CREDENTIALS`, not 423 `ACCOUNT_LOCKED` — leaking lockout on the trigger attempt would help an attacker enumerate the threshold
- Tunable via `auth.security.max-login-attempts` and `auth.security.lockout-window` (no code change, K8s ConfigMap-friendly)

### Password Handling
- BCrypt with cost factor 12 — ~400ms per hash, infeasible to brute-force at scale
- Passwords are never stored, logged, or returned in any response
- OTPs are BCrypt-hashed before storage in Redis and are single-use

### Email Delivery (Forgot-Password OTP)

Outbound mail goes through a small `MailSender` interface so the implementation can be swapped without touching business logic. Two implementations ship today, selected at runtime via `auth.mail.provider`:

| Provider value | Active bean | Use case |
|---|---|---|
| `resend` (prod default) | `ResendMailSender` | HTTPS API key — easiest setup. Free 100/day. Calls `POST https://api.resend.com/emails` via Spring 6 `RestClient`. |
| `logging` | `LoggingMailSender` | Local dev — writes the OTP to the application log. No external credentials required. |

**Resend setup:**

1. Sign up at [resend.com](https://resend.com).
2. Visit [resend.com/api-keys](https://resend.com/api-keys) and create a new API key. Copy the `re_xxxxxxxxxxxxxxxx` value.
3. Set `RESEND_API_KEY=re_xxx` and `AUTH_MAIL_PROVIDER=resend`.
4. Sandbox mode (`AUTH_MAIL_FROM_ADDRESS=onboarding@resend.dev`) only delivers to the email you registered your Resend account with — perfect for local testing. For real users, verify a domain at [resend.com/domains](https://resend.com/domains) and switch `AUTH_MAIL_FROM_ADDRESS` to `noreply@your-domain.com`.

### Audit Logging _(planned)_
Every security event (login success/failure, logout, password change, token refresh) is logged with timestamp, IP address, and user agent — async, to never add latency to the request path.

### Request Tracing
Every request carries a `traceId` (from `X-Request-ID` header or auto-generated UUID), stored in MDC so it appears on every log line in that request. START/END logs include method, URI, status, and duration.

---

## API Response Format

All endpoints return a consistent envelope:

```json
{
  "code": "AUTH-002-200",
  "message": "Login successful",
  "data": {},
  "isSuccess": true,
  "timestamp": "2026-04-05T10:00:00"
}
```

Error responses follow the same structure — `data` is omitted when null.

---

## Getting Started

### Prerequisites
- Docker + Docker Compose
- Java 21
- Maven (or use the included `./mvnw` wrapper)

### Run with Docker Compose

```bash
# 1. Copy the environment template
cp docker/.env.example docker/.env

# 2. Fill in your values in docker/.env
#    At minimum: DB_PASSWORD, REDIS_PASSWORD, JWT_SECRET (min 32 chars)
#    Generate a strong JWT secret:
openssl rand -base64 32

# 3. Start the full stack
cd docker && docker-compose up -d
```

The service starts on `http://localhost:8080` with context path `/api/v1/auth`.

### Run Locally (without Docker)

```bash
# Start PostgreSQL and Redis (via Docker or local install)
# Then run with your config file:
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.config.location=classpath:auth-dev.yml"
```

### Run Tests

```bash
./mvnw test
```

> Test suite is planned for Phase 7 — unit tests with Mockito and integration tests with Testcontainers (real PostgreSQL + Redis).

---

## Project Structure

```
src/main/java/.../authservice/
├── config/          # SecurityConfig, JwtConfig, RedisConfig, I18nConfig
├── controller/      # AuthController, UserController, AdminController
├── service/         # AuthService, JwtService, RedisService, UserService, AdminService
│                    # UserDetailsServiceImpl, MessageResolver, AuthClaims
├── filter/          # JwtAuthenticationFilter, RequestLoggingFilter
├── handler/         # GlobalExceptionHandler
├── entity/          # User, Role, Permission, BaseEntity, AuthUserDetails
├── repository/      # UserRepository, RoleRepository, PermissionRepository
├── mapper/          # UserMapper
├── dto/
│   ├── APIResponse.java     # Unified response envelope
│   ├── request/     # LoginRequestDTO, RegisterRequestDTO, RefreshTokenRequestDTO,
│   │                # LogoutRequestDTO, CreateRoleRequestDTO, CreatePermissionRequestDTO,
│   │                # AssignRolesRequestDTO, AssignPermissionsRequestDTO,
│   │                # UpdateUserStatusRequestDTO, PageRequests
│   └── response/    # LoginResponseDTO, UserProfileResponseDTO, RoleResponseDTO,
│                    # RoleSummaryResponseDTO, PermissionResponseDTO, PageResponseDTO
├── enumeration/     # UserStatus, ResponseCode, RoleEnum, AccountPolicy
└── exception/       # AuthException, UserNotFoundException, RoleNotFoundException,
                     # PermissionNotFoundException, InvalidTokenException, RevokedTokenException
                     # (duplicate-conflict cases — username/email/role/permission — are translated
                     #  centrally from DataIntegrityViolationException in GlobalExceptionHandler,
                     #  so no per-case "AlreadyExists" exception classes are needed)

src/main/resources/
├── db/migration/    # V1 schema · V2 mappings · V3 FKs · V4 indexes
│                    # V5 seed roles/permissions · V6 soft-delete indexes
│                    # V7 default admin user · V8 rename permissions (resource:action:scope)
│                    # V9 add descriptions to role/permission
│                    # V10 partial unique indexes for soft-delete
│                    # V11 created_at indexes for pagination
│                    # V12 locked_until column (brute-force lockout)
├── messages.properties     # i18n — English (default)
├── messages_vi.properties  # i18n — Vietnamese
├── auth.yml                # Application config — production (all secrets via env vars)
└── auth-dev.yml            # Application config — local development

docker/
├── Dockerfile
├── docker-compose.yml
└── .env.example

docs/
├── SCOPE.md         # Full feature specification
└── PLAN.md          # Phased implementation blueprint
```

---

## Configuration

All sensitive values are injected via environment variables — no secrets in the repository.

| Variable | Description | Source |
|---|---|---|
| `DB_URL` | JDBC connection URL | ConfigMap |
| `DB_USERNAME` | Database user | Secret |
| `DB_PASSWORD` | Database password | Secret |
| `REDIS_HOST` | Redis hostname | ConfigMap |
| `REDIS_PASSWORD` | Redis password | Secret |
| `JWT_SECRET` | HS256 signing key (min 32 chars) | Secret |
| `JWT_ACCESS_EXPIRATION` | Access token TTL in ms (default: 900000) | ConfigMap |
| `JWT_REFRESH_EXPIRATION` | Refresh token TTL in ms (default: 604800000) | ConfigMap |
| `AUTH_MAX_LOGIN_ATTEMPTS` | Failed-login threshold before lockout (default: 5) | ConfigMap |
| `AUTH_LOCKOUT_WINDOW` | Lockout duration & rolling failure window (default: `15m`) | ConfigMap |
| `AUTH_OTP_TTL` | Password-reset OTP validity (default: `10m`) | ConfigMap |
| `AUTH_MAIL_PROVIDER` | `resend` (HTTP API, default) · `logging` (log only, dev) | ConfigMap |
| `AUTH_MAIL_FROM_ADDRESS` | Display "From" address (default: `onboarding@resend.dev`) | ConfigMap |
| `AUTH_MAIL_FROM_NAME` | Display "From" name (default: `Auth Service`) | ConfigMap |
| `RESEND_API_KEY` | Resend API key (`re_xxx`) — required when `AUTH_MAIL_PROVIDER=resend` | Secret |

The `Source` column maps directly to Kubernetes `Secret` and `ConfigMap` objects for production deployment.

---

## Deployment

The service is containerized and structured for Kubernetes deployment:

- **Docker image** — multi-stage build (Maven build → JRE 21 runtime), runs as non-root
- **K8s manifests** — Deployment, Service, ConfigMap, Secret in `k8s/` (in progress)
- **Zero-secret repo** — all credentials injected at runtime via env vars

---

## What Is Out of Scope

Intentionally excluded to keep the service focused:

- **OAuth2 / Social login** — belongs in a separate identity provider layer
- **MFA / TOTP** — significant scope addition, separate service concern
- **Email delivery** — interface is defined; implementation delegated to a notification service
- **User profile management** — name, avatar, preferences are application concerns, not auth concerns

---

## Implementation Progress

- [x] Phase 1 — Foundation & configuration
- [x] Phase 2 — Register + Login
- [x] Phase 3 — Token lifecycle (refresh, logout, logout-all)
- [x] Phase 4 — User & Admin APIs
  - [x] `GET /users/me` — authenticated user profile
  - [x] i18n — `Accept-Language`-driven localization for all response messages and validation errors (EN / VI)
  - [x] Admin endpoints — user management, RBAC management (roles, permissions)
  - [x] Per-endpoint RBAC via `@PreAuthorize` with `resource:action:scope` permission naming
- [x] Phase 5 — Security hardening
  - [x] Brute-force protection with time-based auto-unlock
  - [x] `PUT /change-password` — bumps `token_version` to revoke every session on success
  - [x] `POST /forgot-password` + `POST /reset-password` — 6-digit OTP, BCrypt-hashed in Redis, 10-min TTL, single-use
  - [ ] _(deferred)_ Audit log — out of scope for this phase; revisit in Phase 6+
- [ ] Phase 6 — Swagger / OpenAPI documentation
- [ ] Phase 7 — Test suite (unit + integration with Testcontainers)
- [ ] Phase 8 — Final polish & K8s manifests

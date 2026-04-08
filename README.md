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
| `PUT /change-password` | _(planned)_ Requires current password — bumps token version on success |
| `POST /forgot-password` | _(planned)_ Generates a short-lived OTP, sends via email |
| `POST /reset-password` | _(planned)_ Validates OTP, sets new password, invalidates all sessions |

### User API
| Endpoint | Auth | Description |
|---|---|---|
| `GET /users/me` | Required | Returns the current authenticated user's profile — id, username, email, status, roles, joined date |

### Authorization — RBAC
Three-level hierarchy: **User → Role → Permission**

```
User ──< user_role >── Role ──< role_permission >── Permission
```

Permissions are embedded in the JWT payload so downstream services can make authorization decisions without a database call.

### Account Lifecycle
| Status | Meaning |
|---|---|
| `ACTIVE` | Normal, fully operational |
| `LOCKED` | Auto-locked after too many failed login attempts |
| `DISABLED` | Manually disabled by an admin |

### Admin API _(planned)_
Full RBAC management behind `ADMIN_PANEL` permission:
- Manage users, roles, and permissions
- Lock / unlock / disable accounts
- Assign or revoke roles from users

---

## Security Design

### Token Strategy
- **Access token** — short-lived (15 min), stateless JWT, validated per request by `JwtAuthenticationFilter`
- **Refresh token** — long-lived (7 days), stored in Redis with TTL, revoked on logout
- **Token versioning** — `token_version` is embedded in every access token. Bumping the version (on logout-all or password change) immediately invalidates all existing tokens, without a blocklist

### Brute-Force Protection _(planned)_
- Failed login attempts tracked in Redis per username with a rolling TTL window
- Account auto-locked after a configurable threshold (default: 5 attempts / 15 min)
- Configurable via `application.yml` — no code changes required

### Password Handling
- BCrypt with cost factor 12 — ~400ms per hash, infeasible to brute-force at scale
- Passwords are never stored, logged, or returned in any response
- OTPs are BCrypt-hashed before storage in Redis and are single-use

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
  "data": { ... },
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
├── config/          # SecurityConfig, JwtConfig, RedisConfig
├── controller/      # AuthController, UserController
├── service/         # AuthService, JwtService, RedisService, UserService, UserDetailsServiceImpl, AuthClaims
├── filter/          # JwtAuthenticationFilter, RequestLoggingFilter
├── handler/         # GlobalExceptionHandler
├── entity/          # User, Role, Permission, BaseEntity, AuthUserDetails
├── repository/      # UserRepository, RoleRepository
├── dto/
│   ├── request/     # LoginRequestDTO, RegisterRequestDTO, RefreshTokenRequestDTO, LogoutRequestDTO
│   └── response/    # LoginResponseDTO, UserProfileResponseDTO
├── mapper/          # UserMapper
├── enumeration/     # UserStatus, ResponseCode, AccountPolicy, RoleEnum
└── exception/       # AuthException, InvalidTokenException, RevokedTokenException, UserNotFoundException, ...

src/main/resources/
├── db/migration/    # Flyway SQL (V1 → schema, V2 → mappings, V3 → FKs, V4 → indexes, V5 → seed, V6 → soft-delete indexes)
└── auth.yml         # Application config (all secrets via env vars)

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
- [ ] Phase 4 — User & Admin APIs (GET /users/me done)
- [ ] Phase 5 — Security hardening (brute-force, audit log, password reset)
- [ ] Phase 6 — Swagger / OpenAPI documentation
- [ ] Phase 7 — Test suite (unit + integration with Testcontainers)
- [ ] Phase 8 — Final polish & K8s manifests

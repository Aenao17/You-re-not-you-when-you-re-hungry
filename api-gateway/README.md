# API Gateway

The single entry point for the *You're Not You When You're Hungry* food-ordering system. It validates JWT tokens and proxies every request to the appropriate downstream microservice.

---

## Purpose

- Terminate client connections on a single port (8080)
- Validate the `Authorization: Bearer <token>` header before forwarding requests
- Enforce CORS rules for allowed frontend origins
- Route requests to `user-service`, `menu-service`, or `order-service` based on the URL path

The gateway has **no business logic**, **no database**, and exposes **no REST endpoints of its own**.

---

## Technologies

| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Language |
| Spring Boot | 4.0.6 | Application framework |
| Spring Cloud Gateway (WebMVC) | 2025.1.1 | HTTP proxying and routing |
| Spring Security | (Boot managed) | Security filter chain |
| jjwt | 0.11.5 | JWT parsing and validation |
| Lombok | (Boot managed) | Boilerplate reduction |
| Spring Boot Actuator | (Boot managed) | Health and info endpoints |
| Docker | — | Containerisation |

---

## How to Run

### Prerequisites

- Java 17+
- Maven 3.8+ (or use the included `mvnw` wrapper)
- The three downstream services must be reachable (see [root README](../README.md))

### Run locally

```bash
cd api-gateway
./mvnw spring-boot:run
```

The gateway starts on **http://localhost:8080**.

### Run with Docker Compose

```bash
cd api-gateway
docker compose up --build
```

The `compose.yaml` sets `USER_SERVICE_URL`, `MENU_SERVICE_URL`, and `ORDER_SERVICE_URL` to `host.docker.internal` so the gateway can reach locally-running services.

### Environment variables

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `8080` | Gateway listen port |
| `JWT_SECRET` | *(see below)* | HS256 signing secret (Base64) |
| `USER_SERVICE_URL` | `http://localhost:8081` | user-service base URL |
| `MENU_SERVICE_URL` | `http://localhost:8082` | menu-service base URL |
| `ORDER_SERVICE_URL` | `http://localhost:8083` | order-service base URL |

---

## Routing Table

| Route ID | Path Predicate | Upstream Service |
|---|---|---|
| `user-service-auth` | `/api/auth/**` | `USER_SERVICE_URL` |
| `user-service-users` | `/api/users/**` | `USER_SERVICE_URL` |
| `user-service-admin` | `/api/admin/**` | `USER_SERVICE_URL` |
| `menu-service` | `/api/menu/**` | `MENU_SERVICE_URL` |
| `order-service` | `/api/orders/**` | `ORDER_SERVICE_URL` |

---

## Security Rules

The gateway enforces the following access policy before forwarding:

| Path / Method | Access |
|---|---|
| `OPTIONS /**` | Public (CORS preflight) |
| `/actuator/health`, `/actuator/info` | Public |
| `POST /api/auth/register` | Public |
| `POST /api/auth/login` | Public |
| `GET /api/menu/**` | Public |
| `POST /api/menu/**` | Authenticated |
| `PUT /api/menu/**` | Authenticated |
| `PATCH /api/menu/**` | Authenticated |
| `DELETE /api/menu/**` | Authenticated |
| `/api/users/**` | Authenticated |
| `/api/admin/**` | Authenticated |
| `/api/orders/**` | Authenticated |

Role-level authorisation (`ROLE_ADMIN` vs `ROLE_CUSTOMER`) is enforced inside each downstream service.

### CORS

Allowed origins: `http://localhost:5173` (Vite frontend), `http://localhost:8100` (Ionic frontend)

---

## Key Components

| Class | Package | Role |
|---|---|---|
| `ApiGatewayApplication` | `nttdata.apigateway` | Spring Boot entry point |
| `SecurityConfig` | `nttdata.apigateway.config` | Security filter chain, CORS, public path rules |
| `JwtAuthFilter` | `nttdata.apigateway.security` | Extracts Bearer token; populates `SecurityContext` |
| `JwtService` | `nttdata.apigateway.security` | Parses and validates JWT; extracts username and roles |

---

## Actuator Endpoints

| Endpoint | URL |
|---|---|
| Health | http://localhost:8080/actuator/health |
| Info | http://localhost:8080/actuator/info |

---

## Business Rules

1. Requests without a valid JWT to a protected path are rejected with **401 Unauthorized** before they reach any service.
2. The JWT secret must match the secret used by `user-service` to issue tokens. Both read the same `JWT_SECRET` environment variable.
3. Token validation is stateless — no session store is involved.
4. The `/api/internal/**` path used for internal service-to-service calls is **not routed** through the gateway and should not be exposed externally.

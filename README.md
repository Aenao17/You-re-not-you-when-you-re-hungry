# You're Not You When You're Hungry

A food-ordering microservices system built with Spring Boot 4 and Spring Cloud Gateway.

---

## Architecture Overview

```
Client (localhost:5173 / localhost:8100)
            │
            ▼
    ┌───────────────────┐
    │   API Gateway     │  :8080
    │  (JWT validation  │
    │   + routing)      │
    └────────┬──────────┘
             │
    ┌────────┼────────────────┐
    │        │                │
    ▼        ▼                ▼
┌──────────┐ ┌────────────┐ ┌────────────┐
│  user-   │ │   menu-    │ │   order-   │
│ service  │ │  service   │ │  service   │
│  :8081   │ │   :8082    │ │   :8083    │
│ (PG:5433)│ │ (PG:5434)  │ │ (PG:5435)  │
└──────────┘ └────────────┘ └────────────┘
```

All external traffic enters through the **API Gateway** on port **8080**. The gateway validates the JWT token and forwards requests to the appropriate downstream service. Services do not expose themselves directly to clients.

---

## Services

| Service | Port | Description |
|---|---|---|
| [api-gateway](./api-gateway/README.md) | 8080 | Single entry point — JWT validation and route proxying |
| [user-service](./user-service/README.md) | 8081 | User registration, login, JWT issuance, user management |
| [menu-service](./menu-service/README.md) | 8082 | Restaurant and menu-item catalogue |
| [order-service](./order-service/README.md) | 8083 | Order placement and lifecycle management |

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.0.6 |
| API Gateway | Spring Cloud Gateway (WebMVC) 2025.1.1 |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| Persistence | Spring Data JPA + PostgreSQL 16 |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Containerisation | Docker + per-service Docker Compose |
| Build tool | Maven 3 |

---

## Authentication & Authorisation

JWT tokens are issued by **user-service** and validated at both the **api-gateway** and each downstream service independently (stateless, no session).

| Role | Capabilities |
|---|---|
| `ROLE_CUSTOMER` | Register, login, view menu, place orders, view/cancel own orders |
| `ROLE_ADMIN` | All customer actions + manage users, restaurants, menu items, update any order status |

**Token TTL:** 30 minutes  
**Algorithm:** HS256

### Pre-seeded Users (password: `password123`)

| Username | Role |
|---|---|
| `admin1` | ADMIN |
| `customer1` | CUSTOMER |
| `customer2` | CUSTOMER |

---

## Running the Full System

### Option 1 — Run each service locally (development)

Start each service from its own directory. Each service uses Spring Boot Docker Compose support to automatically spin up its PostgreSQL database.

```bash
# Terminal 1 — user-service (starts its own PostgreSQL on port 5433)
cd user-service
./mvnw spring-boot:run

# Terminal 2 — menu-service (starts its own PostgreSQL on port 5434)
cd menu-service
./mvnw spring-boot:run

# Terminal 3 — order-service (starts its own PostgreSQL on port 5435)
cd order-service
./mvnw spring-boot:run

# Terminal 4 — api-gateway
cd api-gateway
./mvnw spring-boot:run
```

All four services must be running before sending requests through the gateway.

### Option 2 — Run each service with Docker Compose

Build and start each service individually with Docker Compose (includes its database):

```bash
cd user-service  && docker compose up --build -d
cd menu-service  && docker compose up --build -d
cd order-service && docker compose up --build -d
cd api-gateway   && docker compose up --build -d
```

> **Note:** The api-gateway `compose.yaml` uses `host.docker.internal` to reach the other services. Make sure the other three services are running before starting the gateway.

---

## API Entry Point

All API calls go through the gateway at `http://localhost:8080`.

| Route prefix | Forwarded to |
|---|---|
| `/api/auth/**` | user-service |
| `/api/users/**` | user-service |
| `/api/admin/**` | user-service |
| `/api/menu/**` | menu-service |
| `/api/orders/**` | order-service |

### Quick-start example

```bash
# 1. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"pass1234","email":"alice@example.com","firstName":"Alice","lastName":"Smith"}'

# 2. Login — copy the accessToken from the response
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"pass1234"}'

# 3. Browse restaurants (public)
curl http://localhost:8080/api/menu/restaurants

# 4. Place an order (use the token from step 2)
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"restaurantId":1,"items":[{"menuItemId":1,"quantity":2}]}'
```

---

## Inter-Service Communication

The **order-service** calls downstream services directly (bypassing the gateway) over plain HTTP:

| Caller | Target | Endpoint | Purpose |
|---|---|---|---|
| order-service | user-service | `GET /api/internal/users/{username}` | Fetch user ID for new orders |
| order-service | menu-service | `GET /api/menu/restaurants/{id}` | Validate items and snapshot prices |

The `/api/internal/**` path is **not** exposed via the API Gateway.

---

## Swagger / OpenAPI

| Service | URL |
|---|---|
| user-service | http://localhost:8081/swagger-ui.html |
| menu-service | http://localhost:8082/swagger-ui.html |
| order-service | http://localhost:8083/swagger-ui.html |

The API Gateway does not expose a Swagger UI. Use the per-service URLs during development.

---

## Port Reference

| Component | Port |
|---|---|
| API Gateway | 8080 |
| user-service | 8081 |
| menu-service | 8082 |
| order-service | 8083 |
| user-service PostgreSQL | 5433 |
| menu-service PostgreSQL | 5434 |
| order-service PostgreSQL | 5435 |

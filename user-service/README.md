# User Service

Handles user registration, authentication, and account management for the food-ordering system. It is the sole issuer of JWT tokens consumed by all other services.

## Purpose

- Register new users (always assigned the `CUSTOMER` role)
- Authenticate users and issue signed JWT access tokens
- Provide profile information for logged-in users
- Expose admin-only endpoints for listing and retrieving users
- Expose an internal endpoint used by `order-service` to look up users by username

## Technologies

| Technology | Version | Purpose |

| Java | 17 | Language |
| Spring Boot | 4.0.6 | Application framework |
| Spring Security | (Boot managed) | Authentication, authorisation, password hashing |
| Spring Data JPA | (Boot managed) | ORM and repository abstraction |
| PostgreSQL | 16 | Persistent user storage |
| jjwt | 0.11.5 | JWT generation and validation |
| SpringDoc OpenAPI | 2.8.6 | Swagger UI / API documentation |
| Lombok | (Boot managed) | Boilerplate reduction |
| Docker | — | Containerisation |

## How to Run

### Prerequisites

- Java 17+
- Maven 3.8+ (or use `mvnw`)
- Docker (required for the auto-managed PostgreSQL container)

### Run locally

Spring Boot Docker Compose support automatically starts a PostgreSQL 16 container on port **5433**.

cd user-service
./mvnw spring-boot:run

The service starts on **http://localhost:8081**.

### Run with Docker Compose

cd user-service
docker compose up --build

This starts both `user-service-database` (PostgreSQL) and `user-service-app`.

### Environment variables

| Variable | Default | Description |

| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5433/user_db` | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `user` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | `password` | DB password |
| `JWT_SECRET` | _(hardcoded Base64 value)_ | HS256 signing key |

## API Endpoints

All endpoints are also available via the **API Gateway** at `http://localhost:8080`.

### Auth — `/api/auth`

| Method | Path | Auth | Description |

| `POST` | `/api/auth/register` | Public | Register a new user |
| `POST` | `/api/auth/login` | Public | Authenticate and receive a JWT |

#### `POST /api/auth/register`

**Request body:**

{
"username": "alice",
"password": "pass1234",
"email": "alice@example.com",
"firstName": "Alice",
"lastName": "Smith",
"phoneNumber": "+40700000000"
}

**Response `201 Created`:**

{
"id": 4,
"username": "alice",
"email": "alice@example.com",
"firstName": "Alice",
"lastName": "Smith",
"phoneNumber": "+40700000000",
"role": "CUSTOMER"
}

#### `POST /api/auth/login`

**Request body:**

{
"username": "alice",
"password": "pass1234"
}

**Response `200 OK`:**

{
"accessToken": "<jwt>",
"tokenType": "Bearer"
}

### Users — `/api/users`

Requires a valid JWT (`ROLE_CUSTOMER` or `ROLE_ADMIN`).

| Method | Path | Auth | Description |

| `GET` | `/api/users/me` | Authenticated | Get the currently logged-in user's profile |
| `GET` | `/api/users/hello` | Authenticated | Health-check greeting |

### Admin — `/api/admin`

Requires `ROLE_ADMIN`.

| Method | Path | Auth | Description |

| `GET` | `/api/admin/users` | ADMIN | List all users |
| `GET` | `/api/admin/users/{id}` | ADMIN | Get a user by ID |
| `GET` | `/api/admin/users/username/{username}` | ADMIN | Get a user by username |

### Internal — `/api/internal/users`

Not routed through the API Gateway. Used only by `order-service`.

| Method | Path | Auth | Description |

| `GET` | `/api/internal/users/{username}` | None | Return a lightweight user summary |

**Response:**

{
"id": 3,
"username": "customer1",
"email": "customer1@example.com",
"role": "ROLE_CUSTOMER"
}

## Swagger UI

**URL:** http://localhost:8081/swagger-ui.html

The full interactive API documentation is available here while the service is running.

## Data Model

### `User` entity (`users` table)

| Field | Type | Notes |

| `id` | `Long` | Auto-generated primary key |
| `username` | `String` | Unique |
| `password` | `String` | BCrypt-hashed |
| `firstName` | `String` | |
| `lastName` | `String` | |
| `email` | `String` | |
| `phoneNumber` | `String` | |
| `role` | `Role` | Enum: `CUSTOMER`, `ADMIN` |
| `createdAt` | `LocalDateTime` | Set on creation |
| `updatedAt` | `LocalDateTime` | Updated on save |

## Main Business Rules

1. **Registration always assigns `ROLE_CUSTOMER`** — the `role` field in the request body is ignored. Admin users must be seeded or promoted directly in the database.
2. **Passwords are BCrypt-hashed** — plain-text passwords are never stored.
3. **JWT tokens are valid for 30 minutes** and signed with HS256.
4. **Token claims include** the username and authorities (`ROLE_CUSTOMER` / `ROLE_ADMIN`).
5. The **internal endpoint** (`/api/internal/users/{username}`) is unauthenticated and is only intended for internal service-to-service calls; it must not be exposed externally.
6. **Pre-seeded accounts** are created on startup if they do not already exist:
   - `admin1` / `password123` → ADMIN
   - `customer1` / `password123` → CUSTOMER
   - `customer2` / `password123` → CUSTOMER

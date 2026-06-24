# Order Service

Manages the full lifecycle of food orders. Customers place orders against a restaurant's menu, and administrators can confirm, complete, or cancel them.

## Purpose

- Accept order creation requests from authenticated customers
- Validate that all requested menu items belong to the specified restaurant
- Snapshot item names and prices at the time of order creation
- Track order status through the `CREATED → CONFIRMED → COMPLETED` (or `CANCELLED`) lifecycle
- Allow customers to view and cancel their own orders
- Allow administrators to view all orders and update their status

## Technologies

| Technology | Version | Purpose |

| Java | 17 | Language |
| Spring Boot | 4.0.6 | Application framework |
| Spring Security | (Boot managed) | JWT validation, method-level authorisation |
| Spring Data JPA | (Boot managed) | ORM and repository abstraction |
| Spring Validation | (Boot managed) | Request body validation |
| PostgreSQL | 16 | Persistent storage |
| jjwt | 0.11.5 | JWT validation |
| SpringDoc OpenAPI | 2.3.0 | Swagger UI / API documentation |
| Lombok | (Boot managed) | Boilerplate reduction |
| Docker | — | Containerisation |

## How to Run

### Prerequisites

- Java 17+
- Maven 3.8+ (or use `mvnw`)
- Docker (required for the auto-managed PostgreSQL container)
- `user-service` and `menu-service` must be running (order creation calls both)

### Run locally

Spring Boot Docker Compose support automatically starts a PostgreSQL 16 container on port **5435**.

cd order-service
./mvnw spring-boot:run

The service starts on **http://localhost:8083**.

### Run with Docker Compose

cd order-service
docker compose up --build

This starts both `order-service-database` (PostgreSQL) and `order-service-app`.

### Environment variables

| Variable | Default | Description |

| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5435/order_db` | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `order_user` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | `password` | DB password |
| `MENU_SERVICE_URL` | `http://localhost:8082` | Base URL for menu-service |
| `USER_SERVICE_URL` | `http://localhost:8081` | Base URL for user-service |

## API Endpoints

All endpoints are also available via the **API Gateway** at `http://localhost:8080`.

### Orders — `/api/orders`

| Method | Path | Auth | Description |

| `POST` | `/api/orders` | CUSTOMER | Place a new order |
| `GET` | `/api/orders/my` | CUSTOMER | List the authenticated customer's orders |
| `GET` | `/api/orders/{orderId}` | CUSTOMER (own) / ADMIN (any) | Get order details |
| `GET` | `/api/orders` | ADMIN | List all orders |
| `PATCH` | `/api/orders/{orderId}/status` | ADMIN | Update order status |
| `PATCH` | `/api/orders/{orderId}/cancel` | CUSTOMER (own) / ADMIN | Cancel an order |

#### `POST /api/orders` — place a new order

**Request body:**

{
"restaurantId": 1,
"items": [
{ "menuItemId": 1, "quantity": 2 },
{ "menuItemId": 3, "quantity": 1 }
]
}

**Response `201 Created`:**

{
"id": 7,
"userId": 3,
"username": "customer1",
"restaurantId": 1,
"restaurantName": "Demo Italian Restaurant",
"items": [
{
"id": 10,
"menuItemId": 1,
"menuItemName": "Spaghetti Carbonara",
"quantity": 2,
"unitPrice": 28.00,
"subtotal": 56.00
},
{
"id": 11,
"menuItemId": 3,
"menuItemName": "Caesar Salad",
"quantity": 1,
"unitPrice": 18.00,
"subtotal": 18.00
}
],
"totalPrice": 74.00,
"status": "CREATED",
"createdAt": "2026-06-24T16:00:00",
"updatedAt": "2026-06-24T16:00:00"
}

#### `PATCH /api/orders/{orderId}/status` — update status (ADMIN)

**Request body:**

{
"status": "CONFIRMED"
}

## Swagger UI

**URL:** http://localhost:8083/swagger-ui.html

The full interactive API documentation is available here while the service is running.

## Data Model

### `Order` entity (`orders` table)

| Field | Type | Notes |

| `id` | `Long` | Auto-generated primary key |
| `userId` | `Long` | Copied from user-service at creation time |
| `username` | `String` | Copied from JWT principal |
| `restaurantId` | `Long` | Validated against menu-service |
| `restaurantName` | `String` | Snapshotted from menu-service |
| `items` | `List<OrderItem>` | One-to-many |
| `totalPrice` | `BigDecimal` | Sum of all item subtotals |
| `status` | `OrderStatus` | Enum (see below) |
| `createdAt` | `LocalDateTime` | |
| `updatedAt` | `LocalDateTime` | |

### `OrderItem` entity (`order_items` table)

| Field | Type | Notes |

| `id` | `Long` | Auto-generated primary key |
| `menuItemId` | `Long` | Reference to menu-service item |
| `menuItemName` | `String` | Snapshotted at creation |
| `quantity` | `Integer` | Minimum 1 |
| `unitPrice` | `BigDecimal` | Snapshotted at creation |
| `subtotal` | `BigDecimal` | `unitPrice × quantity` |
| `order` | `Order` | Many-to-one |

### `OrderStatus` enum

CREATED → CONFIRMED → COMPLETED
↓ ↓
CANCELLED (terminal)

| Status | Description |

| `CREATED` | Order placed, awaiting confirmation |
| `CONFIRMED` | Restaurant confirmed the order |
| `COMPLETED` | Order fulfilled and delivered |
| `CANCELLED` | Order cancelled — cannot be undone |

## Inter-Service Communication

When creating an order, `order-service` makes two synchronous HTTP calls:

| Call | Endpoint | Purpose |

| Fetch user | `GET {USER_SERVICE_URL}/api/internal/users/{username}` | Retrieve the caller's `userId` |
| Fetch restaurant | `GET {MENU_SERVICE_URL}/api/menu/restaurants/{restaurantId}` | Validate items and snapshot prices/names |

A **dev profile** (`--spring.profiles.active=dev`) replaces both HTTP clients with in-memory fakes (`FakeUserClient`, `FakeMenuClient`) to allow running the service standalone without the other services.

## Main Business Rules

1. **All order items must belong to the requested restaurant.** If any `menuItemId` is not in the restaurant's menu, the request is rejected with `400 Bad Request`.
2. **Item names and prices are snapshotted** at order creation time. Later changes to menu items do not affect existing orders.
3. **Status transitions are strictly enforced:**
   - Only ADMIN can advance status (`CREATED → CONFIRMED → COMPLETED`).
   - `COMPLETED` orders cannot be cancelled.
   - Customers may only cancel their own orders; admins may cancel any order.
4. **Customers can only view their own orders.** Attempting to fetch another customer's order returns `403 Forbidden`.
5. **`totalPrice`** is calculated server-side from snapshotted unit prices and is not accepted from the client.
6. **Minimum quantity per line item is 1.** The `@Min(1)` constraint is enforced on `CreateOrderItemRequest.quantity`.

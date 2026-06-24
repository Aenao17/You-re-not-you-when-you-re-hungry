# Menu Service

Manages the restaurant catalogue and menu items. Public read access allows any client to browse restaurants and their menus without authentication; write operations are restricted to administrators.

---

## Purpose

- Create, update, and delete restaurants
- Add, update, and delete menu items belonging to a restaurant
- Allow any client (authenticated or not) to list restaurants, search by keyword, and view menu items
- Expose restaurant data to `order-service` for order validation and price snapshotting

---

## Technologies

| Technology | Version | Purpose |
|---|---|---|
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

---

## How to Run

### Prerequisites

- Java 17+
- Maven 3.8+ (or use `mvnw`)
- Docker (required for the auto-managed PostgreSQL container)

### Run locally

Spring Boot Docker Compose support automatically starts a PostgreSQL 16 container on port **5434**.

```bash
cd menu-service
./mvnw spring-boot:run
```

The service starts on **http://localhost:8082**.

### Run with Docker Compose

```bash
cd menu-service
docker compose up --build
```

This starts both `menu-service-database` (PostgreSQL) and `menu-service-app`.

### Environment variables

| Variable | Default | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5434/menu_db` | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `menu` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | `password` | DB password |

---

## API Endpoints

All endpoints are also available via the **API Gateway** at `http://localhost:8080`.

### Restaurants — `/api/menu/restaurants`

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/menu/restaurants` | Public | List all restaurants (with their menu items) |
| `GET` | `/api/menu/restaurants/{id}` | Public | Get a restaurant by ID |
| `GET` | `/api/menu/restaurants/search?keyword=` | Public | Search restaurants by name keyword |
| `POST` | `/api/menu/restaurants` | ADMIN | Create a new restaurant |
| `PUT` | `/api/menu/restaurants/{id}` | ADMIN | Update a restaurant |
| `DELETE` | `/api/menu/restaurants/{id}` | ADMIN | Delete a restaurant |

#### `POST /api/menu/restaurants` — request body

```json
{
  "name": "Pasta Palace",
  "description": "Authentic Italian pasta dishes made fresh daily."
}
```

**Response `201 Created`:**
```json
{
  "restaurantId": 3,
  "name": "Pasta Palace",
  "description": "Authentic Italian pasta dishes made fresh daily.",
  "menuItems": []
}
```

---

### Menu Items — `/api/menu/...`

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/menu/items/{itemId}` | Public | Get a single menu item by ID |
| `POST` | `/api/menu/restaurants/{restaurantId}/items` | ADMIN | Add a menu item to a restaurant |
| `PUT` | `/api/menu/items/{itemId}` | ADMIN | Update a menu item |
| `DELETE` | `/api/menu/items/{itemId}` | ADMIN | Delete a menu item |

#### `POST /api/menu/restaurants/{restaurantId}/items` — request body

```json
{
  "name": "Margherita Pizza",
  "price": 32.50
}
```

**Response `201 Created`:**
```json
{
  "id": 5,
  "name": "Margherita Pizza",
  "price": 32.50
}
```

---

## Swagger UI

**URL:** http://localhost:8082/swagger-ui.html

The full interactive API documentation is available here while the service is running.

---

## Data Model

### `Restaurant` entity (`restaurants` table)

| Field | Type | Notes |
|---|---|---|
| `restaurantId` | `Long` | Auto-generated primary key |
| `name` | `String` | Not blank |
| `description` | `String` | Up to 500 characters |
| `menuItems` | `List<MenuItem>` | One-to-many relationship |

### `MenuItem` entity (`menu_items` table)

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | Auto-generated primary key |
| `name` | `String` | Not blank |
| `price` | `BigDecimal` | Must be positive |
| `restaurant` | `Restaurant` | Many-to-one relationship |

---

## Main Business Rules

1. **Public read access** — `GET` requests to any `/api/menu/**` path do not require a JWT token.
2. **Admin-only writes** — `POST`, `PUT`, and `DELETE` operations require a valid JWT with `ROLE_ADMIN`. This is enforced via `@PreAuthorize("hasRole('ADMIN')")` on the service layer.
3. **Cascading deletes** — deleting a restaurant removes all its menu items.
4. **Price snapshotting** — when `order-service` creates an order it reads menu item prices from this service and stores them in the order. Subsequent price changes do not affect existing orders.
5. **Pre-seeded data** is loaded on first startup if no restaurants exist:
   - *Demo Italian Restaurant* — Spaghetti Carbonara, Tiramisu, Caesar Salad
   - *Burger House* — Classic Cheeseburger, BBQ Bacon Burger, Veggie Burger, French Fries

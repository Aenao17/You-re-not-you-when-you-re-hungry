# You're Not You When You're Hungry

A food-ordering microservices system built with Spring Boot 4 and Spring Cloud Gateway.

---

## Architecture Overview

All external traffic enters through the **API Gateway** on port **8080**. The gateway validates the JWT token and forwards requests to the appropriate downstream service. Services do not expose themselves directly to clients.

## Services

| Service | Port | Description |

| [api-gateway](./api-gateway/README.md) | 8080 | Single entry point — JWT validation and route proxying |
| [user-service](./user-service/README.md) | 8081 | User registration, login, JWT issuance, user management |
| [menu-service](./menu-service/README.md) | 8082 | Restaurant and menu-item catalogue |
| [order-service](./order-service/README.md) | 8083 | Order placement and lifecycle management |

## Technology Stack

| Layer | Technology |

| Language | Java 17 |
| Framework | Spring Boot 4.0.6 |
| API Gateway | Spring Cloud Gateway (WebMVC) 2025.1.1 |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| Persistence | Spring Data JPA + PostgreSQL 16 |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Containerisation | Docker + per-service Docker Compose |
| Build tool | Maven 3 |

## Authentication & Authorisation

JWT tokens are issued by **user-service** and validated at both the **api-gateway** and each downstream service independently (stateless, no session).

| Role | Capabilities |

| `ROLE_CUSTOMER` | Register, login, view menu, place orders, view/cancel own orders |
| `ROLE_ADMIN` | All customer actions + manage users, restaurants, menu items, update any order status |

**Token TTL:** 30 minutes

### Pre-seeded Users (password: `password123`)

| Username | Role |

| `admin1` | ADMIN |
| `customer1` | CUSTOMER |
| `customer2` | CUSTOMER |

## Running the Full System

### Option 1 — Run each service locally (development)

Start each service from its own directory. Each service uses Spring Boot Docker Compose support to automatically spin up its PostgreSQL database.

````bash
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


All four services must be running before sending requests through the gateway.

### Option 2 — Run each service with Docker Compose

Build and start each service individually with Docker Compose (includes its database):


cd user-service  && docker compose up --build -d
cd menu-service  && docker compose up --build -d
cd order-service && docker compose up --build -d
cd api-gateway   && docker compose up --build -d


 **Note:** The api-gateway `compose.yaml` uses `host.docker.internal` to reach the other services. Make sure the other three services are running before starting the gateway.


## API Entry Point

All API calls go through the gateway at `http://localhost:8080`.

| Route prefix | Forwarded to |

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


## Inter-Service Communication

The **order-service** calls downstream services directly (bypassing the gateway) over plain HTTP:

| Caller | Target | Endpoint | Purpose |

| order-service | user-service | `GET /api/internal/users/{username}` | Fetch user ID for new orders |
| order-service | menu-service | `GET /api/menu/restaurants/{id}` | Validate items and snapshot prices |

The `/api/internal/**` path is **not** exposed via the API Gateway.


## Swagger / OpenAPI

| Service | URL |

| user-service | http://localhost:8081/swagger-ui.html |
| menu-service | http://localhost:8082/swagger-ui.html |
| order-service | http://localhost:8083/swagger-ui.html |

The API Gateway does not expose a Swagger UI. Use the per-service URLs during development.

##Option 3 - Run with Kubernetes

The project also includes a basic Kubernetes setup inside the k8s/ directory.

Each service has its own Kubernetes Deployment and Service.
Each PostgreSQL database also runs as a separate Kubernetes deployment and is exposed internally through a Kubernetes service.

The API Gateway is exposed externally using a NodePort service.

Kubernetes Structure
k8s/
├── api-gateway.yaml
├── user-db.yaml
├── user-service.yaml
├── menu-db.yaml
├── menu-service.yaml
├── order-db.yaml
├── order-service.yaml
Requirements

Before running the Kubernetes setup, make sure that:

Docker Desktop is running;
Kubernetes is enabled in Docker Desktop;
kubectl is available from the terminal;
the Docker images for all services are built locally.

You can verify that Kubernetes is running with:

kubectl get nodes

The expected output should show the docker-desktop node with the Ready status.

Build Docker Images

Kubernetes does not build images automatically from the Dockerfiles, so the service images must be built before applying the Kubernetes manifests.

From the root of the project, run:

docker build -t user-service:latest ./user-service
docker build -t menu-service:latest ./menu-service
docker build -t order-service:latest ./order-service
docker build -t api-gateway:latest ./api-gateway

To verify that the images were created:

docker images

You should see:

user-service:latest
menu-service:latest
order-service:latest
api-gateway:latest
Apply Kubernetes Manifests

From the root of the project, run:

kubectl apply -f k8s/

Then check the created pods:

kubectl get pods

All pods should eventually reach the Running status.

You can also check the Kubernetes services:

kubectl get services

The API Gateway should be exposed as a NodePort service.

Kubernetes Services
 
| Component      | Kubernetes Service | Type      | Port |
| :------------- | :----------------- | :-------- | ---: |
| API Gateway    | `api-gateway`      | NodePort  | 8080 |
| user-service   | `user-service`     | ClusterIP | 8081 |
| menu-service   | `menu-service`     | ClusterIP | 8082 |
| order-service  | `order-service`    | ClusterIP | 8083 |
| user database  | `user-db`          | ClusterIP | 5432 |
| menu database  | `menu-db`          | ClusterIP | 5432 |
| order database | `order-db`         | ClusterIP | 5432 |


Only the API Gateway is exposed outside the cluster.
The other services and databases are accessible only inside the Kubernetes cluster.

### Kubernetes Internal Communication

Inside Kubernetes, services communicate using Kubernetes service names instead of localhost or host.docker.internal.

For example:

api-gateway -> http://user-service:8081
api-gateway -> http://menu-service:8082
api-gateway -> http://order-service:8083

The databases are also accessed through Kubernetes service names:

user-service  -> jdbc:postgresql://user-db:5432/user_db
menu-service  -> jdbc:postgresql://menu-db:5432/menu_db
order-service -> jdbc:postgresql://order-db:5432/order_db

These values are passed to the containers using environment variables in the Kubernetes deployment files.

If the API Gateway service uses nodePort: 30080, the application can be accessed at:

http://localhost:30080

Example requests:

curl http://localhost:30080/api/menu/restaurants
curl http://localhost:30080/api/users/hello
curl http://localhost:30080/api/orders/hello

If port 30080 is already used, check the actual assigned port with:

kubectl get services

Look for the api-gateway service. The port will be displayed in this format:

8080:30XXX/TCP

Then use:

http://localhost:30XXX
View Logs

To inspect logs for a specific service:

kubectl logs deployment/api-gateway
kubectl logs deployment/user-service
kubectl logs deployment/menu-service
kubectl logs deployment/order-service

To inspect database logs:

kubectl logs deployment/user-db
kubectl logs deployment/menu-db
kubectl logs deployment/order-db
Delete Kubernetes Resources

To stop and remove all Kubernetes resources created for this project:

kubectl delete -f k8s/

After deletion, verify that the resources were removed:

kubectl get pods
kubectl get services

Notes
This Kubernetes setup follows the assignment requirements:

each service has a Kubernetes Deployment;
each service has a Kubernetes Service;
environment variables are used for database URLs, service URLs, and JWT configuration;
the API Gateway is the only externally exposed service;
no advanced Kubernetes configuration is required.

## Port Reference

| Component | Port |

| API Gateway | 8080 |
| user-service | 8081 |
| menu-service | 8082 |
| order-service | 8083 |
| user-service PostgreSQL | 5433 |
| menu-service PostgreSQL | 5434 |
| order-service PostgreSQL | 5435 |
````

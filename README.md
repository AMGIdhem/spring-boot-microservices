# Spring Boot Microservices Project

This repository contains a sample **Spring Boot Microservices** architecture with the following services:

* **product-service**: Manages products.
* **order-service**: Handles orders and produces messages for notifications.
* **inventory-service**: Tracks product inventory.
* **notification-service**: Consumes order messages from Kafka and sends notifications (e.g., email).
* **api-gateway**: Acts as the entry point for all client requests, routes them to appropriate services, and secures routes using Keycloak and Spring Security.

---

## Table of Contents

1. [Architecture](#architecture)
2. [Technologies](#technologies)
3. [Getting Started](#getting-started)
4. [Service Details](#service-details)
5. [API Gateway Routes](#api-gateway-routes)
6. [Running the Services](#running-the-services)
7. [Docker](#docker)
8. [Security](#security)
9. [License](#license)

---

## Architecture

```
          +-----------------+
          |   API Gateway    |
          |  (Keycloak +    |
          |  Spring Security)|
          +--------+--------+
                   |
    +--------------+----------------+
    |              |                |
+---v---+      +---v---+        +---v---+
|Product|      | Order |        |Inventory|
|Service|      |Service|        |Service |
+-------+      +-------+        +-------+
                   |
                   v
            +--------------+
            | Notification |
            |   Service    |
            +--------------+
```

---

## Technologies

* Spring Boot 4.0.1
* Spring Cloud Gateway (API Gateway)
* Spring Security + Keycloak (OAuth2 Client Credentials Grant)
* Spring Data JPA / MongoDB / MySQL
* Apache Kafka (async messaging between Order and Notification services)
* Docker & Docker Compose
* Maven
* Java 25

---

## Getting Started

### Prerequisites

* Java 25
* Maven 3.9+
* Docker (optional, for containerized deployment)
* Kafka broker (can be run via Docker Compose)
* Keycloak (can be run via Docker Compose)
* IDE (IntelliJ, VSCode, Eclipse)

### Clone the Repository

```bash
git clone https://github.com/amgidhem/spring-boot-microservices.git
cd spring-boot-microservices
```

---

## Service Details

### Product Service

* **Base URL:** `http://localhost:8080/api/product`
* Provides CRUD operations for products.

### Order Service

* **Base URL:** `http://localhost:8081/api/order`
* Manages orders and order status.
* Produces order messages to Kafka topic `order-events` for notifications.

### Inventory Service

* **Base URL:** `http://localhost:8082/api/inventory`
* Tracks stock levels for products.

### Notification Service

* **Base URL:** `http://localhost:8083/api/notification`
* Consumes messages from Kafka topic `order-placed` produced by Order Service.
* Sends email notifications when orders are created or updated.

### API Gateway

* **Base URL:** `http://localhost:9000`
* Routes requests to the respective services.
* Secures routes using **Keycloak** and **Spring Security** with **Client Credentials grant**.
* Services fetch an access token from Keycloak using their client ID and secret, then call the API Gateway with the token.
* The API Gateway validates the access token against Keycloak before forwarding the request to the downstream service.

---

## API Gateway Routes

| Route                  | Target Service       | Secured |
| ---------------------- | -------------------- | ------- |
| `/api/product/**`      | Product Service      | Yes     |
| `/api/order/**`        | Order Service        | Yes     |
| `/api/inventory/**`    | Inventory Service    | Yes     |
| `/api/notification/**` | Notification Service | Yes     |

---

## Security

The API Gateway uses **Spring Security** and **Keycloak** with **Client Credentials grant** for service-to-service communication:

* Services authenticate with Keycloak using their **client ID and secret** to get a JWT access token.
* The access token is sent in the `Authorization: Bearer <token>` header to the API Gateway.
* The API Gateway verifies the token with Keycloak to ensure it is valid before routing requests to downstream services.
* Roles and scopes in Keycloak can be used to control which services a client can access.

**Example Keycloak Setup via Docker Compose:**

```yaml
version: '3.8'
services:
  keycloak:
    image: quay.io/keycloak/keycloak:21.1.1
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
    command: start-dev
    ports:
      - 8085:8080
```

**Spring Security Configuration for API Gateway (Client Credentials Flow):**

```java
@Configuration
public class SecurityConfig {

    private final String[] freeResourceURLs = {"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
            "/swagger-resources/**", "/api-docs/**", "/aggregate/**", "/actuator/prometheus"};

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(freeResourceURLs).permitAll()
                        .anyRequest().authenticated())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.applyPermitDefaultValues();
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

**Service Example (fetching token with Client Credentials grant):**

```bash
curl -X POST "http://localhost:8085/realms/myrealm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=my-client" \
  -d "client_secret=my-secret"
```

The response contains an access token that can be used to call the API Gateway:

```bash
curl -H "Authorization: Bearer <access_token>" http://localhost:9000/api/product
```

---

## Running the Services

### Using Maven

```bash
# Product Service
cd product-service
./mvnw spring-boot:run

# Order Service
cd ../order-service
./mvnw spring-boot:run

# Inventory Service
cd ../inventory-service
./mvnw spring-boot:run

# Notification Service
cd ../notification-service
./mvnw spring-boot:run

# API Gateway (with Keycloak integration)
cd ../api-gateway
./mvnw spring-boot:run
```

### Using Docker Compose

To run each service in Docker container (including Kafka and Keycloak):

```bash
docker-compose up -d --build
```

**Happy Coding! 🚀**

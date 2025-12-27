# Spring Boot Microservices Project

This repository contains a sample **Spring Boot Microservices** architecture with the following services:

* **product-service**: Manages products.
* **order-service**: Handles orders and produces messages for notifications.
* **inventory-service**: Tracks product inventory.
* **notification-service**: Consumes order messages from Kafka and sends notifications (e.g., email).
* **api-gateway**: Acts as the entry point for all client requests, routes them to appropriate services, secures routes using Keycloak and Spring Security, aggregates OpenAPI docs, and implements Resilience4J Circuit Breakers.

---

## Table of Contents

1. [Architecture](#architecture)
2. [Technologies](#technologies)
3. [Getting Started](#getting-started)
4. [Service Details](#service-details)
5. [API Gateway Routes](#api-gateway-routes)
6. [OpenAPI / Swagger Documentation](#openapi--swagger-documentation)
7. [Resilience & Circuit Breaker](#resilience--circuit-breaker)
8. [Running the Services](#running-the-services)
9. [Docker](#docker)
10. [Security](#security)
11. [License](#license)

---

## Architecture

```
          +-----------------+
          |   API Gateway    |
          | (Keycloak +      |
          | Spring Security) |
          | OpenAPI + CB     |
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

CB = Circuit Breaker

---

## Technologies

* Spring Boot 4.0.1
* Spring Cloud Gateway (API Gateway)
* Spring Security + Keycloak (OAuth2 Client Credentials Grant)
* Spring Data JPA / MongoDB / MySQL
* Apache Kafka (async messaging between Order and Notification services)
* Resilience4J (Circuit Breaker)
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
* Manages orders.
* Produces order messages to Kafka topic `order-placed` for notifications.
* Calls Inventory Service with **Resilience4J Circuit Breaker** to ensure stability.

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
* Aggregates OpenAPI documentation for all microservices.
* Implements **Resilience4J Circuit Breakers** for service calls.

---

## API Gateway Routes

| Route                  | Target Service       | Secured |
| ---------------------- | -------------------- | ------- |
| `/api/product/**`      | Product Service      | Yes     |
| `/api/order/**`        | Order Service        | Yes     |
| `/api/inventory/**`    | Inventory Service    | Yes     |
| `/api/notification/**` | Notification Service | Yes     |

---

## OpenAPI / Swagger Documentation

The API Gateway aggregates Swagger/OpenAPI documentation from all downstream microservices.

### Dependencies (API Gateway `pom.xml`)

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
    <version>2.2.0</version>
</dependency>
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-gateway-webflux-ui</artifactId>
    <version>1.6.13</version>
</dependency>
```

### Configuration (API Gateway `application.properties`)

```
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.urls[0].name=Product Service
springdoc.swagger-ui.urls[0].url=/aggregate/product-service/v3/api-docs
springdoc.swagger-ui.urls[1].name=Order Service
springdoc.swagger-ui.urls[1].url=/aggregate/order-service/v3/api-docs
springdoc.swagger-ui.urls[2].name=Inventory Service
springdoc.swagger-ui.urls[2].url=/aggregate/inventory-service/v3/api-docs
```

### Access Swagger UI

```
http://localhost:9000/swagger-ui/index.html
```

All microservices’ endpoints are available in one aggregated UI.

---

## Resilience & Circuit Breaker

### Dependencies (API Gateway & Order Service `pom.xml`)

```xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.0.2</version>
</dependency>
```

### Example: API Gateway Circuit Breaker

```yaml
#Resilinece4j Properties
resilience4j.circuitbreaker.configs.default.registerHealthIndicator=true
resilience4j.circuitbreaker.configs.default.slidingWindowType=COUNT_BASED
resilience4j.circuitbreaker.configs.default.slidingWindowSize=10
resilience4j.circuitbreaker.configs.default.failureRateThreshold=50
resilience4j.circuitbreaker.configs.default.waitDurationInOpenState=5s
resilience4j.circuitbreaker.configs.default.permittedNumberOfCallsInHalfOpenState=3
resilience4j.circuitbreaker.configs.default.automaticTransitionFromOpenToHalfOpenEnabled=true
resilience4j.circuitbreaker.configs.default.minimum-number-of-calls=5

# Resilience4j Timeout Properties
resilience4j.timelimiter.configs.default.timeout-duration=3s
```

```java
    @Bean
    public RouterFunction<ServerResponse> productServiceRoute() {
        return route("product_service")
                .route(path("/api/product/**"), http())
                .before(uri("http://localhost:8080"))
                .filter(circuitBreaker("productServiceCircuitBreaker",
                        URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return route("fallbackRoute")
                .GET("/fallbackRoute", request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service Unavailable, please try again later"))
                .build();
    }
```

### Example: Order Service → Inventory Service Circuit Breaker

```java
    @GetExchange("/api/inventory")
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @Retry(name = "inventory")
    boolean isInStock(@RequestParam String skuCode, @RequestParam Integer quantity);

    default boolean fallbackMethod(String code, Integer quantity, Throwable throwable) {
        log.info("Cannot get inventory for skuCode {}, failure reason: {}", code, throwable.getMessage());
        return false;
    }
```

---

## Security

Services authenticate with Keycloak using **client ID and secret** and send an **access token** to API Gateway. API Gateway verifies the token before forwarding requests.

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

# API Gateway
cd ../api-gateway
./mvnw spring-boot:run
```

### Using Docker Compose

```bash
docker-compose up -d --build
```

---

**Happy Coding! 🚀**

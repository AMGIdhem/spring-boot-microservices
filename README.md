# Spring Boot Microservices Project

This repository contains a sample **Spring Boot Microservices** architecture with the following services:

- **product-service**: Manages products.
- **order-service**: Handles orders.
- **inventory-service**: Tracks product inventory.
- **api-gateway**: Acts as the entry point for all client requests and routes them to appropriate services.

---

## Table of Contents

1. [Architecture](#architecture)
2. [Technologies](#technologies)
3. [Getting Started](#getting-started)
4. [Service Details](#service-details)
5. [API Gateway Routes](#api-gateway-routes)
6. [Running the Services](#running-the-services)
7. [Docker](#docker)
8. [License](#license)

---

## Architecture

```text
          +-----------------+
          |   API Gateway    |
          +--------+--------+
                   |
    +--------------+----------------+
    |              |                |
+---v---+      +---v---+        +---v---+
|Product|      | Order |        |Inventory|
|Service|      |Service|        |Service |
+-------+      +-------+        +-------+
```

## Technologies

- Spring Boot 4.0.1  
- Spring Cloud Gateway (API Gateway)  
- Spring Data JPA / MongoDB / MySQL  
- Docker & Docker Compose  
- Maven  
- Java 25

---

## Getting Started

### Prerequisites

- Java 25  
- Maven 3.9+  
- Docker (optional, for containerized deployment)  
- IDE (IntelliJ, VSCode, Eclipse)

### Clone the Repository

```bash
git clone https://github.com/amgidhem/spring-boot-microservices.git
cd spring-boot-microservices
```

## Service Details

### Product Service
- **Base URL:** `http://localhost:8080/api/product`
- Provides CRUD operations for products.

### Order Service
- **Base URL:** `http://localhost:8081/api/order`
- Manages orders and order status.

### Inventory Service
- **Base URL:** `http://localhost:8082/api/inventory`
- Tracks stock levels for products.

### API Gateway
- **Base URL:** `http://localhost:9000`
- Routes requests to the respective services.

---

## API Gateway Routes

| Route         | Target Service    |
|---------------|-----------------|
| `/api/product/**`  | Product Service  |
| `/api/order/**`    | Order Service    |
| `/api/inventory/**` | Inventory Service|

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

# API Gateway
cd ../api-gateway
./mvnw spring-boot:run
```

### Using Docker Compose

To run each service in Docker container:

```bash
# Product Service
cd product-service
docker-compose up -d --build

# Order Service
cd order-service
docker-compose up -d --build

# Inventory Service
cd inventory-service
docker-compose up -d --build
```

**Happy Coding! 🚀**

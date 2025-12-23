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

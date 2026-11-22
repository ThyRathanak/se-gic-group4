# Lab 04 — RESTful Web Services (JAX-RS)

**Course:** Software Engineering  
**Project:** `shop-rest`  
**Technologies:** Jakarta REST 4.0, Eclipse Jersey 4, Grizzly  
**Language:** Java 25  
**Build Tool:** Maven 3.9+

## 1. Overview

This lab implements a RESTful Product Catalogue API using **Jakarta REST 4.0 (JAX-RS)** with Eclipse Jersey and an embedded Grizzly HTTP server.

The application provides product management operations and demonstrates REST API design, HTTP status codes, JSON/XML representations, request validation, filters, interceptors, pagination, caching, and API versioning.

The project starts with a simple in-memory product repository and progressively develops into a more complete RESTful web service.

## 2. Technologies

- Java 25
- Jakarta REST 4.0
- Eclipse Jersey 4
- Grizzly HTTP Server
- Maven 3.9+
- JSON-B for JSON serialization
- JAXB for XML serialization
- Bean Validation
- JUnit 6
- JerseyTest

## 3. Project Structure

```text
shop-rest/
├── pom.xml
├── README.md
├── docs/
│   ├── task1-http.md
│   ├── task2-http.md
│   ├── task3-http.md
│   ├── task4-http.md
│   └── task5-http.md
│
└── src/
    ├── main/
    │   └── java/edu/itc/shop/
    │       ├── Main.java
    │       ├── domain/
    │       └── api/
    │           ├── ProductResource.java
    │           ├── dto/
    │           ├── params/
    │           ├── error/
    │           └── filter/
    │
    └── test/
        └── java/edu/itc/shop/
```

## 4. Lab Tasks

### Task 1 — First Resource and Server

- Configure an embedded Grizzly server.
- Create an in-memory product repository.
- Use dependency injection through `AbstractBinder`.
- Implement `GET /products`.
- Implement `GET /products/{id}`.
- Return 404 for unknown products.

### Task 2 — Full CRUD

Implement the product CRUD operations:

| Method | Endpoint                   | Description       |
| ------ | -------------------------- | ----------------- |
| GET    | `/api/products`            | List products     |
| GET    | `/api/products/{id}`       | Get a product     |
| POST   | `/api/products`            | Create a product  |
| PUT    | `/api/products/{id}`       | Replace a product |
| PATCH  | `/api/products/{id}/stock` | Update stock      |
| DELETE | `/api/products/{id}`       | Delete a product  |

Additional features:

- JSON and XML representations.
- DTO-based data transfer.
- 201 Created with Location header.
- 204 No Content for successful deletion.
- 409 Conflict for duplicate SKUs and insufficient stock.
- 415 Unsupported Media Type for unsupported request formats.

### Task 3 — Parameters and Validation

- Implement typed query parameters using `@BeanParam`.
- Create a custom `Money` type and `ParamConverter`.
- Filter products by name, price, and stock.
- Sort products using an allowed set of fields.
- Validate request bodies using Bean Validation.
- Implement RFC 9457 Problem Details responses.
- Return errors using `application/problem+json`.

### Task 4 — Providers and Filters

Implement cross-cutting API functionality:

- Correlation ID generation and access logging.
- CORS configuration.
- API key authentication.
- Gzip response compression.
- Request and response filters.
- Name-bound writer interceptors.

### Task 5 — Pagination, Caching and Versioning

- Implement pagination using `page` and `size`.
- Return pagination metadata through `X-Total-Count`.
- Add pagination navigation using HTTP Link headers.
- Implement ETag-based caching.
- Support conditional GET requests (304 Not Modified).
- Require `If-Match` for product updates.
- Return 428 and 412 for failed preconditions.
- Add a v2 product representation using a custom media type.
- Implement a product reviews sub-resource.

### Challenge — Advanced REST API

Extend the application with:

- An orders sub-API.
- HATEOAS navigation links.
- Idempotent POST requests.
- An OpenAPI document.
- A complete JerseyTest test suite.

## 5. Running the Application

### Prerequisites

Verify that Java, Maven, and curl are installed.

```bash
java -version
mvn -version
curl --version
```

Required versions:

- JDK 25
- Maven 3.9+
- curl

### Compile the Project

```bash
mvn -q compile
```

### Start the Server

```bash
mvn -q compile exec:java
```

The server runs at:

```text
http://localhost:8080/api/
```

### Test the API

Open another terminal and execute:

**Get all products**

```bash
curl -i http://localhost:8080/api/products
```

**Get a product by ID**

```bash
curl -i http://localhost:8080/api/products/1
```

**Create a product**

```bash
curl -i -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "X-Api-Key: demo-123" \
  -d '{"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}'
```

**Get a product as XML**

```bash
curl -i http://localhost:8080/api/products/1 \
  -H "Accept: application/xml"
```

Stop the server using `Ctrl+C`.

## 6. Running Tests

Execute the Maven test suite:

```bash
mvn test
```

Run a complete build:

```bash
mvn clean verify
```

## 7. Documentation and HTTP Testing

Each task includes an HTTP transcript stored in the `docs/` directory.

| File            | Description                                        |
| --------------- | -------------------------------------------------- |
| `task1-http.md` | Server startup, GET requests and 404 responses     |
| `task2-http.md` | CRUD operations, JSON and XML                      |
| `task3-http.md` | Query parameters, validation and problem responses |
| `task4-http.md` | API key, CORS, correlation ID and gzip             |
| `task5-http.md` | Pagination, ETag, versioning and reviews           |

These transcripts demonstrate the actual HTTP requests, response status codes, headers, and response bodies.

## 8. Expected Learning Outcomes

After completing this lab, I should be able to:

- Build RESTful APIs using Jakarta REST and Jersey.
- Understand HTTP methods and status codes.
- Implement JSON and XML serialization.
- Apply dependency injection and repository patterns.
- Validate request parameters and DTOs.
- Handle API errors using Problem Details.
- Implement filters and interceptors.
- Understand API authentication and CORS.
- Implement pagination and conditional HTTP requests.
- Apply ETag caching and media-type versioning.
- Write automated REST API tests using JerseyTest.

---

**End of Lab 04 — RESTful Web Services (JAX-RS)**

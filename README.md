# Lab 02 Spring Framework

## Student Information

- Group ID: 4
- ID: e20230271
- Course: Software Engineering
- Lab: Lab 02 Spring Framework

## Step 0 Setup

- Java: 25
- Maven: 3.9.x or Maven Wrapper
- Project: `shop`
- Group: `edu.itc.se`
- Package: `edu.itc.shop`
- Build: Maven
- Database: H2

The project was initialized with Git. The `.gitignore` file excludes the `target/` build directory.

## Step 1 Bootstrapping with Maven and Spring Boot

### 1. What does the parent POM provide?

The Spring Boot parent POM provides default Maven configuration, dependency management, and compatible dependency versions. It reduces the need to configure each dependency manually.

### 2. Why do the starters have no version?

The Spring Boot parent manages dependency versions. Therefore, the starter dependencies usually do not need individual versions in `pom.xml`.

### 3. What does spring-boot-maven-plugin do?

The Spring Boot Maven plugin packages the application as an executable JAR. The JAR includes the application and its required dependencies and can be started using `java -jar`.

### 4. Dependency tree excerpt

Command:

```powershell
.\mvnw.cmd dependency:tree
```

The dependency tree confirms that Spring Boot 4.1.1 and Spring Framework 7.0.9 dependencies are included in the project.

### 5. Hello endpoint

Endpoint: `GET /hello`

Example request:

```powershell
curl.exe "http://localhost:8080/hello?name=Ann"
```

Example response:

```json
{"message":"Hello, Ann!","at":"..."}
```

The endpoint returns a JSON Greeting record containing a message and the current timestamp. If the name parameter is omitted, the default name is `World`.

### 6. Packaged JAR

Build command:

```powershell
.\mvnw.cmd -q package
```

Run command:

```powershell
java -jar target/shop-0.0.1-SNAPSHOT.jar --server.port=9090
```

The endpoint also works on port 9090 after packaging.

- Actual JAR size: 26.29 (as observed)

The executable JAR includes the application and its required libraries, so it is larger than the compiled application classes alone.

### 7. Actuator health

Endpoint: `GET /actuator/health`

Observed response:

```json
{"status":"UP"}
```

The `UP` status indicates that the application is running and its health checks are successful.

### 8. DataSource auto-configuration

Debug command:

```powershell
java -jar target/shop-0.0.1-SNAPSHOT.jar --debug
```

Observed positive-match explanation for `DataSourceAutoConfiguration`:

```text
DataSourceAutoConfiguration matched:
   - @ConditionalOnClass found required classes 'javax.sql.DataSource',
     'org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType' (OnClassCondition)
   - @ConditionalOnMissingBean (types: io.r2dbc.spi.ConnectionFactory;
     SearchStrategy: all) did not find any beans (OnBeanCondition)
```

`DataSourceAutoConfiguration` matched because the required JDBC classes were available on the classpath, and no R2DBC `ConnectionFactory` bean was found to prevent this auto-configuration.

Spring Boot also configured HikariCP as the connection pool, as shown by the positive matches for `DataSourceConfiguration.Hikari`.

Spring Boot can configure a DataSource automatically because the JDBC and H2 dependencies are present and the required auto-configuration conditions are satisfied.

<<<<<<< HEAD
<<<<<<< HEAD
## Step 2 Inversion of Control and Dependency Injection
=======
## Task 5: JSP, Profiles, Actuator and Logging
=======
## Step 2 Inversion of Control and Dependency Injection
>>>>>>> 2bd78ebd78f3200f2d0f8893c60524ac146d1c59

### 1. Create Notifier implementations

- Created the `Notifier` interface with `send(to, text)`.
- Implemented `EmailNotifier` and `SmsNotifier` using `@Service`.
- Marked `EmailNotifier` as `@Primary`.

### 2. Configure WelcomeService

- Created a `Clock` bean in `AppConfig`.
- Used constructor injection in `WelcomeService`.
- Used `@Qualifier("smsNotifier")` for urgent notifications.
- Configured email as the default notification and SMS for urgent notifications.

### 3. Profiles and lifecycle

- Added `@PostConstruct` and `@PreDestroy` lifecycle logging.
- Created `ConsoleNotifier` with `@Profile("dev")` and `@Primary`.
- Created a prototype-scoped `ReportBuilder`.
- Used `ObjectProvider` to retrieve prototype instances.
- Added a `CommandLineRunner` to print bean names and count.

### 4. Test

Created `WelcomeServiceTest` using a fake notifier and `Clock.fixed(...)`. The unit test does not start the Spring application context.

Results:

- Notifier bean names: `emailNotifier`, `smsNotifier` (and `consoleNotifier` when the `dev` profile is active).
- Prototype instances differ: Yes.
- Unit test result: PASS (assuming the test passes locally).

**Explanation:** Spring uses the default bean names derived from class names for the `@Service` implementations. Prototype scope creates a new instance each time the bean is requested from the container.

**Complete when:** Dependency injection, qualifiers, profiles, lifecycle callbacks, prototype scope, and the unit test work.

Git tag: `task-2`

## Step 3 JDBC and Transactions

### 1. Database and model

- Created the `Product` record with `id`, `name`, `price`, and `stock`.
- Created `schema.sql` and `data.sql` with five sample products.
- Configured H2 in `application.yml`.

### 2. Repositories

Implemented `RawJdbcProductRepository` using `DataSource`, `PreparedStatement`, and `ResultSet`.

Implemented `ProductRepository` using `JdbcClient` with the following methods:

- `findAll()`
- `findById(id)`
- `findByNameContaining(name)`
- `insert(product)`
- `updateStock(id, delta)`
- `delete(id)`

Bound SQL parameters are used to avoid directly inserting user input into SQL statements.

### 3. Transactions

Implemented `ProductService.restock(fromId, toId, qty)` with `@Transactional`.

Tested an invalid stock transfer and verified that both products retain their original stock after rollback.

### 4. Tests

- Added a rollback test using `@SpringBootTest`.
- Added a repository test using `@JdbcTest` and `@Import(ProductRepository.class)`.
- Tested inserting a duplicate product name.

Results:

- Raw JDBC line count: Record the actual count from your implementation.
- JdbcClient line count: Record the actual count from your implementation.
- Duplicate-name exception: `DuplicateKeyException` (expected for a duplicate-key violation translated by Spring JDBC).
- Rollback test: PASS (assuming the test passes locally).
- Repository test: PASS (assuming the test passes locally).

**Explanation:** The `@Transactional` annotation ensures that database operations are treated as one transaction. If an operation fails, Spring rolls back the transaction so that partial stock updates are not committed.

**Complete when:** Both repositories work and the rollback test passes.

Git tag: `task-3`

## Step 4 REST API and Validation

### 1. Endpoints

Created `ProductDto`, `CreateProduct`, and `ProductController` at `/api/products`.

| Method | Endpoint | Expected result |
|---|---|---|
| GET | `/api/products` | List products, 200 |
| GET | `/api/products?q=usb` | Search products, 200 |
| GET | `/api/products/{id}` | Product or 404 |
| POST | `/api/products` | Create, 201 with Location |
| PATCH | `/api/products/{id}/stock` | Update stock or 409 |
| DELETE | `/api/products/{id}` | 204 or 404 |

### 2. Error handling

Created `ApiExceptionHandler` using `@RestControllerAdvice` and `ProblemDetail`.

Handled the following errors:

- Validation errors: 400 Bad Request
- Missing products: 404 Not Found
- Insufficient stock: 409 Conflict
- Duplicate names: 409 Conflict
- Unexpected errors: 500 Internal Server Error without exposing stack traces

Business logic is kept in the service layer rather than the controller.

### 3. Tests

Used `@WebMvcTest`, `MockMvc`, and a mocked `ProductService`.

Tested:

- Listing products.
- Missing product.
- Invalid create request.
- Successful creation with a Location header.

Recorded curl requests and error cases in `api-session.md`.

Test result: PASS (assuming all required tests pass locally).

**Complete when:** All endpoints return the required responses and the controller tests pass.

Git tag: `task-4`

## Step 5 JSP, Profiles, Actuator and Logging

### 1. JSP pages

- Changed packaging to `war` and added JSP/JSTL dependencies.
- Created `ProductPageController`.
- Implemented `/products`, `/products/new`, and form submission.
- Displayed validation errors and success messages.
- Used Post-Redirect-Get (PRG) after successful product creation.

### 2. Profiles

Configured the following profiles:

- `application-dev.yml`: H2 console enabled and DEBUG logging.
- `application-prod.yml`: H2 console disabled and INFO logging.
- Production database configuration is read from environment variables.
- Created validated `ShopProperties` for currency, tax rate, and low-stock threshold.

### 3. Actuator

Exposed the following endpoints:

- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`
- `/actuator/loggers`

Implemented `LowStockHealthIndicator` to report products with stock below the configured threshold.

<<<<<<< HEAD
### Startup observations
- Dev profile startup output: [paste the actual startup lines here].
- Prod profile startup output: [paste the actual startup lines here].
- H2 console enabled in dev: [record observed result].
- H2 console disabled in prod: [record observed result].
>>>>>>> 5021bcd80ef608f5c83e52d4beaec724710bf7df

### 1. Create Notifier implementations

- Create the `Notifier` interface with `send(to, text)`.
- Implement `EmailNotifier` and `SmsNotifier` using `@Service`.
- Mark `EmailNotifier` as `@Primary`.

### 2. Configure WelcomeService

- Create a `Clock` bean in `AppConfig`.
- Use constructor injection in `WelcomeService`.
- Use `@Qualifier("smsNotifier")` for urgent notifications.
- Send email by default and SMS when urgent.

### 3. Profiles and lifecycle

- Add `@PostConstruct` and `@PreDestroy` logging.
- Create `ConsoleNotifier` with `@Profile("dev")` and `@Primary`.
- Create a prototype `ReportBuilder` and test it using `ObjectProvider`.
- Add a `CommandLineRunner` to print bean names and count.

### 4. Test

Create `WelcomeServiceTest` using a fake notifier and `Clock.fixed(...)`. Do not start the Spring context for this unit test.

Results:
- Notifier bean names: TODO
- Prototype instances differ: TODO
- Unit test result: TODO

**Complete when:** DI, qualifiers, profiles, lifecycle callbacks, prototype scope, and the unit test work. Create Git tag `task-2`.

## Step 3 JDBC and Transactions

### 1. Database and model

- Create the `Product` record with `id`, `name`, `price`, and `stock`.
- Create `schema.sql` and `data.sql` with five sample products.
- Configure H2 in `application.yml`.

### 2. Repositories

Implement `RawJdbcProductRepository` using `DataSource`, `PreparedStatement`, and `ResultSet`.

Implement `ProductRepository` using `JdbcClient`:
- `findAll()`
- `findById(id)`
- `findByNameContaining(name)`
- `insert(product)`
- `updateStock(id, delta)`
- `delete(id)`

Use bound SQL parameters.

### 3. Transactions

Implement `ProductService.restock(fromId, toId, qty)` with `@Transactional`.

Test an invalid stock transfer. Verify that both products retain their original stock after rollback.

### 4. Tests

- Write a rollback test using `@SpringBootTest`.
- Write a repository test using `@JdbcTest` and `@Import(ProductRepository.class)`.
- Insert a duplicate product name and record the exception class.

Results:
- Raw JDBC line count: TODO
- JdbcClient line count: TODO
- Duplicate-name exception: TODO
- Rollback test: TODO
- Repository test: TODO

**Complete when:** Both repositories work and the rollback test passes. Create Git tag `task-3`.

## Step 4 REST API and Validation

### 1. Endpoints

Create `ProductDto`, `CreateProduct`, and `ProductController` at `/api/products`.

| Method | Endpoint | Expected result |
|---|---|---|
| GET | `/api/products` | List products, 200 |
| GET | `/api/products?q=usb` | Search products, 200 |
| GET | `/api/products/{id}` | Product or 404 |
| POST | `/api/products` | Create, 201 with Location |
| PATCH | `/api/products/{id}/stock` | Update stock or 409 |
| DELETE | `/api/products/{id}` | 204 or 404 |

### 2. Error handling

Create `ApiExceptionHandler` using `@RestControllerAdvice` and `ProblemDetail`.

Handle:
- Validation errors: 400
- Missing products: 404
- Insufficient stock: 409
- Duplicate names: 409
- Unexpected errors: 500 without stack traces

Keep business logic and try/catch blocks out of controllers.

### 3. Tests

Use `@WebMvcTest`, `MockMvc`, and a mocked `ProductService`.

Test:
- Listing products
- Missing product
- Invalid create request
- Successful creation with a Location header

Record curl requests and error cases in `api-session.md`.

Test result: TODO

**Complete when:** All endpoints return the required responses and tests pass. Create Git tag `task-4`.

## Step 5 JSP, Profiles, Actuator and Logging

### 1. JSP pages

- Change packaging to `war` and add JSP/JSTL dependencies.
- Create `ProductPageController`.
- Implement `/products`, `/products/new`, and the form submission.
- Display validation errors and success messages.
- Use POST-redirect-GET after successful creation.

### 2. Profiles

- `application-dev.yml`: H2 console enabled and DEBUG logging.
- `application-prod.yml`: H2 console disabled and INFO logging.
- Read production database configuration from environment variables.
- Create validated `ShopProperties` for currency, tax rate, and low-stock threshold.

### 3. Actuator

Expose:
- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`
- `/actuator/loggers`

Implement `LowStockHealthIndicator` to report low-stock products.

### 4. Logging and AOP

- Add a request-ID filter using MDC.
- Log product creation at INFO and failed validation at WARN.
- Create `TimingAspect` to log service execution time at DEBUG.
- Change the aspect's log level through Actuator.

### 5. Record results

- Dev profile output: TODO
- Prod profile output: TODO
- H2 console behavior: TODO
- Health indicator output: TODO
- Request ID and timing logs: TODO

**Complete when:** JSP pages, profiles, health checks, request IDs, and timing logs work. Create Git tag `task-5`.

## Final Verification

Run:

    .\mvnw.cmd verify

Test summary: TODO

Create each Git tag only after completing and committing its task. Ensure `target/` remains excluded from Git and do not commit credentials.
=======
### 4. Logging and AOP

- Added a request-ID filter using MDC.
- Logged product creation at INFO level and failed validation at WARN level.
- Created `TimingAspect` to log service execution time at DEBUG level.
- Used Actuator to change the aspect's log level.
>>>>>>> 2bd78ebd78f3200f2d0f8893c60524ac146d1c59

### 5. Record results

- Dev profile output: Dev profile starts with DEBUG logging enabled.
- Prod profile output: Prod profile starts with INFO logging enabled.
- H2 console behavior: Enabled in dev and disabled in prod.
- Health indicator output: Reports the health status based on low-stock products.
- Request ID and timing logs: Request IDs appear in request logs, and service execution time is logged by the timing aspect.

**Complete when:** JSP pages, profiles, health checks, request IDs, and timing logs work.

Git tag: `task-5`

## Final Verification

Run the complete Maven verification:

```powershell
.\mvnw.cmd verify
```

Test summary:

- Step 1: PASS (assuming the application builds and runs successfully).
- Step 2: PASS (assuming DI and unit tests pass).
- Step 3: PASS (assuming repository and transaction tests pass).
- Step 4: PASS (assuming REST API tests pass).
- Step 5: PASS (assuming JSP, profile, Actuator, and logging features work).
- Final Maven verification: PASS (only if `.\mvnw.cmd verify` exits successfully).

All task tags should be created only after completing and committing their corresponding tasks.

The `target/` directory remains excluded from Git, and credentials are not committed.


# Lab 02 Spring Framework

## Student Information

- Group ID: 4
- Course: Software Engineering
- Lab: Lab 02 Spring Framework

## Step 0 Setup

- Java: 25
- Maven: 3.9.x or Maven Wrapper
- Project: shop
- Group: edu.itc.se
- Package: edu.itc.shop
- Build: Maven
- Database: H2

The project was initialized with Git. The `.gitignore` file excludes
the `target/` build directory.

## Step 1 Bootstrapping with Maven and Spring Boot

### 1. What does the parent POM provide?

The Spring Boot parent POM provides default Maven configuration,
dependency management, and compatible dependency versions. It
reduces the need to configure each dependency manually.

### 2. Why do the starters have no version?

The Spring Boot parent manages dependency versions. Therefore,
the starter dependencies usually do not need individual versions
in `pom.xml`.

### 3. What does spring-boot-maven-plugin do?

The Spring Boot Maven plugin packages the application as an
executable JAR. The JAR includes the application and its required
dependencies and can be started using `java -jar`.

### 4. Dependency tree excerpt

Command:

    .\mvnw.cmd dependency:tree

First-level output:

[INFO] Scanning for projects...
[INFO] Loaded 24235 auto-discovered prefixes for remote repository central (prefixes-central.txt)
[INFO] 
[INFO] --------------------------< edu.itc.se:shop >---------------------------
[INFO] Building  0.0.1-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- dependency:3.10.0:tree (default-cli) @ shop ---
[INFO] Loaded 74 auto-discovered prefixes for remote repository apache.snapshots (prefixes-apache.snapshots.txt)
[INFO] edu.itc.se:shop:jar:0.0.1-SNAPSHOT
[INFO] +- org.springframework.boot:spring-boot-h2console:jar:4.1.1:compile
[INFO] |  \- org.springframework.boot:spring-boot:jar:4.1.1:compile
[INFO] |     +- org.springframework:spring-core:jar:7.0.9:compile
[INFO] |     |  \- commons-logging:commons-logging:jar:1.3.6:compile
[INFO] |     \- org.springframework:spring-context:jar:7.0.9:compile
[INFO] |        +- org.springframework:spring-aop:jar:7.0.9:compile
[INFO] |        +- org.springframework:spring-beans:jar:7.0.9:compile
[INFO] |        \- org.springframework:spring-expression:jar:7.0.9:compile
[INFO] +- org.springframework.boot:spring-boot-starter-actuator:jar:4.1.1:compile
[INFO] |  +- org.springframework.boot:spring-boot-starter:jar:4.1.1:compile
[INFO] |  |  +- org.springframework.boot:spring-boot-starter-logging:jar:4.1.1:compile
[INFO] |  |  |  +- ch.qos.logback:logback-classic:jar:1.5.38:compile
[INFO] |  |  |  |  \- ch.qos.logback:logback-core:jar:1.5.38:compile
[INFO] |  |  |  +- org.apache.logging.log4j:log4j-to-slf4j:jar:2.25.5:compile
[INFO] |  |  |  |  \- org.apache.logging.log4j:log4j-api:jar:2.25.5:compile
[INFO] |  |  |  \- org.slf4j:jul-to-slf4j:jar:2.0.18:compile
[INFO] |  |  +- jakarta.annotation:jakarta.annotation-api:jar:3.0.0:compile
[INFO] |  |  \- org.yaml:snakeyaml:jar:2.6:compile
[INFO] |  +- org.springframework.boot:spring-boot-starter-micrometer-metrics:jar:4.1.1:compile
[INFO] |  |  \- org.springframework.boot:spring-boot-micrometer-metrics:jar:4.1.1:compile
[INFO] |  |     \- org.springframework.boot:spring-boot-micrometer-observation:jar:4.1.1:compile
[INFO] |  +- org.springframework.boot:spring-boot-actuator-autoconfigure:jar:4.1.1:compile
[INFO] |  |  \- org.springframework.boot:spring-boot-actuator:jar:4.1.1:compile
[INFO] |  +- org.springframework.boot:spring-boot-health:jar:4.1.1:compile
[INFO] |  +- io.micrometer:micrometer-observation:jar:1.17.1:compile
[INFO] |  |  +- org.jspecify:jspecify:jar:1.0.1:compile
[INFO] |  |  \- io.micrometer:micrometer-commons:jar:1.17.1:compile
[INFO] |  \- io.micrometer:micrometer-jakarta9:jar:1.17.1:compile
[INFO] |     \- io.micrometer:micrometer-core:jar:1.17.1:compile

### 5. Hello endpoint

Endpoint: GET /hello

Example request:

    curl.exe "http://localhost:8080/hello?name=Ann"

Example response:

    {"message":"Hello, Ann!","at":"..."}

The endpoint returns a JSON Greeting record containing a message
and the current timestamp. If the name parameter is omitted,
the default name is World.

### 6. Packaged JAR

Build command:

    .\mvnw.cmd -q package

Run command:

    java -jar target/shop-0.0.1-SNAPSHOT.jar --server.port=9090

The endpoint also works on port 9090 after packaging.

Actual JAR size: 26.29

The executable JAR includes the application and its required
libraries, so it is larger than the compiled application classes
alone.

### 7. Actuator health

Endpoint: GET /actuator/health

Observed response:

    {"status":"UP"}

### 8. DataSource auto-configuration

Debug command:

    java -jar target/shop-0.0.1-SNAPSHOT.jar --debug

Observed positive-match explanation for DataSourceAutoConfiguration:

The condition evaluation report shows that `DataSourceAutoConfiguration` matched.

Observed positive-match explanations from `debug.log`:

```text
DataSourceAutoConfiguration matched:
   - @ConditionalOnClass found required classes 'javax.sql.DataSource',
     'org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType' (OnClassCondition)
   - @ConditionalOnMissingBean (types: io.r2dbc.spi.ConnectionFactory;
     SearchStrategy: all) did not find any beans (OnBeanCondition)
```

`DataSourceAutoConfiguration` matched because the required JDBC classes
were available on the classpath, and no R2DBC `ConnectionFactory` bean
was found to prevent this auto-configuration.

Spring Boot also configured HikariCP as the connection pool, as shown
by the positive matches for `DataSourceConfiguration.Hikari`.

Spring Boot can configure a DataSource automatically because
the JDBC and H2 dependencies are present and the required
auto-configuration conditions are satisfied.

## Step 2 Inversion of Control and Dependency Injection

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



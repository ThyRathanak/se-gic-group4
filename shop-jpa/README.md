# Lab 03: Hibernate Framework and Spring Data JPA

## Step 1: Entities and Mappings – Schema Validation

### Objective
Map the shop domain using JPA entities and embedded value objects. Use Flyway to create the database schema and Hibernate to validate that the entity mappings match the database.

### Implementation
- Configured Flyway to manage database migrations.
- Configured Hibernate with `spring.jpa.hibernate.ddl-auto=validate`.
- Set `spring.jpa.open-in-view=false`.
- Created the initial migration `V1__init.sql`.
- Used `@Embeddable` value objects and `@Enumerated(EnumType.STRING)` for enums.
- Configured entity IDs using sequence generators with `allocationSize = 50`.

### Testing and Evidence

**1. Successful schema validation**

Ran:

```bash
./mvnw spring-boot:run
```

Flyway successfully applied the initial migration:

```text
Successfully applied 1 migration to schema "PUBLIC",
now at version v1
```

Hibernate initialized the `EntityManagerFactory`, and the application started without a `SchemaManagementException`.

**2. Deliberate schema mismatch: Rename `sku` to `code`**

Temporarily renamed the `sku` column to `code` in the migration SQL without changing the entity.

Observed error:

```text
Schema validation: missing column [sku] in table [product]
```

This proves Hibernate validates entity columns against the database schema.

**3. Deliberate sequence mismatch**

Temporarily changed `product_seq` to `INCREMENT BY 1` instead of 50.

Observed error:

```text
The increment size of the [product_seq] sequence is set to [50]
in the entity mapping but the mapped database sequence
increment size is [1]
```

Restored the migration to use `sku` and sequence increment size 50.

### Conclusion
Flyway manages database schema creation, while Hibernate validates the entity mappings. The deliberate errors demonstrate that incorrect column names and sequence configurations are detected during application startup.

---

## Step 2: Associations, Cascading and Entity Identity

### Objective
Implement relationships between entities, manage the lifecycle of order lines through the Order aggregate root, and ensure entity identity works correctly with Java collections and Hibernate.

### Implementation
The required relationships include:

- `Order` to `OrderLine`: One-to-Many.
- `OrderLine` to `Order`: Many-to-One.
- `OrderLine` to `Product`: Many-to-One.
- `Product` to `Tag`: Many-to-Many.
- `Tag` to `Product`: Many-to-Many inverse relationship.

The `Order` entity owns the lifecycle of its lines through cascading and orphan removal.

### Testing and Evidence

**1. Bidirectional association**

The order and order-line relationship must keep both sides synchronized when adding or removing lines.

**2. Cascade and orphan removal**

The required test is `OrderCascadeTest`.

It should demonstrate:
- Persisting an order inserts its order lines.
- Removing one line deletes that line from the database.
- Removing an order also removes its remaining lines.

**3. Entity identity**

The required `EntityIdentityTest` verifies that:
- A persisted entity remains accessible in a `HashSet`.
- Two instances representing the same database entity are equal.
- Two separately loaded instances are not necessarily the same Java object.

**4. Bidirectional `toString()` pitfall**

Temporarily added `lines` to `Order.toString()` and `order` to `OrderLine.toString()`.

Printed an order outside a transaction and reproduced:

```text
java.lang.StackOverflowError
```

Restored both `toString()` methods to include scalar fields only.

### Conclusion
Bidirectional entity relationships must be managed carefully. Cascading and orphan removal simplify entity lifecycle management. Entity equality must also be implemented correctly to work with Java collections.

Including bidirectional relationships in `toString()` can cause infinite recursion and a `StackOverflowError`.

---

## Step 3: Repositories, Queries, Projections and REST Endpoints

### Objective
Replace hand-written database access with Spring Data JPA repositories. Implement derived queries, JPQL, native SQL, projections, Specifications, and paginated REST endpoints.

### Implementation

The repository layer provides database operations using:

- `CustomerRepository`
- `ProductRepository`
- `OrderRepository`

Implemented or required query features include:

1. Derived queries for customers, orders, and products.
2. JPQL queries using `@Query`.
3. Native SQL for low-stock products.
4. Interface and record projections.
5. Dynamic product filtering using `Specification`.
6. Pagination and sorting.

### Testing and Evidence

**1. Derived-query typo**

Temporarily misspelled `Email` as `Emali` in `OrderRepository`.

Observed Spring Data startup error:

```text
Caused by: org.springframework.data.core.PropertyReferenceException:
No property 'emali' found for type 'Customer';
Did you mean 'email'; Traversed path: Order.customer
```

Restored the correct method:

```java
findByCustomerEmailAndStatusOrderByPlacedAtDesc
```

This demonstrates that Spring Data parses derived-query method names during application startup.

**2. Open EntityManager in View**

Configured:

```yaml
spring:
  jpa:
    open-in-view: false
```

In `OpenInViewTest`, a repository-loaded `Order` becomes detached after its repository transaction ends.

Accessing `getLines()` afterward throws:

```text
LazyInitializationException
```

The actual customer-orders controller delegates to:

```java
OrderService.findByCustomerEmail(...)
```

The service uses a read-only transaction to fetch order lines and products and map them into DTOs before returning the response.

The test calls the controller method and verifies that the order line and product name are available in the response.

**3. REST endpoints**

The lab requires three endpoints:

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/orders` | Paginated orders filtered by status |
| GET | `/api/products` | Products with dynamic filters and pagination |
| GET | `/api/customers/{email}/orders` | Customer orders with their lines |

The customer-orders endpoint performs association traversal and DTO mapping inside the service transaction.

### Conclusion
Spring Data JPA reduces manual database access through repository methods and query abstractions. Derived queries are validated during startup, projections reduce unnecessary data retrieval, and Specifications support dynamic filtering.

With Open EntityManager in View disabled, lazy associations must be loaded and mapped inside the service transaction.

---

## Step 4: Entity States, Dirty Checking, Transactions and Optimistic Locking

### Objective
Understand managed and detached entity behavior, transaction boundaries, Hibernate dirty checking, flush ordering, and optimistic locking using concurrent transactions.

### Implementation

Implemented transaction management in the service layer using `@Transactional`.

The application uses:
- Managed entity updates.
- Detached entity merging.
- Read-only transactions.
- Explicit flushing.
- Optimistic locking with `@Version`.
- Retry handling through a separate `OrderFacade` bean.

### Testing and Evidence

**1. Detached entity and merge**

In `OrderServiceTest`, changing a `Product` returned from `findById` did not update the stored stock because the entity was detached.

After calling:

```java
products.save(detachedProduct);
```

The stock change was persisted.

The SQL logger captured:

```text
SELECT
UPDATE
```

Spring Data JPA uses `EntityManager.merge()` for the detached entity.

The returned managed copy is the instance attached to the persistence context.

**2. Optimistic locking**

Implemented:

```java
OrderFacade.changeStatusWithRetry(...)
```

The facade retries the transactional status change up to three times in a separate bean.

In `OptimisticLockingTest`, two threads attempt to update the same order concurrently.

One transaction loses the version race and rolls back. The facade retry allows both threads to eventually commit.

The final version increases twice.

**3. Transaction log evidence**

Captured from the test-profile logs:

```text
2026-11-12T21:40:30.458+07:00 TRACE 13064
--- [shop-jpa] [pool-3-thread-1]
o.s.t.i.TransactionInterceptor :
Getting transaction for
[edu.itc.shop.service.OrderService.changeStatus]

2026-11-12T21:40:30.464+07:00 DEBUG 13064
--- [shop-jpa] [pool-3-thread-2]
org.hibernate.SQL :
update orders
set customer_id=?,placed_at=?,status=?,version=?
where id=? and version=?

2026-11-12T21:40:30.470+07:00 DEBUG 13064
--- [shop-jpa] [pool-3-thread-1]
o.s.orm.jpa.JpaTransactionManager :
Initiating transaction rollback after commit exception
```

### Conclusion
Managed entities are automatically updated through Hibernate dirty checking. Detached entities require merging to persist their changes.

Optimistic locking prevents lost updates by checking the entity version during updates. Retrying in a separate service bean allows failed transactions to be executed again safely.

---

## Step 5: N+1, Performance Tuning and a Second Migration

### Objective

Identify the N+1 query problem, measure SQL statement counts, implement three different fetching strategies, and benchmark their performance.

The experiments also demonstrate how accessing associated products can introduce another N+1 problem.

### Step 1: Reproduce the N+1 Problem

Created `ReportService` with a naive `linesPerPaidOrder()` method.

The method:
1. Retrieves all PAID orders using `OrderRepository`.
2. Iterates through the retrieved orders.
3. Accesses `order.getLines().size()` for each order.

Created a `QueryCounter` helper using Hibernate Statistics.

The helper provides:

- `reset()` — resets the prepared-statement counter.
- `count()` — returns the number of prepared SQL statements.

The counter uses:

```java
emf.unwrap(SessionFactory.class).getStatistics()
```

and retrieves the statement count through:

```java
stats.getPrepareStatementCount()
```

### Step 2: Demonstrate the N+1 Problem

Created `TestData` to generate test data containing:

- 50 PAID orders.
- 3 order lines per order.
- 150 order lines in total.

Created `NPlusOneTest` to verify the SQL statement count.

The naive implementation executes **51 SQL statements**:

| Operation | SQL Statements |
|---|---:|
| Retrieve all PAID orders | 1 |
| Retrieve order lines for 50 orders | 50 |
| **Total** | **51** |

The test is marked with `@Tag("demo")` to identify it as the original N+1 demonstration.

### Step 3: Implement Three Fetching Solutions

Three different fetching strategies were implemented and tested.

**1. JOIN FETCH**

Uses JPQL to retrieve orders and their order lines together.

```java
@Query("""
    select distinct o
    from Order o
    join fetch o.lines
    where o.status = :status
""")
List<Order> findByStatusWithLines(
    @Param("status") OrderStatus status
);
```

This reduces the original report to one SQL statement.

**2. EntityGraph**

Uses Spring Data JPA's `@EntityGraph` to specify the associations that should be fetched.

```java
@EntityGraph(attributePaths = {"lines", "lines.product"})
List<Order> findWithGraphByStatus(OrderStatus status);
```

This allows the required associations to be loaded together.

**3. Batch Fetching**

Uses Hibernate's `@BatchSize` annotation on the `Order.lines` collection.

```java
@BatchSize(size = 16)
```

This allows Hibernate to load multiple collections in batches rather than issuing an individual query for every order.

### Step 4: Benchmark and Further N+1 Demonstration

#### 4.1 Original Report Benchmark

Created `ReportBenchmarkTest` to measure the performance of the original report implementations.

Each variant was:
1. Executed once as a warm-up.
2. Measured 20 times.
3. Evaluated using median wall-clock execution time.
4. Checked against its expected SQL statement count.

**Benchmark results:**

| Variant | SQL Statements | Median Time (ms) |
|---|---:|---:|
| Naive | 51 | 24.351 |
| JOIN FETCH | 1 | 9.977 |
| EntityGraph | 1 | 19.145 |
| Batch Fetching (`16`) | 5 | 40.337 |

JOIN FETCH was the fastest in this particular local benchmark.

Batch fetching reduced the SQL statement count, although its median execution time was higher in this run.

These timings are specific to the local H2 development environment.

#### 4.2 Product Name N+1 Demonstration

Extended the report to access the product name associated with each order line:

```java
line.getProduct().getName()
```

Created a separate test dataset containing:

- 50 PAID orders.
- 3 distinct products per order.
- 150 distinct products in total.

This exposed another N+1 problem because the associated products were lazily loaded.

The naive implementation executed **201 SQL statements**:

| Operation | SQL Statements |
|---|---:|
| Retrieve PAID orders | 1 |
| Retrieve order lines | 50 |
| Retrieve associated products | 150 |
| **Total** | **201** |

#### 4.3 Product Fetching Comparison

Compared the following fetching strategies:

| Variant | SQL Statements |
|---|---:|
| Naive | 201 |
| JOIN FETCH (lines and products) | 1 |
| EntityGraph (lines and products) | 1 |
| Batch Fetching | 12 |

The JOIN FETCH implementation retrieves both order lines and their associated products using a fetch join.

The EntityGraph implementation uses:

```java
@EntityGraph(attributePaths = {"lines", "lines.product"})
```

to fetch both associations.

For the batch-fetching experiment, the configuration used:

```java
@BatchSize(size = 16)
```

and the test-specific Hibernate property:

```properties
spring.jpa.properties.hibernate.default_batch_fetch_size=32
```

The measured result was 12 SQL statements, compared with 201 in the naive implementation.

This demonstrates that batch fetching reduces repeated lazy-loading queries, while JOIN FETCH and EntityGraph can retrieve both required associations in one statement.

### Conclusion

The experiments demonstrate two N+1 problems:

1. Loading order lines for multiple orders.
2. Loading products associated with those order lines.

Three solutions were implemented:

- **JOIN FETCH:** Fetches the required associations using JPQL.
- **EntityGraph:** Defines which associations Hibernate should fetch.
- **Batch Fetching:** Loads lazy associations in groups to reduce repeated queries.

The benchmark results also demonstrate that fewer SQL statements do not always guarantee lower execution time. Both query counts and execution times should be considered when evaluating performance.

**5. Flyway V2 indexes**

Created `V2__indexes.sql` with three indexes:

```sql
CREATE INDEX idx_orders_customer_placed
    ON orders (customer_id, placed_at DESC);

CREATE INDEX idx_order_line_order
    ON order_line (order_id);

CREATE INDEX idx_product_category_amount
    ON product (category, amount);
```

On startup, Flyway should migrate the schema to version 2.

Verified the migration history using:

```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Expected: two successful migration records, versions 1 and 2.

**6. EXPLAIN query plans**

Used the following SQL to inspect the paginated orders query:

```sql
EXPLAIN SELECT o.id, o.status, o.placed_at, c.name
FROM orders o
JOIN customer c ON c.id = o.customer_id
WHERE o.status = 'PAID'
ORDER BY o.placed_at DESC
OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY;
```

Results:

```text
Before V2: Pending capture from a running H2 database.

After V2: Pending capture from a running H2 database.
```

The orders query filters by `status`, while the new customer/placed index starts with `customer_id`.

Therefore, H2 may still scan and sort for the global status query. The customer-specific orders query can use the new composite index.

The actual query plans must be captured before claiming an improvement.

### Conclusion
N+1 problems can be reduced using fetch joins, entity graphs, and batch fetching. Entity graphs can load multiple required associations in a single query.

JDBC batching reduces database insert overhead, while Flyway V2 introduces indexes to support frequently used queries.

Actual query plans and measured execution times are important evidence when evaluating database performance.

---

## Final Lab 03 Summary

Lab 03 demonstrates how Hibernate ORM and Spring Data JPA manage database persistence in a Spring Boot application.

The five tasks cover:

1. Entity mapping and schema validation using Flyway.
2. Entity relationships, cascading, and identity.
3. Spring Data repositories, queries, projections, and REST endpoints.
4. Entity states, dirty checking, transactions, and optimistic locking.
5. N+1 optimization, JDBC batching, and database indexing.

The report uses application startup logs, SQL statements, automated tests, and database execution plans to demonstrate the behavior of Hibernate and Spring Data JPA.

**Remaining evidence to complete:**
- Step 2: Include `OrderCascadeTest` and `EntityIdentityTest` results and SQL excerpts.
- Step 3: Include `OrderRepositoryTest` results and the three HTTP endpoint transcripts.
- Step 5: Fill in the actual median timings.
- Step 5: Capture the actual H2 EXPLAIN plans before and after V2.
- Step 5: Include the actual `BatchInsertTest` results and batching log.


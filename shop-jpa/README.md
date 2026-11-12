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
Identify and fix N+1 query problems, measure database performance, enable JDBC insert batching, verify the effect of Open EntityManager in View, and add database indexes using Flyway V2.

### Implementation

**1. N+1 query problem**

Implemented `NPlusOneTest` to measure prepared-statement counts for report queries.

The test warms each report once, then measures 20 calls and calculates the median execution time.

The report uses 50 orders with 3 lines each. Product entities are unique per order line, so accessing product names also exposes lazy-loading queries.

**2. N+1 solutions**

Compared four approaches:

- Naive implementation with collection and product batch fetching.
- Fetch join for order lines.
- Entity graph for order lines and products.
- Batch fetching.

Actual statement counts from the report:

| Variant | Statements | Median ms |
|---|---:|---:|
| Naive + collection/product batch fetching | 10 | Pending |
| Fetch join for lines | 6 | Pending |
| Entity graph for lines and products | 1 | Pending |
| Batch fetching | 10 | Pending |

The original 51-statement baseline was measured before collection batching and product-name access.

**3. Product-name regression**

Fetching order lines alone does not fetch their products.

When the report accesses:

```java
line.getProduct().getName()
```

additional product queries can occur.

With:

```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 32
```

Hibernate groups lazy product loading into batches.

For 150 unique products:

```text
ceil(150 / 32) = 5 statements
```

The entity graph loads both associations in a single query.

**4. JDBC insert batching**

Configured:

```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc.batch_size: 50
        order_inserts: true
```

`BatchInsertTest` inserts 1,000 products inside one transaction and checks Hibernate's entity insert and prepared-statement counters.

Enabled batch logging:

```yaml
logging:
  level:
    org.hibernate.engine.jdbc.batch: TRACE
```

The identity-generator comparison belongs on a scratch branch. Changing the product ID strategy to `GenerationType.IDENTITY` disables insert batching and should increase the prepared-statement count toward 1,000.

The active entity mapping remains sequence-based with allocation size 50.

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

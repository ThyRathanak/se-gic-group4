# Shop JPA schema validation

## Startup validation

Run `./mvnw spring-boot:run`. Flyway applied the initial migration:

```text
Successfully applied 1 migration to schema "PUBLIC", now at version v1 (execution time 00:00.013s)
```

Hibernate initialized the `EntityManagerFactory` and the application started without a `SchemaManagementException`.

## Open EntityManager in View

`spring.jpa.open-in-view=false` is set in `application.yaml`. In
`OpenInViewTest`, a repository-loaded `Order` is detached once the repository
transaction ends, and touching `getLines()` then throws
`LazyInitializationException`. The real `GET /api/customers/{email}/orders`
controller delegates to `OrderService.findByCustomerEmail`; that read-only
transaction fetches lines and products and maps them into DTOs before returning.
The test calls this controller method and verifies the line and product name
are available in the response. Keep association traversal and DTO mapping in
the service transaction when Open EntityManager in View is disabled.

## V2 indexes

`V2__indexes.sql` adds `idx_orders_customer_placed`, `idx_order_line_order`,
and `idx_product_category_amount`. On application startup, Flyway should log
that it is migrating the `PUBLIC` schema to version 2 (`indexes`); verify the
history in the H2 console with:

```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Expected: two successful rows, versions 1 and 2. Capture the actual H2 plans
for the paginated `/api/orders?status=PAID&page=0&size=20&sort=placedAt,desc`
query in the console with this equivalent SQL before and after V2:

```sql
EXPLAIN SELECT o.id, o.status, o.placed_at, c.name
FROM orders o JOIN customer c ON c.id = o.customer_id
WHERE o.status = 'PAID'
ORDER BY o.placed_at DESC
OFFSET 0 ROWS FETCH NEXT 20 ROWS ONLY;
```

```text
Before V2: pending capture from a running H2 database.
After V2:  pending capture from a running H2 database.
```

The orders endpoint filters on `status`, while the customer/placed index starts
with `customer_id`; H2 may therefore still scan and sort for this global status
query. The customer-specific orders query can use the new composite index. Do
not treat a plan as improved until the captured H2 output confirms it.

## Deliberate schema mismatches

These messages were captured by changing only the migration SQL for each run, then restoring it.

### Rename `sku` to `code` in SQL only

```text
Schema validation: missing column [sku] in table [product]
```

### Set `product_seq` to `INCREMENT BY 1`

```text
The increment size of the [product_seq] sequence is set to [50] in the entity mapping but the mapped database sequence increment size is [1]
```

The migration has been restored to use the entity column `sku` and sequence increment size `50`.

## Bidirectional `toString()` pitfall

Temporarily adding `lines` to `Order.toString()` and `order` to `OrderLine.toString()`, then printing an order outside a transaction, reproduced this exception:

```text
java.lang.StackOverflowError
```

The two `toString()` methods were restored to include scalar fields only.

## N+1 report measurements

`NPlusOneTest` warms each report once, then measures 20 calls and prints the
median wall time and prepared-statement count. Products are unique per order
line so reading `line.getProduct().getName()` exposes lazy product loading.
With 50 orders, 3 lines each, `@BatchSize(16)` on `Order.lines`, and
`hibernate.default_batch_fetch_size=32`, the expected statement counts are:

| variant | statements | median ms |
|---|---:|---:|
| naive + collection/product batch fetching | 10 | pending local test run |
| fetch join for lines | 6 | pending local test run |
| entity graph for lines and products | 1 | pending local test run |
| batch fetching | 10 | pending local test run |

The original 51-statement baseline predates collection batching and product
name access. Fetching lines
alone still regresses when the report touches products: the fetch-join version
uses additional product selects. A default batch size of 32 groups those lazy
product loads into `ceil(150 / 32) = 5` statements; the entity graph loads both
associations in its single query. The test prints measured medians prefixed
with `N+1 median:`; copy those values into the table after running
`./mvnw -Dtest=NPlusOneTest test`.

## JDBC insert batching

The application enables `hibernate.jdbc.batch_size=50` and
`hibernate.order_inserts=true`. `BatchInsertTest` inserts 1,000 products in
one transaction, then checks Hibernate's entity insert and prepared-statement
counters. Hibernate's batch logger is enabled at TRACE under
`org.hibernate.engine.jdbc.batch`.

The identity-generator comparison belongs on a scratch branch: changing
`Product` to `GenerationType.IDENTITY` disables insert batching and should
raise the prepared-statement counter toward 1,000. The active entity mapping
remains sequence-based with allocation size 50.

## Derived-query typo

Temporarily misspelling `Email` as `Emali` in `OrderRepository` caused application startup to fail with this Spring Data error:

```text
Caused by: org.springframework.data.core.PropertyReferenceException: No property 'emali' found for type 'Customer'; Did you mean 'email'; Traversed path: Order.customer
```

The repository method name has been restored to `findByCustomerEmailAndStatusOrderByPlacedAtDesc`.

## Detached entity merge

In `OrderServiceTest`, changing a `Product` returned from `findById` did not alter the stored stock until `save(detachedProduct)` was called. The SQL logger captured one `SELECT` followed by one `UPDATE` for that save. Spring Data JPA uses `EntityManager.merge` for this detached entity; the returned managed copy is the instance attached to the persistence context.

## Optimistic locking and retry

`OrderFacade.changeStatusWithRetry` retries the transactional status change up to three times in a separate bean. In `OptimisticLockingTest`, one direct concurrent update loses the version race and rolls back; the facade version lets both threads eventually commit, incrementing the version twice.

Captured from the test-profile logs:

```text
2026-11-12T21:40:30.458+07:00 TRACE 13064 --- [shop-jpa] [pool-3-thread-1] o.s.t.i.TransactionInterceptor : Getting transaction for [edu.itc.shop.service.OrderService.changeStatus]
2026-11-12T21:40:30.464+07:00 DEBUG 13064 --- [shop-jpa] [pool-3-thread-2] org.hibernate.SQL : update orders set customer_id=?,placed_at=?,status=?,version=? where id=? and version=?
2026-11-12T21:40:30.470+07:00 DEBUG 13064 --- [shop-jpa] [pool-3-thread-1] o.s.orm.jpa.JpaTransactionManager : Initiating transaction rollback after commit exception
```

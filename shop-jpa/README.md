# Shop JPA schema validation

## Startup validation

Run `./mvnw spring-boot:run`. Flyway applied the initial migration:

```text
Successfully applied 1 migration to schema "PUBLIC", now at version v1 (execution time 00:00.013s)
```

Hibernate initialized the `EntityManagerFactory` and the application started without a `SchemaManagementException`.

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

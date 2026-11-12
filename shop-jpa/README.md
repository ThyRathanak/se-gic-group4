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

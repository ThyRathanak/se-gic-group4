## Task 2 – Associations, cascading and entity identity

### Mapping
- `OrderLine.order` (`@ManyToOne`) is the **owning side**: it writes `order_line.order_id`.
- `Order.lines` is the **inverse side**: `@OneToMany(mappedBy = "order", cascade = ALL, orphanRemoval = true)`.
  `addLine(Product, int)` creates the line with `order = this` and copies `product.getPrice()` into `unitPrice`;
  `removeLine(OrderLine)` removes it from the list and sets `line.order = null`. `getLines()` is read-only.
- `Product.tags` owns the `product_tag` join table; `Tag.products` is `mappedBy = "tags"`. Both are `Set`s,
  and `Product.tag(Tag)` updates both sides.
- Every entity has hand-written `equals` (same class + non-null equal id), a constant `hashCode` per class,
  and a `toString` with scalar fields only. No Lombok.

### Test result
```text
$ ./mvnw test -Dtest=OrderCascadeTest,EntityIdentityTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Persisting an order: 1 + N inserts, no update
```text
DEBUG org.hibernate.SQL : select next value for orders_seq
DEBUG org.hibernate.SQL : select next value for order_line_seq
DEBUG org.hibernate.SQL : select next value for order_line_seq
DEBUG org.hibernate.SQL : insert into orders (customer_id,placed_at,status,version,id) values (?,?,?,?,?)
DEBUG org.hibernate.SQL : insert into order_line (order_id,product_id,quantity,amount,currency,id) values (?,?,?,?,?,?)
DEBUG org.hibernate.SQL : insert into order_line (order_id,product_id,quantity,amount,currency,id) values (?,?,?,?,?,?)
```
One insert into `orders` and two into `order_line`, with no `update`. The owning side (`line.order`) is set
before persist, so `order_id` is already bound in each INSERT (`binding parameter (1:BIGINT) <- [1]`).

### Removing a line: orphan removal
```text
DEBUG org.hibernate.SQL : delete from order_line where id=?
DEBUG org.hibernate.SQL : select count(ol1_0.id) from order_line ol1_0
```
The count drops from 2 to 1. Removing the whole order then cascades REMOVE:
```text
DEBUG org.hibernate.SQL : delete from order_line where id=?
DEBUG org.hibernate.SQL : delete from orders where id=? and version=?
```

### Entity identity
A new `Product` added to a `HashSet` is still found after it is persisted and gets an id, because `hashCode`
does not depend on the id. After `clear()` and reloading, the two instances are `equals` but not `==`.

### The toString pitfall
I temporarily put `order` in `OrderLine.toString()` and `lines` in `Order.toString()`, then printed an order
after its EntityManager was closed:
```text
(paste your real exception from step E here)
```
The `lines` collection is lazy and there is no open session, so Hibernate cannot load it
(`LazyInitializationException`). If the collection is already loaded, the two `toString` methods call each other
forever and end in `StackOverflowError`. I reverted both methods to scalar fields only.
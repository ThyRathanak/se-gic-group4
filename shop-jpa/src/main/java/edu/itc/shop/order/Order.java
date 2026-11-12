
package edu.itc.shop.order;

import edu.itc.shop.customer.Customer;
import edu.itc.shop.domain.Money;
import edu.itc.shop.product.Product;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "order_seq"
    )
    @SequenceGenerator(
        name = "order_seq",
        sequenceName = "order_seq",
        allocationSize = 50
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Instant placedAt;

    @Version
    private long version;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> lines = new ArrayList<>();

    protected Order() {
    }

    public Order(Customer customer, OrderStatus status, Instant placedAt) {
        this.customer = customer;
        this.status = status;
        this.placedAt = placedAt;
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getPlacedAt() {
        return placedAt;
    }

    public long getVersion() {
        return version;
    }

    public List<OrderLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public void addLine(Product product, int quantity) {
        OrderLine line = new OrderLine(this, product, quantity, product.getPrice());
        lines.add(line);
    }

    public void removeLine(OrderLine line) {
        if (lines.remove(line)) {
            line.detachFromOrder();
        }
    }

    public Money total() {
        if (lines.isEmpty()) {
            return Money.eur("0.00");
        }

        String currency = lines.get(0).getUnitPrice().currency();
        BigDecimal amount = BigDecimal.ZERO;

        for (OrderLine line : lines) {
            Money unitPrice = line.getUnitPrice();
            if (!currency.equals(unitPrice.currency())) {
                throw new IllegalStateException("Cannot total order lines with different currencies");
            }
            amount = amount.add(unitPrice.times(line.getQuantity()).amount());
        }

        return new Money(amount, currency);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Order order = (Order) object;
        return id != null && id.equals(order.id);
    }

    @Override
    public int hashCode() {
        return Order.class.hashCode();
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", status=" + status +
                ", placedAt=" + placedAt +
                ", version=" + version +
                '}';
    }
}

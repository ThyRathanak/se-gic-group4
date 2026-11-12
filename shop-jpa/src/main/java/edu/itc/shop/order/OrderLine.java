
package edu.itc.shop.order;

import edu.itc.shop.domain.Money;
import edu.itc.shop.product.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "order_line")
public class OrderLine {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "order_line_seq"
    )
    @SequenceGenerator(
        name = "order_line_seq",
        sequenceName = "order_line_seq",
        allocationSize = 50
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Embedded
    private Money unitPrice;

    protected OrderLine() {
    }

    public OrderLine(
            Order order,
            Product product,
            int quantity,
            Money unitPrice
    ) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    void detachFromOrder() {
        this.order = null;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        OrderLine orderLine = (OrderLine) object;
        return id != null && id.equals(orderLine.id);
    }

    @Override
    public int hashCode() {
        return OrderLine.class.hashCode();
    }

    @Override
    public String toString() {
        return "OrderLine{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                '}';
    }
}

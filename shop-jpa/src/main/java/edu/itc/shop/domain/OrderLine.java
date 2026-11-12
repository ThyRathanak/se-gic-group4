package edu.itc.shop.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Embedded;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_line")
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_line_seq_gen")
    @SequenceGenerator(
        name = "order_line_seq_gen",
        sequenceName = "order_line_seq",
        allocationSize = 50
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    @Embedded
    private Money unitPrice;

    protected OrderLine() {
    }

    @Override
    public boolean equals(Object o) {   
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderLine other = (OrderLine) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return OrderLine.class.hashCode();
    }

    @Override
    public String toString() {
        return "OrderLine[id=%s, quantity=%d, unitPrice=%s]"
            .formatted(id, quantity, unitPrice);
    }

    public OrderLine(Order order, Product product, int quantity, Money unitPrice) {
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

    public void setOrder(Order order) {
        this.order = order;
    }
}
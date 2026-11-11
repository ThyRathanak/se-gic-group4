package edu.itc.shop.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq")
    @SequenceGenerator(name = "order_seq", sequenceName = "order_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
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
    private List<OrderLine> orderLines = new ArrayList<>();

    protected Order() {}

    public Order(Customer customer, OrderStatus status, Instant placedAt) {
        this.customer = customer;
        this.status = status;
        this.placedAt = placedAt;
    }

    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public OrderStatus getStatus() { return status; }
    public Instant getPlacedAt() { return placedAt; }
    public long getVersion() { return version; }
    public List<OrderLine> getOrderLines() { return orderLines; }
}
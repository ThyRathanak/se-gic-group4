package edu.itc.shop.service;

import edu.itc.shop.domain.Order;
import edu.itc.shop.repository.OrderRepository;
import edu.itc.shop.web.dto.PlaceOrder;
import java.util.List;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orders;

    public OrderService(OrderRepository orders) {
        this.orders = orders;
    }

    @PreAuthorize("#customerId == authentication.name")
    @Transactional
    public Order place(String customerId, PlaceOrder cmd) {
        return orders.save(new Order(
                customerId,
                cmd.productId(),
                cmd.quantity()
        ));
    }

    @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.name")
    @Transactional(readOnly = true)
    public List<Order> history(String customerId) {
        return orders.findByCustomer(customerId);
    }

    @PostAuthorize("hasRole('ADMIN') or returnObject.customer == authentication.name")
    @Transactional(readOnly = true)
    public Order get(long id) {
        return orders.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found: " + id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Order ship(long id) {
        Order order = orders.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found: " + id));

        order.ship();
        return orders.save(order);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<Order> all() {
        return orders.findAll();
    }
}
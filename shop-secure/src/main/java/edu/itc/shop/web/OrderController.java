package edu.itc.shop.web;

import edu.itc.shop.domain.Order;
import edu.itc.shop.service.OrderService;
import edu.itc.shop.web.dto.PlaceOrder;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order place(
            @RequestBody PlaceOrder cmd,
            org.springframework.security.core.Authentication authentication) {

        return orders.place(authentication.getName(), cmd);
    }

    @GetMapping("/mine")
    public List<Order> mine(
            org.springframework.security.core.Authentication authentication) {

        return orders.history(authentication.getName());
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable long id) {
        return orders.get(id);
    }

    @PostMapping("/{id}/ship")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ship(@PathVariable long id) {
        orders.ship(id);
    }

    @GetMapping
    public List<Order> all() {
        return orders.all();
    }
}
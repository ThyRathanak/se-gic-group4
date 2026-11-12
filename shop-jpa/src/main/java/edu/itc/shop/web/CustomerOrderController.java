package edu.itc.shop.web;

import edu.itc.shop.service.OrderService;
import edu.itc.shop.web.dto.OrderDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerOrderController {

    private final OrderService orderService;

    public CustomerOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{email}/orders")
    public List<OrderDto> ordersForCustomer(@PathVariable String email) {
        return orderService.findByCustomerEmail(email);
    }
}

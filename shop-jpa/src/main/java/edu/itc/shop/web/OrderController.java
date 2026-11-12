package edu.itc.shop.web;

import edu.itc.shop.order.OrderStatus;
import edu.itc.shop.service.OrderService;
import edu.itc.shop.web.dto.OrderDto;
import edu.itc.shop.web.dto.OrderSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public Page<OrderSummary> byStatus(
            @RequestParam OrderStatus status,
            @PageableDefault(size = 20, sort = "placedAt", direction = DESC) Pageable pageable
    ) {
        return orderService.findByStatus(status, pageable);
    }
}

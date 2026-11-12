package edu.itc.shop.web.dto;

import edu.itc.shop.domain.Money;
import edu.itc.shop.order.OrderStatus;

import java.time.Instant;
import java.util.List;

public record OrderDto(
        Long id,
        OrderStatus status,
        Instant placedAt,
        Money total,
        List<OrderLineDto> lines
) {
}

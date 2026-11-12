package edu.itc.shop.web.dto;

import edu.itc.shop.domain.Money;

public record OrderLineDto(
        Long id,
        Long productId,
        String sku,
        String name,
        int quantity,
        Money unitPrice
) {
}

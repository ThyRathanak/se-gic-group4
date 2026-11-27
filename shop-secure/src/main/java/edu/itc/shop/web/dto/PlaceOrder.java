package edu.itc.shop.web.dto;

public record PlaceOrder(
        Long productId,
        int quantity
) {
}
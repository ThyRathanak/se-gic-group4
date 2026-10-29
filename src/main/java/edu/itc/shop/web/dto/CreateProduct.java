package edu.itc.shop.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateProduct(
    @NotBlank(message = "Name is required")
    @Size(max = 80, message = "Name must not exceed 80 characters")
    String name,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    BigDecimal price,

    @PositiveOrZero(message = "Stock cannot be negative")
    int stock
) {}

package edu.itc.shop.common;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public record Money(
        BigDecimal amount,
        String currency
) {
}

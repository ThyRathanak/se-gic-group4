package edu.itc.shop.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public record Money(
    @Column(precision = 12, scale = 2) BigDecimal amount,
    @Column(length = 3) String currency
) {
    public static Money eur(String amount) {
        return new Money(new BigDecimal(amount), "EUR");
    }

    public Money times(int multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }
}
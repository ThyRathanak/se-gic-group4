package edu.itc.shop.domain;

import java.math.BigDecimal;

public record Money(BigDecimal amount, String currency) {

    public static Money parse(String value) {
        String[] parts = value.trim().split("\\s+");

        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Money must use the format: amount currency"
            );
        }

        try {
            BigDecimal amount = new BigDecimal(parts[0]);
            String currency = parts[1];

            return new Money(amount, currency);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Invalid money amount: " + value, ex
            );
        }
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + currency;
    }
}
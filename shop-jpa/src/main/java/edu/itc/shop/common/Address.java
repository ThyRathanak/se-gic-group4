
package edu.itc.shop.common;

import jakarta.persistence.Embeddable;

@Embeddable
public record Address(
        String street,
        String city,
        String country
) {
}

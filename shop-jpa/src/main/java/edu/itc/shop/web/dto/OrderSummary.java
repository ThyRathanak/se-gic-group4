package edu.itc.shop.web.dto;

import edu.itc.shop.order.OrderStatus;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;

public interface OrderSummary {

    Long getId();

    OrderStatus getStatus();

    Instant getPlacedAt();

    @Value("#{T(edu.itc.shop.web.dto.OrderSummary$CustomerName).of(target.customerName)}")
    CustomerName getCustomer();

    interface CustomerName {
        String getName();

        static CustomerName of(String name) {
            return () -> name;
        }
    }
}

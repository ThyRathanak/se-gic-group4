package edu.itc.shop.domain;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

public class InsufficientStockException extends ClientErrorException {

    public InsufficientStockException(long productId, int currentStock, int delta) {
        super("Cannot change stock for product " + productId
                + " by " + delta + "; current stock is " + currentStock,
                Response.Status.CONFLICT);
    }
}

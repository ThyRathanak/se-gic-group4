package edu.itc.shop.domain;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

public class InsufficientStockException extends ClientErrorException {

    public InsufficientStockException(long id) {
        super("Product " + id + " does not have enough stock",
                Response.Status.CONFLICT);
    }
}
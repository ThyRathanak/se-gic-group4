package edu.itc.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.itc.shop.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void restock(long fromId, long toId, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be positive");
        }

        int removed = repository.updateStock(fromId, -qty);

        if (removed == 0) {
            throw new IllegalStateException(
                    "Source product not found or insufficient stock");
        }

        int added = repository.updateStock(toId, qty);

        if (added == 0) {
            throw new IllegalStateException(
                    "Destination product not found");
        }
    }
}
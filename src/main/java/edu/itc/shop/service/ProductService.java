package edu.itc.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import edu.itc.shop.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void restock(long fromId, long toId, int qty) {
        int rowsAffected1 = productRepository.updateStock(fromId, -qty);
        if (rowsAffected1 == 0) {
            throw new IllegalStateException("Not enough stock in source product or product not found!");
        }

        int rowsAffected2 = productRepository.updateStock(toId, qty);
        if (rowsAffected2 == 0) {
            throw new IllegalStateException("Target product not found!");
        }
    }
}
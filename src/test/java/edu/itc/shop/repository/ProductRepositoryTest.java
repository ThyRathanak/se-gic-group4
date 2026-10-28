package edu.itc.shop.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.itc.shop.domain.Product;

@SpringBootTest
@ActiveProfiles("test")
@JdbcTest
@Import(ProductRepository.class)
class ProductRepositoryTest {

    @Autowired
    ProductRepository repository;

    @Test
    void findsFiveInitialProducts() {
        assertEquals(5, repository.findAll().size());
    }

    @Test
    void findsProductById() {
        assertTrue(repository.findById(1).isPresent());
    }

    @Test
    void insertsAndDeletesProduct() {
        Product product = new Product(
                null, "Webcam", new BigDecimal("35.00"), 8);

        long id = repository.insert(product);

        assertTrue(repository.findById(id).isPresent());
        assertEquals(1, repository.delete(id));
        assertFalse(repository.findById(id).isPresent());
    }

    @Test
    void searchesByName() {
        assertFalse(
                repository.findByNameContaining("lap").isEmpty());
    }
}
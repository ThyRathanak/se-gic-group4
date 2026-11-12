package edu.itc.shop;

import edu.itc.shop.domain.Category;
import edu.itc.shop.domain.Money;
import edu.itc.shop.domain.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductMappingTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistAndReloadMoneyAndCategory() {
        Money price = new Money(new BigDecimal("29.99"), "USD");

        Product product = new Product(
            "TEST-001",
            "Test Product",
            price,
            Category.BOOKS,
            10
        );

        entityManager.persist(product);
        entityManager.flush();

        Long productId = product.getId();

        entityManager.clear();

        Product reloaded = entityManager.find(Product.class, productId);

        assertThat(reloaded).isNotNull();
        assertThat(reloaded.getSku()).isEqualTo("TEST-001");
        assertThat(reloaded.getPrice().amount())
            .isEqualByComparingTo("29.99");
        assertThat(reloaded.getPrice().currency()).isEqualTo("USD");
        assertThat(reloaded.getCategory()).isEqualTo(Category.BOOKS);
    }
}
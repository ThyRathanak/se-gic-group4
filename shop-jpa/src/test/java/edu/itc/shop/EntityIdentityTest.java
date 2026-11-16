package edu.itc.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import edu.itc.shop.domain.Category;
import edu.itc.shop.domain.Money;
import edu.itc.shop.domain.Product;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
class EntityIdentityTest {

    @Autowired
    private EntityManager em;

    @Test
    void shouldMaintainHashSetMembershipAndEntityEquality() {
        Product product = new Product(
                "ID-001",
                "Test Product",
                Money.eur("20.00"),
                Category.HARDWARE,
                10
        );

        Set<Product> products = new HashSet<>();
        products.add(product);

        em.persist(product);
        em.flush();

        assertTrue(products.contains(product));

        Long productId = product.getId();

        em.clear();

        Product reloadedProduct = em.find(Product.class, productId);

        assertEquals(product, reloadedProduct);
        assertFalse(product == reloadedProduct);
    }
}
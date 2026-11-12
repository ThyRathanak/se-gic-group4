package edu.itc.shop.product;

import edu.itc.shop.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class ProductMappingTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void persistsAndReloadsMoneyAndStringCategory() {
        Product product = new Product(
                "KB-001",
                "Mechanical Keyboard",
                Money.eur("89.90"),
                Category.HARDWARE,
                12
        );

        entityManager.persist(product);
        entityManager.flush();
        Long productId = product.getId();
        entityManager.clear();

        Product reloaded = entityManager.find(Product.class, productId);

        assertThat(reloaded.getPrice().amount()).isEqualByComparingTo(new BigDecimal("89.90"));
        assertThat(reloaded.getPrice().currency()).isEqualTo("EUR");
        assertThat(reloaded.getCategory()).isEqualTo(Category.HARDWARE);

        Object storedCategory = entityManager.getEntityManager()
                .createNativeQuery("SELECT category FROM product WHERE id = :id")
                .setParameter("id", productId)
                .getSingleResult();
        assertThat(storedCategory.toString()).isEqualTo("HARDWARE");
    }
}

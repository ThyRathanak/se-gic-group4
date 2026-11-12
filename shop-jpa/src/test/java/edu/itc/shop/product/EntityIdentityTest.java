package edu.itc.shop.product;

import edu.itc.shop.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class EntityIdentityTest {

    @Autowired
    private TestEntityManager em;

    @Test
    void hashSetMembershipSurvivesPersistenceAndReload() {
        Product product = new Product(
                "ID-001",
                "Identity Test Product",
                Money.eur("19.95"),
                Category.HARDWARE,
                5
        );
        Set<Product> products = new HashSet<>();
        products.add(product);

        em.persist(product);
        em.flush();

        assertThat(products).contains(product);

        Long productId = product.getId();
        em.clear();
        Product reloaded = em.find(Product.class, productId);

        assertThat(reloaded).isEqualTo(product);
        assertThat(reloaded).isNotSameAs(product);
        assertThat(products).contains(reloaded);
    }
}

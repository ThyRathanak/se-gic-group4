package edu.itc.shop.domain;

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

    @Autowired TestEntityManager em;

    @Test
    void hashSetStillContainsProductAfterPersist() {
        Product p = new Product("ID-001", "Notebook", Money.eur("3.50"), Category.BOOKS);
        Set<Product> set = new HashSet<>();
        set.add(p);

        em.persist(p);
        em.flush();

        assertThat(p.getId()).isNotNull();
        assertThat(set.contains(p)).isTrue();
    }

    @Test
    void reloadedIsEqualButNotSame() {
        Product p = em.persistAndFlush(
                new Product("ID-002", "Pen", Money.eur("1.20"), Category.BOOKS));
        em.clear();

        Product reloaded = em.find(Product.class, p.getId());

        assertThat(reloaded.equals(p)).isTrue();
        assertThat(reloaded == p).isFalse();
    }
}
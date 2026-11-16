package edu.itc.shop.domain;

import org.junit.jupiter.api.BeforeEach;
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
class OrderCascadeTest {

    @Autowired TestEntityManager em;

    Customer customer;
    Product keyboard;
    Product mouse;

    @BeforeEach
    void setUp() {
        Address addr = new Address("St 1", "Phnom Penh", "12000");
        customer = em.persist(new Customer("Dara", "dara@itc.edu.kh", addr, addr));
        keyboard = em.persist(new Product("KB-001", "Keyboard", Money.eur("89.90"), Category.HARDWARE));
        mouse    = em.persist(new Product("MS-001", "Mouse", Money.eur("19.50"), Category.HARDWARE));
        em.flush();
    }

    private Long persistOrderWithTwoLines() {
        Order order = new Order(customer);
        order.addLine(keyboard, 2);
        order.addLine(mouse, 1);
        em.persist(order);
        em.flush();
        return order.getId();
    }

    private long countLines() {
        return em.getEntityManager()
                .createQuery("select count(l) from OrderLine l", Long.class)
                .getSingleResult();
    }

    @Test
    void persistOrder_insertsOnePlusN_noUpdate() {
        Long id = persistOrderWithTwoLines();
        em.clear();

        Order reloaded = em.find(Order.class, id);
        assertThat(reloaded.getLines()).hasSize(2);
        assertThat(reloaded.total().amount()).isEqualByComparingTo(new BigDecimal("199.30"));
    }

    @Test
    void removeLine_orphanRemoval_thenRemoveOrder_cascade() {
        Long id = persistOrderWithTwoLines();
        em.clear();

        Order order = em.find(Order.class, id);
        order.removeLine(order.getLines().get(0));
        em.flush();
        assertThat(countLines()).isEqualTo(1);

        em.remove(order);
        em.flush();
        assertThat(countLines()).isZero();
    }

    @Test
    void removeOrder_deletesAllLines() {
        Long id = persistOrderWithTwoLines();
        em.clear();

        em.remove(em.find(Order.class, id));
        em.flush();
        assertThat(countLines()).isZero();
    }
}
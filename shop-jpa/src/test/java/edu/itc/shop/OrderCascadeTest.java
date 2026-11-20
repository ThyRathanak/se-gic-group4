package edu.itc.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import edu.itc.shop.domain.Address;
import edu.itc.shop.domain.Category;
import edu.itc.shop.domain.Customer;
import edu.itc.shop.domain.Money;
import edu.itc.shop.domain.Order;
import edu.itc.shop.domain.OrderLine;
import edu.itc.shop.domain.OrderStatus;
import edu.itc.shop.domain.Product;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest(properties = {
    "spring.jpa.properties.hibernate.session_factory.statement_inspector=edu.itc.shop.SqlCapture"
})

public class OrderCascadeTest {

    @Autowired
    private EntityManager em;

    @Test
    void persistOrderShouldCascadeToOrderLines() {
        Customer customer = new Customer(
            "John Smith",
            "john@example.com",
            new Address("Street 1", "Phnom Penh", "12000"),
            new Address("Street 2", "Phnom Penh", "12000")
        );

        em.persist(customer);

        Product product1 = new Product(
            "P001",
            "Keyboard",
            Money.eur("50.00"),
            Category.HARDWARE,
            10
        );

        Product product2 = new Product(
            "P002",
            "Mouse",
            Money.eur("25.00"),
            Category.HARDWARE,
            20
        );

        em.persist(product1);
        em.persist(product2);

        em.flush();

        SqlCapture.clear();

        Order order = new Order(
            customer,
            OrderStatus.NEW,
            Instant.now()
        );

        order.addLine(product1, 2);
        order.addLine(product2, 3);

        em.persist(order);
        em.flush();

        List<String> sqlStatements = SqlCapture.getStatements();

        long orderInserts = sqlStatements.stream()
            .map(sql -> sql.toLowerCase(Locale.ROOT))
            .filter(sql -> sql.startsWith("insert into orders"))
            .count();

        long orderLineInserts = sqlStatements.stream()
            .map(sql -> sql.toLowerCase(Locale.ROOT))
            .filter(sql -> sql.startsWith("insert into order_line"))
            .count();

        long updates = sqlStatements.stream()
            .map(sql -> sql.toLowerCase(Locale.ROOT))
            .filter(sql -> sql.startsWith("update "))
            .count();

        assertEquals(1, orderInserts);
        assertEquals(2, orderLineInserts);
        assertEquals(0, updates);

        Long orderId = order.getId();

        em.clear();

        Order savedOrder = em.find(Order.class, orderId);

        assertEquals(2, savedOrder.getLines().size());

        assertEquals(
            Money.eur("175.00"),
            savedOrder.total()
        );
    }

    @Test
    void shouldRemoveOrphanLinesAndCascadeWhenOrderIsDeleted() {
        Customer customer = new Customer(
                "Jane Smith",
                "jane@example.com",
                new Address("Street 1", "Phnom Penh", "12000"),
                new Address("Street 2", "Phnom Penh", "12000")
        );

        Product product1 = new Product(
                "KEY-002", "Keyboard", Money.eur("50.00"), Category.HARDWARE, 10
        );

        Product product2 = new Product(
                "MOU-002", "Mouse", Money.eur("25.00"), Category.HARDWARE, 10
        );

        em.persist(customer);
        em.persist(product1);
        em.persist(product2);

        Order order = new Order(customer, OrderStatus.NEW, Instant.now());
        order.addLine(product1, 2);
        order.addLine(product2, 3);

        em.persist(order);
        em.flush();

        Long orderId = order.getId();

        em.clear();
        order = em.find(Order.class, orderId);

        long initialCount = em.createQuery(
                "select count(l) from OrderLine l", Long.class
        ).getSingleResult();

        assertEquals(2L, initialCount);

        SqlCapture.clear();

        OrderLine lineToRemove = order.getLines().get(0);
        order.removeLine(lineToRemove);

        em.flush();

        long countAfterOrphanRemoval = em.createQuery(
                "select count(l) from OrderLine l", Long.class
        ).getSingleResult();

        assertEquals(1L, countAfterOrphanRemoval);

        boolean orphanDeleteExecuted = SqlCapture.getStatements().stream()
                .map(sql -> sql.toLowerCase(Locale.ROOT))
                .anyMatch(sql -> sql.startsWith("delete from order_line"));

        assertTrue(orphanDeleteExecuted);

        SqlCapture.clear();

        em.remove(order);
        em.flush();

        long countAfterOrderRemoval = em.createQuery(
                "select count(l) from OrderLine l", Long.class
        ).getSingleResult();

        assertEquals(0L, countAfterOrderRemoval);
    }

}
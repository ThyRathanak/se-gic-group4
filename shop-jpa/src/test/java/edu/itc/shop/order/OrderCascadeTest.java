package edu.itc.shop.order;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import edu.itc.shop.customer.Customer;
import edu.itc.shop.domain.Address;
import edu.itc.shop.domain.Money;
import edu.itc.shop.product.Category;
import edu.itc.shop.product.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class OrderCascadeTest {

    private final Logger sqlLogger = (Logger) LoggerFactory.getLogger("org.hibernate.SQL");
    private final ListAppender<ILoggingEvent> sqlAppender = new ListAppender<>();

    @Autowired
    private TestEntityManager em;

    @BeforeEach
    void captureSql() {
        sqlAppender.start();
        sqlLogger.addAppender(sqlAppender);
    }

    @AfterEach
    void stopCapturingSql() {
        sqlLogger.detachAppender(sqlAppender);
        sqlAppender.stop();
    }

    @Test
    void cascadesOrderLinesAndRemovesOrphans() {
        Address address = new Address("1 Market Street", "Phnom Penh", "12000");
        Customer customer = new Customer("Dara", "dara@example.com", address, address);
        Product keyboard = new Product(
                "KB-001", "Keyboard", Money.eur("12.50"), Category.HARDWARE, 10);
        Product mouse = new Product(
                "MS-001", "Mouse", Money.eur("3.00"), Category.HARDWARE, 20);

        em.persist(customer);
        em.persist(keyboard);
        em.persist(mouse);
        em.flush();

        Order order = new Order(customer, OrderStatus.NEW, Instant.parse("2026-11-12T00:00:00Z"));
        order.addLine(keyboard, 2);
        order.addLine(mouse, 1);
        sqlAppender.list.clear();

        em.persist(order);
        em.flush();

        List<String> insertSql = loggedSql();
        assertThat(insertSql).filteredOn(sql -> sql.contains("insert into orders")).hasSize(1);
        assertThat(insertSql).filteredOn(sql -> sql.contains("insert into order_line")).hasSize(2);
        assertThat(insertSql).noneMatch(sql -> sql.startsWith("update "));

        Long orderId = order.getId();
        em.clear();

        Order reloaded = em.find(Order.class, orderId);
        assertThat(reloaded.getLines()).hasSize(2);
        assertThat(reloaded.total().amount()).isEqualByComparingTo("28.00");
        assertThat(reloaded.total().currency()).isEqualTo("EUR");

        List<OrderLine> lines = reloaded.getLines();
        sqlAppender.list.clear();
        reloaded.removeLine(lines.get(0));
        em.flush();

        assertThat(countLinesForOrder(orderId)).isEqualTo(1);
        assertThat(loggedSql()).anyMatch(sql -> sql.startsWith("delete from order_line"));

        sqlAppender.list.clear();
        em.remove(reloaded);
        em.flush();

        assertThat(countLinesForOrder(orderId)).isZero();
        assertThat(loggedSql()).anyMatch(sql -> sql.startsWith("delete from order_line"));
    }

    private long countLinesForOrder(Long orderId) {
        return em.getEntityManager()
                .createQuery("select count(line) from OrderLine line where line.order.id = :orderId", Long.class)
                .setParameter("orderId", orderId)
                .getSingleResult();
    }

    private List<String> loggedSql() {
        return sqlAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .map(sql -> sql.toLowerCase(Locale.ROOT).strip())
                .toList();
    }
}

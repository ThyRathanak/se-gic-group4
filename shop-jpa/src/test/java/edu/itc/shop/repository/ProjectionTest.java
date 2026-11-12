package edu.itc.shop.repository;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import edu.itc.shop.customer.Customer;
import edu.itc.shop.domain.Address;
import edu.itc.shop.domain.Money;
import edu.itc.shop.order.Order;
import edu.itc.shop.order.OrderStatus;
import edu.itc.shop.product.Category;
import edu.itc.shop.product.Product;
import edu.itc.shop.web.dto.OrderSummary;
import edu.itc.shop.web.dto.ProductView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class ProjectionTest {

    private final Logger sqlLogger = (Logger) LoggerFactory.getLogger("org.hibernate.SQL");
    private final ListAppender<ILoggingEvent> sqlAppender = new ListAppender<>();

    @Autowired
    private TestEntityManager em;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

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
    void pagesInterfaceAndRecordProjections() {
        Address address = new Address("1 Market Street", "Phnom Penh", "12000");
        Customer customer = new Customer("Dara", "projection@example.com", address, address);
        Product product = new Product(
                "PR-001", "Projection Product", Money.eur("15.50"), Category.HARDWARE, 7);
        Order order = new Order(customer, OrderStatus.NEW, Instant.parse("2026-11-12T00:00:00Z"));

        em.persist(customer);
        em.persist(product);
        em.persist(order);
        em.flush();
        sqlAppender.list.clear();

        List<OrderSummary> summaries = orderRepository
                .findByStatus(OrderStatus.NEW, PageRequest.of(0, 5))
                .getContent();
        assertThat(summaries).hasSize(1);
        assertThat(summaries.get(0).getId()).isEqualTo(order.getId());
        assertThat(summaries.get(0).getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(summaries.get(0).getCustomer().getName()).isEqualTo("Dara");

        String orderProjectionSql = loggedSql().stream()
                .filter(sql -> sql.startsWith("select") && sql.contains("from orders"))
                .findFirst()
                .orElseThrow();
        assertThat(orderProjectionSql)
                .startsWith("select o1_0.id,o1_0.status,o1_0.placed_at,c1_0.name from");

        sqlAppender.list.clear();
        List<ProductView> products = productRepository
                .findByCategory(Category.HARDWARE, PageRequest.of(0, 5))
                .getContent();

        String productProjectionSql = loggedSql().stream()
                .filter(sql -> sql.startsWith("select") && sql.contains("from product"))
                .findFirst()
                .orElseThrow();
        assertThat(productProjectionSql)
                .startsWith("select p1_0.id,p1_0.sku,p1_0.name,p1_0.amount,p1_0.stock from");

        assertThat(products).hasSize(1);
        assertThat(products.get(0).id()).isEqualTo(product.getId());
        assertThat(products.get(0).sku()).isEqualTo("PR-001");
        assertThat(products.get(0).name()).isEqualTo("Projection Product");
        assertThat(products.get(0).amount()).isEqualByComparingTo("15.50");
        assertThat(products.get(0).stock()).isEqualTo(7);
    }

    private List<String> loggedSql() {
        return sqlAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .map(sql -> sql.toLowerCase(Locale.ROOT).strip())
                .toList();
    }
}

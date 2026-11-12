package edu.itc.shop.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import edu.itc.shop.customer.Customer;
import edu.itc.shop.domain.Address;
import edu.itc.shop.order.Order;
import edu.itc.shop.order.OrderStatus;
import edu.itc.shop.domain.Money;
import edu.itc.shop.product.Category;
import edu.itc.shop.product.Product;
import edu.itc.shop.repository.CustomerRepository;
import edu.itc.shop.repository.OrderRepository;
import edu.itc.shop.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(OrderServiceTest.ReadOnlyTrapConfiguration.class)
class OrderServiceTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ReadOnlyMutationTrap readOnlyMutationTrap;

    @Autowired
    private jakarta.persistence.EntityManagerFactory entityManagerFactory;

    @Test
    void marksManagedOrderPaidThroughDirtyChecking() {
        Address address = new Address("1 Market Street", "Phnom Penh", "12000");
        Customer customer = customerRepository.save(
                new Customer("Dirty Check Customer", "dirty-check@example.com", address, address));
        Order order = orderRepository.save(
                new Order(customer, OrderStatus.NEW, Instant.parse("2026-11-12T00:00:00Z")));

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();

        orderService.markPaid(order.getId());

        assertThat(statistics.getEntityUpdateCount()).isEqualTo(1);
        assertThat(orderRepository.findById(order.getId()))
                .get()
                .extracting(Order::getStatus)
                .isEqualTo(OrderStatus.PAID);
    }

    @Test
    void detachedProductChangesNeedSaveToMerge() {
        Product original = productRepository.save(new Product(
                "DET-001", "Detached Product", Money.eur("10.00"), Category.HARDWARE, 7));
        Long productId = original.getId();

        Product detached = productRepository.findById(productId).orElseThrow();
        detached.setStock(99);

        Product beforeMerge = productRepository.findById(productId).orElseThrow();
        assertThat(beforeMerge.getStock()).isEqualTo(7);

        Logger sqlLogger = (Logger) LoggerFactory.getLogger("org.hibernate.SQL");
        ListAppender<ILoggingEvent> sqlAppender = new ListAppender<>();
        sqlAppender.start();
        sqlLogger.addAppender(sqlAppender);
        try {
            productRepository.save(detached);
        } finally {
            sqlLogger.detachAppender(sqlAppender);
            sqlAppender.stop();
        }

        List<String> sql = sqlAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .map(statement -> statement.toLowerCase(Locale.ROOT).strip())
                .toList();
        assertThat(sql).filteredOn(statement -> statement.startsWith("select")
                && statement.contains("from product")).hasSize(1);
        assertThat(sql).filteredOn(statement -> statement.startsWith("update product")).hasSize(1);

        assertThat(productRepository.findById(productId).orElseThrow().getStock()).isEqualTo(99);
    }

    @Test
    void readOnlyViewReturnsOrderDto() {
        Address address = new Address("2 Market Street", "Phnom Penh", "12000");
        Customer customer = customerRepository.save(
                new Customer("View Customer", "view@example.com", address, address));
        Order order = orderRepository.save(new Order(
                customer, OrderStatus.NEW, Instant.parse("2026-11-12T00:00:00Z")));

        assertThat(orderService.view(order.getId()).status()).isEqualTo(OrderStatus.NEW);
    }

    @Test
    void readOnlyMutationTrapDoesNotFlush() {
        Address address = new Address("3 Market Street", "Phnom Penh", "12000");
        Customer customer = customerRepository.save(
                new Customer("Read Only Customer", "readonly@example.com", address, address));
        Order order = orderRepository.save(new Order(
                customer, OrderStatus.NEW, Instant.parse("2026-11-12T00:00:00Z")));

        Statistics statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();

        assertThat(readOnlyMutationTrap.mutateStatus(order.getId())).isEqualTo(FlushMode.MANUAL);
        assertThat(orderRepository.findById(order.getId()).orElseThrow().getStatus())
                .isEqualTo(OrderStatus.NEW);
        assertThat(statistics.getEntityUpdateCount()).isZero();
    }

    @TestConfiguration(proxyBeanMethods = false)
    public static class ReadOnlyTrapConfiguration {

        @Bean
        ReadOnlyMutationTrap readOnlyMutationTrap(OrderRepository orderRepository, EntityManager entityManager) {
            return new ReadOnlyMutationTrap(orderRepository, entityManager);
        }
    }

    public static class ReadOnlyMutationTrap {

        private final OrderRepository orderRepository;
        private final EntityManager entityManager;

        public ReadOnlyMutationTrap(OrderRepository orderRepository, EntityManager entityManager) {
            this.orderRepository = orderRepository;
            this.entityManager = entityManager;
        }

        @org.springframework.transaction.annotation.Transactional(readOnly = true)
        public FlushMode mutateStatus(Long id) {
            Order order = orderRepository.findById(id).orElseThrow();
            order.setStatus(OrderStatus.PAID); // Deliberately wrong: read-only method mutates a managed entity.
            return entityManager.unwrap(Session.class).getHibernateFlushMode();
        }
    }
}

package edu.itc.shop.web;

import edu.itc.shop.customer.Customer;
import edu.itc.shop.domain.Address;
import edu.itc.shop.domain.Money;
import edu.itc.shop.order.Order;
import edu.itc.shop.order.OrderStatus;
import edu.itc.shop.product.Category;
import edu.itc.shop.product.Product;
import edu.itc.shop.repository.CustomerRepository;
import edu.itc.shop.repository.OrderRepository;
import edu.itc.shop.repository.ProductRepository;
import edu.itc.shop.web.dto.OrderDto;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class OpenInViewTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerOrderController controller;

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void detachedEntityCannotLoadLinesButControllerReturnsServiceMappedDto() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("2");
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where success = true", Integer.class))
                .isEqualTo(2);

        Address address = new Address("8 Lazy Street", "Phnom Penh", "12000");
        Customer customer = customerRepository.save(new Customer(
                "Open In View Customer", "open-in-view@example.com", address, address));
        Product product = productRepository.save(new Product(
                "OIV-001", "Lazy Loading Book", Money.eur("12.00"), Category.BOOKS, 5));
        Order order = new Order(customer, OrderStatus.PAID, Instant.parse("2026-11-12T00:00:00Z"));
        order.addLine(product, 2);
        orderRepository.save(order);

        // Repository calls end their transactions. With open-in-view=false this entity is detached.
        Order detached = orderRepository.findById(order.getId()).orElseThrow();
        assertThatThrownBy(() -> detached.getLines().size())
                .isInstanceOf(LazyInitializationException.class);

        // The production controller delegates to the transactional service, which maps lines
        // and products to DTOs before its transaction closes.
        var response = controller.ordersForCustomer(customer.getEmail());
        assertThat(response).hasSize(1);
        OrderDto dto = response.getFirst();
        assertThat(dto.lines()).hasSize(1);
        assertThat(dto.lines().getFirst().name()).isEqualTo("Lazy Loading Book");
    }
}

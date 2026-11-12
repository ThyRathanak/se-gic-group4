package edu.itc.shop.repository;

import edu.itc.shop.customer.Customer;
import edu.itc.shop.domain.Address;
import edu.itc.shop.domain.Money;
import edu.itc.shop.order.Order;
import edu.itc.shop.order.OrderStatus;
import edu.itc.shop.product.Category;
import edu.itc.shop.product.Product;
import edu.itc.shop.web.dto.OrderSummary;
import edu.itc.shop.web.dto.ProductView;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
class OrderRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    private Customer customer;
    private Product lowStockProduct;
    private Product highStockProduct;
    private Order olderPaidOrder;
    private Order newerPaidOrder;

    @BeforeEach
    void setUp() {
        Address address = new Address("1 Market Street", "Phnom Penh", "12000");
        customer = new Customer("Dara", "repo@example.com", address, address);
        lowStockProduct = new Product(
                "RP-LOW", "Low stock keyboard", Money.eur("20.00"), Category.HARDWARE, 2);
        highStockProduct = new Product(
                "RP-HIGH", "High stock mouse", Money.eur("5.00"), Category.HARDWARE, 20);

        em.persist(customer);
        em.persist(lowStockProduct);
        em.persist(highStockProduct);
        em.flush();

        olderPaidOrder = createOrder(OrderStatus.PAID, "2026-11-10T00:00:00Z");
        newerPaidOrder = createOrder(OrderStatus.PAID, "2026-11-11T00:00:00Z");
        Order newOrder = createOrder(OrderStatus.NEW, "2026-11-12T00:00:00Z");

        orderRepository.saveAll(List.of(olderPaidOrder, newerPaidOrder, newOrder));
        em.flush();
    }

    @Test
    void findsOrdersByCustomerEmailAndStatusNewestFirst() {
        List<Order> orders = orderRepository.findByCustomerEmailAndStatusOrderByPlacedAtDesc(
                customer.getEmail(), OrderStatus.PAID);

        assertThat(orders).containsExactly(newerPaidOrder, olderPaidOrder);
    }

    @Test
    void fetchJoinInitializesLinesButPlainFindDoesNot() {
        Order fetched = orderRepository.findWithLinesByCustomerEmail(customer.getEmail()).stream()
                .filter(order -> order.getId().equals(newerPaidOrder.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(isLinesLoaded(fetched)).isTrue();
        assertThat(fetched.getLines()).hasSize(2);
        assertThat(Hibernate.isInitialized(fetched.getLines().get(0).getProduct())).isTrue();

        Long orderId = fetched.getId();
        em.clear();
        Order plain = orderRepository.findById(orderId).orElseThrow();

        assertThat(isLinesLoaded(plain)).isFalse();
    }

    @Test
    void executesNativeLowStockQuery() {
        assertThat(productRepository.findLowStock(5)).containsExactly(lowStockProduct);
    }

    @Test
    void returnsOrderAndProductProjections() {
        Page<OrderSummary> orderPage = orderRepository.findByStatus(
                OrderStatus.PAID,
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "placedAt"))
        );

        assertThat(orderPage.getContent()).hasSize(2);
        assertThat(orderPage.getContent().get(0).getCustomer().getName()).isEqualTo("Dara");
        assertThat(orderPage.getContent().get(0).getPlacedAt())
                .isEqualTo(Instant.parse("2026-11-11T00:00:00Z"));

        Page<ProductView> productPage = productRepository.findByCategory(
                Category.HARDWARE,
                PageRequest.of(0, 5)
        );
        assertThat(productPage.getContent()).hasSize(2);
        assertThat(productPage.getContent())
                .extracting(ProductView::amount)
                .containsExactlyInAnyOrder(Money.eur("20.00").amount(), Money.eur("5.00").amount());
    }

    private Order createOrder(OrderStatus status, String placedAt) {
        Order order = new Order(customer, status, Instant.parse(placedAt));
        order.addLine(lowStockProduct, 1);
        order.addLine(highStockProduct, 2);
        return order;
    }

    private boolean isLinesLoaded(Order order) {
        return em.getEntityManager()
                .getEntityManagerFactory()
                .getPersistenceUnitUtil()
                .isLoaded(order, "lines");
    }
}

package edu.itc.shop.support;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import edu.itc.shop.domain.Address;
import edu.itc.shop.domain.Category;
import edu.itc.shop.domain.Customer;
import edu.itc.shop.domain.Money;
import edu.itc.shop.domain.Order;
import edu.itc.shop.domain.OrderStatus;
import edu.itc.shop.domain.Product;
import edu.itc.shop.repository.CustomerRepository;
import edu.itc.shop.repository.OrderRepository;
import edu.itc.shop.repository.ProductRepository;

@Component
public class TestData {

    private static final String TEST_EMAIL = "nplusone@test.com";
    private static final String PRODUCT_TEST_EMAIL =
            "nplusone-products@test.com";

    private final CustomerRepository customers;
    private final ProductRepository products;
    private final OrderRepository orders;

    public TestData(
            CustomerRepository customers,
            ProductRepository products,
            OrderRepository orders) {
        this.customers = customers;
        this.products = products;
        this.orders = orders;
    }

    @Transactional
    public void paidOrders(int orderCount, int linesPerOrder) {

        Customer customer = customers.findByEmail(TEST_EMAIL)
                .orElseGet(() -> customers.save(
                        new Customer(
                                "N+1 Test Customer",
                                TEST_EMAIL,
                                new Address("Test Street", "Test City", "10000"),
                                new Address("Test Street", "Test City", "10000")
                        )
                ));

        List<Product> productList = new ArrayList<>();

        for (int i = 0; i < linesPerOrder; i++) {
            String sku = "NPLUS-" + i;
            String productName = "N+1 Product " + i;

            Product product = products.findAll().stream()
                    .filter(p -> p.getSku().equals(sku))
                    .findFirst()
                    .orElseGet(() -> products.save(
                            new Product(
                                    sku,
                                    productName,
                                    Money.eur("10.00"),
                                    Category.BOOKS,
                                    100
                            )
                    ));

            productList.add(product);
        }

        List<Order> existingOrders =
                orders.findByCustomerEmailAndStatusOrderByPlacedAtDesc(
                        TEST_EMAIL,
                        OrderStatus.PAID
                );

        if (existingOrders.size() > orderCount) {
            throw new IllegalStateException(
                    "More test orders exist than expected."
            );
        }

        for (int i = existingOrders.size(); i < orderCount; i++) {

            Order order = new Order(
                    customer,
                    OrderStatus.PAID,
                    Instant.now().minusSeconds(i)
            );

            for (int j = 0; j < linesPerOrder; j++) {
                order.addLine(productList.get(j), 1);
            }

            orders.save(order);
        }
    }

    @Transactional
    public void paidOrdersWithDistinctProducts(
            int orderCount,
            int linesPerOrder) {

        Customer customer = customers.findByEmail(PRODUCT_TEST_EMAIL)
                .orElseGet(() -> customers.save(
                        new Customer(
                                "N+1 Product Test Customer",
                                PRODUCT_TEST_EMAIL,
                                new Address("Test Street", "Test City", "10000"),
                                new Address("Test Street", "Test City", "10000")
                        )
                ));

        List<Order> existingOrders =
                orders.findByCustomerEmailAndStatusOrderByPlacedAtDesc(
                        PRODUCT_TEST_EMAIL,
                        OrderStatus.PAID
                );

        if (existingOrders.size() > orderCount) {
            throw new IllegalStateException(
                    "More product test orders exist than expected."
            );
        }

        for (int i = existingOrders.size(); i < orderCount; i++) {

            Order order = new Order(
                    customer,
                    OrderStatus.PAID,
                    Instant.now().minusSeconds(i)
            );

            for (int j = 0; j < linesPerOrder; j++) {

                String sku = "NPLUS-DISTINCT-" + i + "-" + j;
                String productName = "Distinct Product " + i + "-" + j;

                Product product = products.findAll().stream()
                        .filter(p -> p.getSku().equals(sku))
                        .findFirst()
                        .orElseGet(() -> products.save(
                                new Product(
                                        sku,
                                        productName,
                                        Money.eur("10.00"),
                                        Category.BOOKS,
                                        100
                                )
                        ));

                order.addLine(product, 1);
            }

            orders.save(order);
        }
    }
}
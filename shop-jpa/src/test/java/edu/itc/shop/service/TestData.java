package edu.itc.shop.service;

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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TestData {

    private final CustomerRepository customers;
    private final ProductRepository products;
    private final OrderRepository orders;

    public TestData(
            CustomerRepository customers,
            ProductRepository products,
            OrderRepository orders
    ) {
        this.customers = customers;
        this.products = products;
        this.orders = orders;
    }

    public void seedPaidOrders(int orderCount, int linesPerOrder) {
        Address address = new Address("50 Test Street", "Phnom Penh", "12000");
        Customer customer = customers.save(
                new Customer("N+1 Test Customer", "n-plus-one@example.com", address, address));

        int productCount = orderCount * linesPerOrder;
        List<Product> productsForOrders = new ArrayList<>(productCount);
        for (int i = 0; i < productCount; i++) {
            productsForOrders.add(new Product(
                    "N1-%04d".formatted(i),
                    "N+1 Product %04d".formatted(i),
                    Money.eur("10.00"),
                    Category.HARDWARE,
                    100));
        }
        productsForOrders = products.saveAll(productsForOrders);

        List<Order> paidOrders = new ArrayList<>();
        Instant now = Instant.parse("2026-11-12T00:00:00Z");
        for (int i = 0; i < orderCount; i++) {
            Order order = new Order(customer, OrderStatus.PAID, now.minusSeconds(i));
            for (int line = 0; line < linesPerOrder; line++) {
                order.addLine(productsForOrders.get(i * linesPerOrder + line), 1);
            }
            paidOrders.add(order);
        }

        orders.saveAll(paidOrders);
    }
}

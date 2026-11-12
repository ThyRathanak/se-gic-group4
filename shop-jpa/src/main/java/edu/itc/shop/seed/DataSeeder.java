package edu.itc.shop.seed;

import edu.itc.shop.customer.Customer;
import edu.itc.shop.domain.Address;
import edu.itc.shop.domain.Money;
import edu.itc.shop.order.Order;
import edu.itc.shop.order.OrderStatus;
import edu.itc.shop.product.Category;
import edu.itc.shop.product.Product;
import edu.itc.shop.product.Tag;
import edu.itc.shop.repository.CustomerRepository;
import edu.itc.shop.repository.OrderRepository;
import edu.itc.shop.repository.ProductRepository;
import edu.itc.shop.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@Profile("dev")
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final TagRepository tagRepository;
    private final OrderRepository orderRepository;

    public DataSeeder(
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            TagRepository tagRepository,
            OrderRepository orderRepository
    ) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.tagRepository = tagRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (customerRepository.count() > 0) {
            log.info("Skipping dev seed because customers already exist");
            return;
        }

        List<Customer> customers = createCustomers();
        customerRepository.saveAll(customers);

        List<Tag> tags = tagRepository.saveAll(List.of(
                new Tag("popular"),
                new Tag("office"),
                new Tag("gift"),
                new Tag("clearance")
        ));

        List<Product> products = createProducts(tags);
        productRepository.saveAll(products);

        List<Order> orders = createOrders(customers, products);
        orderRepository.saveAll(orders);

        log.info("Seeded {} customers, {} products, {} tags, and {} orders",
                customers.size(), products.size(), tags.size(), orders.size());
    }

    private List<Customer> createCustomers() {
        Address[] addresses = {
                new Address("12 Street 240", "Phnom Penh", "12000"),
                new Address("8 Riverside Road", "Siem Reap", "17000"),
                new Address("31 Market Lane", "Battambang", "02000")
        };

        return List.of(
                new Customer("Dara Sok", "dara@example.com", addresses[0], addresses[0]),
                new Customer("Sophea Chan", "sophea@example.com", addresses[1], addresses[1]),
                new Customer("Vanna Kim", "vanna@example.com", addresses[2], addresses[2])
        );
    }

    private List<Product> createProducts(List<Tag> tags) {
        List<Product> products = new ArrayList<>();
        Category[] categories = Category.values();

        for (int i = 0; i < 12; i++) {
            Product product = new Product(
                    String.format(Locale.ROOT, "DEV-%03d", i + 1),
                    "Dev Sample Product " + (i + 1),
                    Money.eur(String.format(Locale.ROOT, "%d.%02d", 5 + i * 3, (i * 17) % 100)),
                    categories[i % categories.length],
                    5 + (i * 4) % 50
            );
            product.tag(tags.get(i % tags.size()));
            if (i % 2 == 0) {
                product.tag(tags.get((i + 1) % tags.size()));
            }
            products.add(product);
        }

        return products;
    }

    private List<Order> createOrders(List<Customer> customers, List<Product> products) {
        List<Order> orders = new ArrayList<>();
        OrderStatus[] statuses = OrderStatus.values();
        Instant now = Instant.now();

        for (int i = 0; i < 30; i++) {
            Order order = new Order(
                    customers.get(i % customers.size()),
                    statuses[i % statuses.length],
                    now.minus(i, ChronoUnit.DAYS)
            );
            int lineCount = 2 + i % 3;
            for (int line = 0; line < lineCount; line++) {
                Product product = products.get((i * 3 + line * 2) % products.size());
                order.addLine(product, 1 + (i + line) % 4);
            }
            orders.add(order);
        }

        return orders;
    }
}

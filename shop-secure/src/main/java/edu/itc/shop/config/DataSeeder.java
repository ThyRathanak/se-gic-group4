package edu.itc.shop.config;

import edu.itc.shop.domain.Product;
import edu.itc.shop.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedProducts(ProductRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(new Product("USB-C cable", new BigDecimal("7.90"), 25));
                repo.save(new Product("Keyboard", new BigDecimal("49.90"), 10));
                repo.save(new Product("Monitor", new BigDecimal("199.00"), 4));
            }
        };
    }
}
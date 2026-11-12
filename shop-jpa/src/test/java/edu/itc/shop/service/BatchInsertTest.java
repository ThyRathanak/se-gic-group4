package edu.itc.shop.service;

import edu.itc.shop.domain.Money;
import edu.itc.shop.product.Category;
import edu.itc.shop.product.Product;
import edu.itc.shop.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BatchInsertTest {

    @Autowired
    private ProductRepository products;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @Transactional
    void batchesOneThousandProductInserts() {
        Statistics stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        List<Product> batch = new ArrayList<>(1_000);
        for (int i = 0; i < 1_000; i++) {
            batch.add(new Product(
                    "BATCH-%04d".formatted(i),
                    "Batch Product %04d".formatted(i),
                    Money.eur("9.99"),
                    Category.HARDWARE,
                    100));
        }

        products.saveAll(batch);
        entityManager.flush();

        assertThat(stats.getEntityInsertCount()).isEqualTo(1_000);
        assertThat(stats.getPrepareStatementCount())
                .as("sequence allocation and batched inserts should use far fewer than 1,000 prepared statements")
                .isLessThan(100);
    }
}

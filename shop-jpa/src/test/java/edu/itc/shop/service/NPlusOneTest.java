package edu.itc.shop.service;

import edu.itc.shop.repository.CustomerRepository;
import edu.itc.shop.repository.OrderRepository;
import edu.itc.shop.repository.ProductRepository;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Tag("demo")
class NPlusOneTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReportService reportService;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void demonstratesNPlusOneAndComparesThreeFixes() {
        new TestData(customerRepository, productRepository, orderRepository)
                .seedPaidOrders(50, 3);
        QueryCounter queries = new QueryCounter(entityManagerFactory);

        queries.reset();
        Map<Long, List<String>> naive = reportService.linesPerPaidOrder();
        assertThat(naive).hasSize(50).allSatisfy((orderId, names) -> assertThat(names).hasSize(3));
        assertThat(queries.count()).isEqualTo(10);

        queries.reset();
        Map<Long, List<String>> fetchJoin = reportService.linesPerPaidOrderWithFetchJoin();
        assertThat(fetchJoin).hasSize(50).allSatisfy((orderId, names) -> assertThat(names).hasSize(3));
        assertThat(queries.count()).isEqualTo(6);

        queries.reset();
        Map<Long, List<String>> entityGraph = reportService.linesPerPaidOrderWithEntityGraph();
        assertThat(entityGraph).hasSize(50).allSatisfy((orderId, names) -> assertThat(names).hasSize(3));
        assertThat(queries.count()).isEqualTo(1);

        queries.reset();
        Map<Long, List<String>> batched = reportService.linesPerPaidOrderBatched();
        assertThat(batched).hasSize(50).allSatisfy((orderId, names) -> assertThat(names).hasSize(3));
        assertThat(queries.count()).isEqualTo(10);

        printMedian("naive + batch fetching", reportService::linesPerPaidOrder, 10, queries);
        printMedian("fetch join lines", reportService::linesPerPaidOrderWithFetchJoin, 6, queries);
        printMedian("entity graph lines + products", reportService::linesPerPaidOrderWithEntityGraph, 1, queries);
        printMedian("batch fetching", reportService::linesPerPaidOrderBatched, 10, queries);
    }

    private void printMedian(String variant, Supplier<?> report, long expectedStatements, QueryCounter queries) {
        report.get(); // warm-up
        List<Long> elapsedNanos = new ArrayList<>(20);
        long statements = 0;
        for (int i = 0; i < 20; i++) {
            queries.reset();
            long start = System.nanoTime();
            report.get();
            elapsedNanos.add(System.nanoTime() - start);
            statements = queries.count();
        }
        assertThat(statements).isEqualTo(expectedStatements);
        Collections.sort(elapsedNanos);
        double medianMillis = (elapsedNanos.get(9) + elapsedNanos.get(10)) / 2_000_000.0;
        System.out.printf("N+1 median: %s | %d statements | %.3f ms%n", variant, statements, medianMillis);
    }
}

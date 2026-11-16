package edu.itc.shop;

import edu.itc.shop.service.ReportService;
import edu.itc.shop.support.QueryCounter;
import edu.itc.shop.support.TestData;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ReportBenchmarkTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private TestData testData;

    @Autowired
    private QueryCounter queryCounter;

    @Test
    @Tag("benchmark")
    void benchmarkReportVariants() {
        testData.paidOrders(50, 3);

        benchmark("Naive", reportService::linesPerPaidOrder, 51);
        benchmark("JOIN FETCH", reportService::linesPerPaidOrderJoinFetch, 1);
        benchmark("EntityGraph", reportService::linesPerPaidOrderEntityGraph, 1);
    }

    private void benchmark(
            String name,
            Supplier<Map<Long, Integer>> operation,
            long expectedStatements
    ) {
        // Warm-up run (not included in the measurements)
        operation.get();

        long[] times = new long[20];

        for (int i = 0; i < times.length; i++) {
            queryCounter.reset();

            long start = System.nanoTime();
            Map<Long, Integer> result = operation.get();
            long end = System.nanoTime();

            times[i] = end - start;

            assertEquals(50, result.size());
            assertEquals(expectedStatements, queryCounter.count());
        }

        Arrays.sort(times);

        double medianMs =
                (times[9] + times[10]) / 2.0 / 1_000_000.0;

        System.out.printf(
                Locale.ROOT,
                "%s | Statements: %d | Median: %.3f ms%n",
                name,
                expectedStatements,
                medianMs
        );
    }
}
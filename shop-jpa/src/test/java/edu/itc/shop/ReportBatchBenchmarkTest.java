package edu.itc.shop;

import edu.itc.shop.service.ReportService;
import edu.itc.shop.support.QueryCounter;
import edu.itc.shop.support.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ReportBatchBenchmarkTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private TestData testData;

    @Autowired
    private QueryCounter queryCounter;

    @Test
    void benchmarkBatchFetching() {
        testData.paidOrders(50, 3);

        Supplier<Map<Long, Integer>> operation =
                reportService::linesPerPaidOrderBatch;

        operation.get(); // Warm-up

        long[] times = new long[20];

        for (int i = 0; i < times.length; i++) {
            queryCounter.reset();

            long start = System.nanoTime();
            Map<Long, Integer> result = operation.get();
            long end = System.nanoTime();

            times[i] = end - start;

            assertEquals(50, result.size());
            assertEquals(5, queryCounter.count());
        }

        Arrays.sort(times);

        double medianMs =
                (times[9] + times[10]) / 2.0 / 1_000_000.0;

        System.out.printf(
                Locale.ROOT,
                "Batch | Statements: 5 | Median: %.3f ms%n",
                medianMs
        );
    }
}
package edu.itc.shop;

import edu.itc.shop.service.ReportService;
import edu.itc.shop.support.QueryCounter;
import edu.itc.shop.support.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ProductFetchTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private TestData testData;

    @Autowired
    private QueryCounter queryCounter;

    @BeforeEach
    void setUp() {
        testData.paidOrdersWithDistinctProducts(50, 3);
    }

    @Test
    void naiveProductFetching() {
        queryCounter.reset();

        Map<Long, List<String>> result =
                reportService.productNamesPerPaidOrder();

        assertEquals(50, result.size());
        assertEquals(3, result.values().iterator().next().size());

        System.out.println(
                "Naive product fetching: " + queryCounter.count()
                        + " SQL statements"
        );
    }

    @Test
    void joinFetchProductFetching() {
        queryCounter.reset();

        Map<Long, List<String>> result =
                reportService.productNamesPerPaidOrderJoinFetch();

        assertEquals(50, result.size());
        assertEquals(3, result.values().iterator().next().size());

        System.out.println(
                "JOIN FETCH product fetching: " + queryCounter.count()
                        + " SQL statements"
        );
    }

    @Test
    void entityGraphProductFetching() {
        queryCounter.reset();

        Map<Long, List<String>> result =
                reportService.productNamesPerPaidOrderEntityGraph();

        assertEquals(50, result.size());
        assertEquals(3, result.values().iterator().next().size());

        System.out.println(
                "EntityGraph product fetching: " + queryCounter.count()
                        + " SQL statements"
        );
    }
}
package edu.itc.shop.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import edu.itc.shop.repository.ProductRepository;

@SpringBootTest
class ProductServiceTest {

    @Autowired
    ProductService service;

    @Autowired
    ProductRepository repository;

    @Test
    void restockRollsBackWhenSourceHasTooLittleStock() {
        int before1 = repository.findById(1)
                .orElseThrow().stock();

        int before2 = repository.findById(2)
                .orElseThrow().stock();

        assertThrows(IllegalStateException.class,
                () -> service.restock(1, 2, 1_000));

        assertEquals(before1,
                repository.findById(1).orElseThrow().stock());

        assertEquals(before2,
                repository.findById(2).orElseThrow().stock());
    }
}
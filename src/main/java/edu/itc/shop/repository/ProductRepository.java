package edu.itc.shop.repository;

import edu.itc.shop.domain.Product;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository {
    private final JdbcClient jdbc;

    public ProductRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Product> findAll() {
        return jdbc.sql("SELECT id, name, price, stock FROM product ORDER BY id")
                .query(Product.class).list();
    }

    public Optional<Product> findById(long id) {
        return jdbc.sql("SELECT id, name, price, stock FROM product WHERE id = :id")
                .param("id", id)
                .query(Product.class)
                .optional();
    }

    public int updateStock(long id, int delta) {
        return jdbc.sql("UPDATE product SET stock = stock + :delta WHERE id = :id AND stock + :delta >= 0")
                .param("delta", delta)
                .param("id", id)
                .update();
    }
}
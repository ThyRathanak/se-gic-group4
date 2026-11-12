package edu.itc.shop.repository;

import edu.itc.shop.domain.Product;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {
    private final JdbcClient jdbc;

    public ProductRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Product> findAll() {
        return jdbc.sql(
                "SELECT id, name, price, stock FROM product ORDER BY id")
                .query(Product.class)
                .list();
    }

    public List<Product> search(String q) {
        if (q == null || q.isBlank()) {
            return findAll();
        }

        return jdbc.sql(
                "SELECT id, name, price, stock FROM product " +
                        "WHERE LOWER(name) LIKE LOWER(:q) ORDER BY id")
                .param("q", "%" + q.trim() + "%")
                .query(Product.class)
                .list();
    }

    public Optional<Product> findById(long id) {
        return jdbc.sql(
                "SELECT id, name, price, stock FROM product WHERE id = :id")
                .param("id", id)
                .query(Product.class)
                .optional();
    }

    public Product insert(String name, BigDecimal price, int stock) {
        String sql = """
            INSERT INTO product (name, price, stock)
            VALUES (:name, :price, :stock)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.sql(sql)
                .param("name", name)
                .param("price", price)
                .param("stock", stock)
                .update(keyHolder, "id");

        Number key = keyHolder.getKey();

        if (key == null) {
            throw new IllegalStateException(
                    "Failed to retrieve generated product ID");
        }

        return findById(key.longValue())
                .orElseThrow(() -> new IllegalStateException(
                        "Inserted product could not be found"));
    }
   
    public int updateStock(long id, int delta) {
        return jdbc.sql(
                "UPDATE product SET stock = stock + :delta " +
                        "WHERE id = :id AND stock + :delta >= 0")
                .param("delta", delta)
                .param("id", id)
                .update();
    }

    public int deleteById(long id) {
        return jdbc.sql("DELETE FROM product WHERE id = :id")
                .param("id", id)
                .update();
    }
}

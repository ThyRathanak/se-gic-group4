package edu.itc.shop.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import edu.itc.shop.domain.Product;

@Repository
public class RawJdbcProductRepository {

    private final DataSource dataSource;

    public RawJdbcProductRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Product> findAll() {
        String sql =
                "SELECT id, name, price, stock FROM product ORDER BY id";

        List<Product> products = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                products.add(new Product(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getBigDecimal("price"),
                        rs.getInt("stock")));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Could not load products", e);
        }

        return products;
    }
}
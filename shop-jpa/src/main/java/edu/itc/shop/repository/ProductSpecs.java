package edu.itc.shop.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import edu.itc.shop.domain.Category;
import edu.itc.shop.domain.Money;
import edu.itc.shop.domain.Product;

public final class ProductSpecs {

    private ProductSpecs() {
    }

    public static Specification<Product> nameContains(String q) {
        String pattern = "%" + q.toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.<String>get("name")),
                        pattern
                );
    }

    public static Specification<Product> inCategory(Category category) {
        return (root, query, cb) ->
                cb.equal(root.get("category"), category);
    }

    public static Specification<Product> cheaperThan(BigDecimal max) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(
                        root.<Money>get("price").<BigDecimal>get("amount"),
                        max
                );
    }

    public static Specification<Product> inStock() {
        return (root, query, cb) ->
                cb.greaterThan(root.<Integer>get("stock"), 0);
    }

    public static Specification<Product> filter(
            String q,
            Category category,
            BigDecimal maxPrice,
            Boolean inStock
    ) {
        List<Specification<Product>> filters = new ArrayList<>();
        if (q != null && !q.isBlank()) filters.add(nameContains(q));
        if (category != null) filters.add(inCategory(category));
        if (maxPrice != null) filters.add(cheaperThan(maxPrice));
        if (Boolean.TRUE.equals(inStock)) filters.add(inStock());
        if (Boolean.FALSE.equals(inStock)) filters.add(Specification.not(inStock()));
        return Specification.allOf(filters);
    }
}

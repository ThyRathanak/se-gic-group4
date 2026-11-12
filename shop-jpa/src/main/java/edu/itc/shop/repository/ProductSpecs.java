package edu.itc.shop.repository;

import edu.itc.shop.product.Category;
import edu.itc.shop.product.Product;
import edu.itc.shop.domain.Money;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProductSpecs {

    private ProductSpecs() {
    }

    public static Specification<Product> nameContains(String query) {
        String pattern = "%" + query.toLowerCase(Locale.ROOT) + "%";
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("name")),
                pattern
        );
    }

    public static Specification<Product> inCategory(Category category) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.equal(
                root.get("category"),
                category
        );
    }

    public static Specification<Product> cheaperThan(BigDecimal maxPrice) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.lessThan(
                root.<Money>get("price").<BigDecimal>get("amount"),
                maxPrice
        );
    }

    public static Specification<Product> inStock() {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.greaterThan(
                root.<Integer>get("stock"),
                0
        );
    }

    public static Specification<Product> filter(
            String query,
            Category category,
            BigDecimal maxPrice,
            Boolean inStock
    ) {
        List<Specification<Product>> specifications = new ArrayList<>();

        if (query != null) {
            specifications.add(nameContains(query));
        }
        if (category != null) {
            specifications.add(inCategory(category));
        }
        if (maxPrice != null) {
            specifications.add(cheaperThan(maxPrice));
        }
        if (inStock != null) {
            specifications.add(inStock ? inStock() : Specification.not(inStock()));
        }

        return Specification.allOf(specifications);
    }
}

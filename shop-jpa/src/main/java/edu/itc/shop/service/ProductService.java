package edu.itc.shop.service;

import edu.itc.shop.product.Category;
import edu.itc.shop.product.Product;
import edu.itc.shop.repository.ProductRepository;
import edu.itc.shop.repository.ProductSpecs;
import edu.itc.shop.web.dto.ProductView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductView> search(
            String query,
            Category category,
            BigDecimal maxPrice,
            Boolean inStock,
            Pageable pageable
    ) {
        String normalizedQuery = query == null || query.isBlank() ? null : query;
        return productRepository.findAll(
                        ProductSpecs.filter(normalizedQuery, category, maxPrice, inStock),
                        pageable
                )
                .map(this::toView);
    }

    private ProductView toView(Product product) {
        return new ProductView(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getPrice().amount(),
                product.getStock()
        );
    }
}

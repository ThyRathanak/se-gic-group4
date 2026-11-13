package edu.itc.shop.config;

import edu.itc.shop.service.ProductService;
import edu.itc.shop.web.dto.ProductDto;
import java.util.List;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class LowStockHealthIndicator implements HealthIndicator {

    private final ProductService productService;
    private final ShopProperties shopProperties;

    public LowStockHealthIndicator(
            ProductService productService,
            ShopProperties shopProperties) {
        this.productService = productService;
        this.shopProperties = shopProperties;
    }

    @Override
    public Health health() {
        List<ProductDto> lowStockProducts = productService.search(null)
                .stream()
                .filter(product ->
                        product.stock() < shopProperties.lowStockThreshold())
                .toList();

        if (!lowStockProducts.isEmpty()) {
            return Health.down()
                    .withDetail("lowStockProducts", lowStockProducts)
                    .build();
        }

        return Health.up()
                .withDetail("lowStockProducts", List.of())
                .build();
    }
}
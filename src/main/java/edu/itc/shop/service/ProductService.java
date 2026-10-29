
package edu.itc.shop.service;

import edu.itc.shop.domain.Product;
import edu.itc.shop.repository.ProductRepository;
import edu.itc.shop.web.dto.CreateProduct;
import edu.itc.shop.web.dto.ProductDto;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductDto> search(String q) {
        return productRepository.search(q)
                .stream()
                .map(ProductDto::from)
                .toList();
    }

    public ProductDto getById(long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id));

        return ProductDto.from(product);
    }

    @Transactional
    public ProductDto create(CreateProduct cmd) {
        try {
            Product product = productRepository.insert(
                    cmd.name(),
                    cmd.price(),
                    cmd.stock());

            return ProductDto.from(product);
        } catch (DuplicateKeyException ex) {
            throw ex;
        }
    }

    @Transactional
    public ProductDto adjustStock(long id, int delta) {
        int rowsAffected = productRepository.updateStock(id, delta);

        if (rowsAffected == 0) {
            Product product = productRepository.findById(id)
                    .orElseThrow(() ->
                            new ProductNotFoundException(
                                    "Product not found with id: " + id));

            if (delta < 0) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: " + product.name());
            }

            throw new IllegalStateException(
                    "Could not update stock for product: " + product.name());
        }

        return getById(id);
    }

    @Transactional
    public void delete(long id) {
        int rowsAffected = productRepository.deleteById(id);

        if (rowsAffected == 0) {
            throw new ProductNotFoundException(
                    "Product not found with id: " + id);
        }
    }

    @Transactional
    public void restock(long fromId, long toId, int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero");
        }

        int rowsAffected1 =
                productRepository.updateStock(fromId, -qty);

        if (rowsAffected1 == 0) {
            if (productRepository.findById(fromId).isEmpty()) {
                throw new ProductNotFoundException(
                        "Source product not found: " + fromId);
            }

            throw new InsufficientStockException(
                    "Not enough stock in source product");
        }

        int rowsAffected2 =
                productRepository.updateStock(toId, qty);

        if (rowsAffected2 == 0) {
            throw new ProductNotFoundException(
                    "Target product not found: " + toId);
        }
    }
}

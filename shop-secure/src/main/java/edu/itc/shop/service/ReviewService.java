package edu.itc.shop.service;

import edu.itc.shop.domain.Review;
import edu.itc.shop.repository.ProductRepository;
import edu.itc.shop.repository.ReviewRepository;
import edu.itc.shop.web.dto.ReviewRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviews;
    private final ProductRepository products;

    public ReviewService(ReviewRepository reviews, ProductRepository products) {
        this.reviews = reviews;
        this.products = products;
    }

    @Transactional
    public Review create(ReviewRequest request, String username) {
        requireProduct(request.productId());
        return reviews.save(new Review(
                request.productId(), username, request.stars(), request.text()));
    }

    @Transactional(readOnly = true)
    public List<Review> forProduct(long productId) {
        requireProduct(productId);
        return reviews.findByProductIdOrderByIdAsc(productId);
    }

    private void requireProduct(long productId) {
        if (!products.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }
    }
}

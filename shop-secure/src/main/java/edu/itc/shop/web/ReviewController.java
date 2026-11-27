package edu.itc.shop.web;

import edu.itc.shop.domain.Review;
import edu.itc.shop.service.ReviewService;
import edu.itc.shop.web.dto.ReviewRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviews;

    public ReviewController(ReviewService reviews) {
        this.reviews = reviews;
    }

    @PostMapping("/reviews")
    public ResponseEntity<Review> create(
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication
    ) {
        Review saved = reviews.create(request, authentication.getName());
        return ResponseEntity.created(URI.create(
                "/api/products/" + saved.getProductId() + "/reviews")).body(saved);
    }

    @GetMapping("/products/{productId}/reviews")
    public List<Review> forProduct(@PathVariable long productId) {
        return reviews.forProduct(productId);
    }
}

package edu.itc.shop.domain;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.inject.Singleton;

@Singleton
public class ReviewRepository {

    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<Review>> reviews =
            new ConcurrentHashMap<>();
    private final AtomicLong ids = new AtomicLong();

    public List<Review> findByProductId(long productId) {
        List<Review> productReviews = reviews.get(productId);
        return productReviews == null ? List.of() : List.copyOf(productReviews);
    }

    public Review save(long productId, int stars, String author, String text) {
        Review review = new Review(
                ids.incrementAndGet(), productId, stars, author, text);
        reviews.computeIfAbsent(productId, ignored -> new CopyOnWriteArrayList<>())
                .add(review);
        return review;
    }
}

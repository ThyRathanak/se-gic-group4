package edu.itc.shop.web;

import edu.itc.shop.service.ReviewService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProductReviewsPageController {

    private final ReviewService reviews;

    public ProductReviewsPageController(ReviewService reviews) {
        this.reviews = reviews;
    }

    @GetMapping("/products/{productId}/reviews.html")
    public String reviews(@PathVariable long productId, Model model) {
        model.addAttribute("productId", productId);
        model.addAttribute("reviews", reviews.forProduct(productId));
        return "product-reviews";
    }
}

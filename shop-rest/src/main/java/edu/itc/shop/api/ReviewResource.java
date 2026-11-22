package edu.itc.shop.api;

import java.net.URI;
import java.util.List;

import edu.itc.shop.api.dto.ReviewRequest;
import edu.itc.shop.domain.Product;
import edu.itc.shop.domain.Review;
import edu.itc.shop.domain.ReviewRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReviewResource {

    private final Product product;
    private final ReviewRepository reviewRepo;

    public ReviewResource(Product product, ReviewRepository reviewRepo) {
        this.product = product;
        this.reviewRepo = reviewRepo;
    }

    @GET
    public List<Review> list() {
        return reviewRepo.findByProductId(product.id());
    }

    @POST
    public Response create(
            @Valid @NotNull ReviewRequest request,
            @Context UriInfo uriInfo) {
        Review review = reviewRepo.save(
                product.id(), request.stars, request.author, request.text);
        URI location = uriInfo.getAbsolutePathBuilder()
                .path(Long.toString(review.id()))
                .build();
        return Response.created(location).entity(review).build();
    }
}

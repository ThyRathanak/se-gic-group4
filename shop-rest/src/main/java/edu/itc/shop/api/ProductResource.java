package edu.itc.shop.api;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import edu.itc.shop.api.dto.ProductDto;
import edu.itc.shop.api.dto.ProductV2;
import edu.itc.shop.api.dto.StockChange;
import edu.itc.shop.api.error.Problem;
import edu.itc.shop.api.params.ProductFilter;
import edu.itc.shop.domain.DuplicateSkuException;
import edu.itc.shop.domain.InsufficientStockException;
import edu.itc.shop.domain.Product;
import edu.itc.shop.domain.ProductRepository;
import edu.itc.shop.domain.ReviewRepository;

@Path("/products")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ProductResource {

    private final ProductRepository repo;
    private final ReviewRepository reviewRepo;

    @Inject
    public ProductResource(
            ProductRepository repo,
            ReviewRepository reviewRepo) {
        this.repo = repo;
        this.reviewRepo = reviewRepo;
    }

    @GET
    public Response list(
            @BeanParam @Valid ProductFilter f,
            @Context UriInfo uriInfo) {

        var products = repo.findAll().stream();

        if (f.minPrice != null && !f.minPrice.currency().equals("EUR")) {
            throw new BadRequestException("Currency must be EUR");
        }

        if (f.maxPrice != null && !f.maxPrice.currency().equals("EUR")) {
            throw new BadRequestException("Currency must be EUR");
        }

        if (f.q != null && !f.q.isBlank()) {
            products = products.filter(p ->
                    p.name().toLowerCase()
                            .contains(f.q.toLowerCase()));
        }

        if (f.minPrice != null) {
            products = products.filter(p ->
                    p.price().compareTo(f.minPrice.amount()) >= 0);
        }

        if (f.maxPrice != null) {
            products = products.filter(p ->
                    p.price().compareTo(f.maxPrice.amount()) <= 0);
        }

        if (f.inStock) {
            products = products.filter(p -> p.stock() > 0);
        }

        Comparator<Product> comparator = switch (f.sortField()) {
            case "name" -> Comparator.comparing(Product::name);
            case "price" -> Comparator.comparing(Product::price);
            case "stock" -> Comparator.comparingInt(Product::stock);
            default -> throw new BadRequestException(
                    "Unknown sort field: " + f.sortField());
        };

        if (f.descending()) {
            comparator = comparator.reversed();
        }

        List<Product> filtered = products.sorted(comparator).toList();
        int total = filtered.size();
        int lastPage = total == 0 ? 0 : (total - 1) / f.size;
        long startLong = (long) f.page * f.size;
        int start = (int) Math.min(startLong, total);
        int end = Math.min(start + f.size, total);
        List<ProductDto> page = filtered.subList(start, end).stream()
                .map(ProductDto::from)
                .toList();

        Response.ResponseBuilder response = Response.ok(page)
                .header("X-Total-Count", total);
        addPageLink(response, uriInfo, "first", 0);
        if (f.page > 0) {
            addPageLink(response, uriInfo, "prev", f.page - 1);
        }
        if (f.page < lastPage) {
            addPageLink(response, uriInfo, "next", f.page + 1);
        }
        addPageLink(response, uriInfo, "last", lastPage);
        return response.build();
    }

    private void addPageLink(
            Response.ResponseBuilder response,
            UriInfo uriInfo,
            String relation,
            int page) {
        response.header("Link", Link.fromUriBuilder(
                        uriInfo.getRequestUriBuilder()
                                .replaceQueryParam("page", page))
                .rel(relation)
                .build());
    }

    @GET
    @Path("{id: \\d+}")
    @Produces({"application/json; qs=1.0", "application/vnd.shop.v1+json"})
    public Response get(
            @PathParam("id") long id,
            @Context Request request) {
        Product product = repo.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Product " + id + " not found"));
        EntityTag tag = new EntityTag(Integer.toString(product.version()));
        Response.ResponseBuilder precondition = request.evaluatePreconditions(tag);
        if (precondition != null) {
            return precondition.build();
        }

        CacheControl cacheControl = new CacheControl();
        cacheControl.setPrivate(true);
        cacheControl.setMaxAge(60);
        cacheControl.setNoTransform(true);
        return Response.ok(ProductDto.from(product))
                .tag(tag)
                .cacheControl(cacheControl)
                .build();
    }

    @GET
    @Path("{id: \\d+}")
    @Produces("application/vnd.shop.v2+json")
    public ProductV2 getV2(@PathParam("id") long id) {
        Product product = repo.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Product " + id + " not found"));
        return ProductV2.from(product);
    }

    @Path("{id}/reviews")
    public ReviewResource reviews(@PathParam("id") long id) {
        Product product = repo.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Product " + id + " not found"));
        return new ReviewResource(product, reviewRepo);
    }

    @POST
    public Response create(
        @Valid @NotNull ProductDto dto,
        @Context UriInfo uriInfo) {

        repo.findBySku(dto.sku).ifPresent(p -> {
            throw new DuplicateSkuException(dto.sku);
        });

        Product saved = repo.save(dto.toProduct(0, 0));

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(Long.toString(saved.id()))
                .build();

        return Response.created(location)
                .entity(ProductDto.from(saved))
                .build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("id") long id,
            @Valid @NotNull ProductDto dto,
            @HeaderParam("If-Match") String ifMatch,
            @Context Request request,
            @Context UriInfo uriInfo) {

        Product existing = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (ifMatch == null) {
            Problem problem = new Problem(
                    URI.create("about:blank"),
                    "Precondition Required",
                    428,
                    "The If-Match header is required.",
                    uriInfo.getRequestUri(),
                    null,
                    null
            );
            return Response.status(428)
                    .type("application/problem+json")
                    .entity(problem)
                    .build();
        }

        EntityTag currentTag = new EntityTag(Integer.toString(existing.version()));
        Response.ResponseBuilder precondition =
                request.evaluatePreconditions(currentTag);
        if (precondition != null) {
            return precondition.build();
        }

        repo.findBySku(dto.sku)
                .filter(p -> p.id() != id)
                .ifPresent(p -> {
                    throw new DuplicateSkuException(dto.sku);
                });

        Product updated = repo.save(dto.toProduct(id, existing.version()));

        EntityTag updatedTag = new EntityTag(Integer.toString(updated.version()));
        return Response.ok(ProductDto.from(updated))
                .tag(updatedTag)
                .build();
    }

    @DELETE
    @Path("{id: \\d+}")
    public Response delete(@PathParam("id") long id) {
        if (repo.findById(id).isEmpty()) {
            throw new NotFoundException("Product " + id + " not found");
        }

        repo.delete(id);
        return Response.noContent().build();
    }

    @PATCH
    @Path("/{id}/stock")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeStock(
            @PathParam("id") long id,
            @Valid @NotNull StockChange change) {

        Product existing = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        int newStock = existing.stock() + change.delta();

        if (newStock < 0) {
            throw new InsufficientStockException(
                    id,
                    existing.stock(),
                    change.delta()
            );
        }

        Product updated = new Product(
                existing.id(),
                existing.name(),
                existing.sku(),
                existing.price(),
                newStock,
                existing.version() + 1
        );

        repo.save(updated);

        return Response.ok(ProductDto.from(updated)).build();
    }
}

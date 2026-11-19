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
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import edu.itc.shop.api.dto.ProductDto;
import edu.itc.shop.api.dto.StockChange;
import edu.itc.shop.api.params.ProductFilter;
import edu.itc.shop.domain.DuplicateSkuException;
import edu.itc.shop.domain.InsufficientStockException;
import edu.itc.shop.domain.Product;
import edu.itc.shop.domain.ProductRepository;

@Path("/products")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ProductResource {

    private final ProductRepository repo;

    @Inject
    public ProductResource(ProductRepository repo) {
        this.repo = repo;
    }

    @GET
    public List<ProductDto> list(@BeanParam @Valid ProductFilter f) {

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

        return products.sorted(comparator)
                .map(ProductDto::from)
                .toList();
    }

    @GET
    @Path("{id: \\d+}")
    public ProductDto get(@PathParam("id") long id) {
        // throw new IllegalStateException("Temporary 500 test");
        return repo.findById(id)
                .map(ProductDto::from)
                .orElseThrow(() ->
                        new NotFoundException("Product " + id + " not found"));
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
            @Valid @NotNull ProductDto dto) {

        Product existing = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        repo.findBySku(dto.sku)
                .filter(p -> p.id() != id)
                .ifPresent(p -> {
                    throw new DuplicateSkuException(dto.sku);
                });

        Product updated = dto.toProduct(id, existing.version() + 1);
        repo.save(updated);

        return Response.ok(ProductDto.from(updated)).build();
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
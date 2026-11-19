
package edu.itc.shop.api;

import edu.itc.shop.api.dto.ProductDto;
import edu.itc.shop.api.dto.StockChange;
import edu.itc.shop.domain.DuplicateSkuException;
import edu.itc.shop.domain.InsufficientStockException;
import edu.itc.shop.domain.Product;
import edu.itc.shop.domain.ProductRepository;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;

import java.net.URI;
import java.util.List;

@Path("/products")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ProductResource {

    private final ProductRepository repo;

    @Inject
    public ProductResource(ProductRepository repo) {
        this.repo = repo;
    }

    // GET all products
    @GET
    public List<ProductDto> list() {
        return repo.findAll()
                .stream()
                .map(ProductDto::from)
                .toList();
    }

    // GET one product
    @GET
    @Path("{id: \\d+}")
    public ProductDto get(@PathParam("id") long id) {
        Product product = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Product " + id + " not found"));

        return ProductDto.from(product);
    }

    // POST create a product
    @POST
    public Response create(ProductDto dto, @Context UriInfo uriInfo) {

        if (repo.findBySku(dto.sku).isPresent()) {
            throw new DuplicateSkuException(dto.sku);
        }

        Product product = dto.toProduct(0, 0);
        Product saved = repo.save(product);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(String.valueOf(saved.id()))
                .build();

        return Response.created(location)
                .entity(ProductDto.from(saved))
                .build();
    }

    // PUT update a product
    @PUT
    @Path("{id: \\d+}")
    public ProductDto update(
            @PathParam("id") long id,
            ProductDto dto) {

        Product existing = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Product " + id + " not found"));

        repo.findBySku(dto.sku)
                .filter(product -> product.id() != id)
                .ifPresent(product -> {
                    throw new DuplicateSkuException(dto.sku);
                });

        Product updated = dto.toProduct(id, existing.version());
        Product saved = repo.save(updated);

        return ProductDto.from(saved);
    }

    // PATCH update stock
    @PATCH
    @Path("{id: \\d+}/stock")
    @Consumes(MediaType.APPLICATION_JSON)
    public ProductDto changeStock(
            @PathParam("id") long id,
            StockChange change) {

        Product existing = repo.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Product " + id + " not found"));

        int newStock = existing.stock() + change.delta();

        if (newStock < 0) {
            throw new InsufficientStockException(id);
        }

        Product updated = new Product(
                existing.id(),
                existing.name(),
                existing.sku(),
                existing.price(),
                newStock,
                existing.version()
        );

        Product saved = repo.save(updated);

        return ProductDto.from(saved);
    }

    // DELETE product
    @DELETE
    @Path("{id: \\d+}")
    public Response delete(@PathParam("id") long id) {

        if (repo.findById(id).isEmpty()) {
            throw new NotFoundException(
                    "Product " + id + " not found");
        }

        repo.delete(id);

        return Response.noContent().build();
    }
}

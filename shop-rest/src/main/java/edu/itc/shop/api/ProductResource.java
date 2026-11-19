package edu.itc.shop.api;

import edu.itc.shop.api.dto.ProductDto;
import edu.itc.shop.api.dto.StockChange;
import edu.itc.shop.domain.DuplicateSkuException;
import edu.itc.shop.domain.InsufficientStockException;
import edu.itc.shop.domain.Product;
import edu.itc.shop.domain.ProductRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

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

    @GET
    public List<ProductDto> list() {
        return repo.findAll().stream().map(ProductDto::from).toList();
    }

    @GET
    @Path("{id: \\d+}")
    public ProductDto get(@PathParam("id") long id) {
        return repo.findById(id)
                .map(ProductDto::from)
                .orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
    }

    @POST
    public Response create(ProductDto dto, @Context UriInfo uriInfo) {
        repo.findBySku(dto.sku).ifPresent(p -> {
            throw new DuplicateSkuException(dto.sku);
        });
        Product saved = repo.save(dto.toProduct(0, 0));
        URI location = uriInfo.getAbsolutePathBuilder().path(Long.toString(saved.id())).build();
        return Response.created(location).entity(ProductDto.from(saved)).build();
    }

    @PUT
    @Path("{id: \\d+}")
    public ProductDto put(@PathParam("id") long id, ProductDto dto) {
        Product existing = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
        repo.findBySku(dto.sku)
                .filter(p -> p.id() != id)
                .ifPresent(p -> { throw new DuplicateSkuException(dto.sku); });
        Product updated = repo.save(dto.toProduct(id, existing.version()));
        return ProductDto.from(updated);
    }

    @PATCH
    @Path("{id: \\d+}/stock")
    @Consumes(MediaType.APPLICATION_JSON)
    public ProductDto patchStock(@PathParam("id") long id, StockChange change) {
        Product existing = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
        int newStock = existing.stock() + change.delta();
        if (newStock < 0) {
            throw new InsufficientStockException(id, existing.stock(), change.delta());
        }
        Product updated = repo.save(new Product(
                existing.id(),
                existing.name(),
                existing.sku(),
                existing.price(),
                newStock,
                existing.version()
        ));
        return ProductDto.from(updated);
    }

    @DELETE
    @Path("{id: \\d+}")
    public Response delete(@PathParam("id") long id) {
        repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product " + id + " not found"));
        repo.delete(id);
        return Response.noContent().build();
    }
}
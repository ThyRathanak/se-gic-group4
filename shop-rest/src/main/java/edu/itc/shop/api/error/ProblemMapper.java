package edu.itc.shop.api.error;

import java.net.URI;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import edu.itc.shop.domain.DuplicateSkuException;
import edu.itc.shop.domain.InsufficientStockException;

@Provider
public class ProblemMapper implements ExceptionMapper<Exception> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {
        // exception.printStackTrace();
        int status = 500;
        String title = "Internal Server Error";
        String detail = "An unexpected error occurred.";
        String reference = null;

        if (exception instanceof WebApplicationException webException) {
            status = webException.getResponse().getStatus();
            title = webException.getResponse().getStatusInfo().getReasonPhrase();
            detail = webException.getMessage();

        } else if (exception instanceof DuplicateSkuException) {
            status = 409;
            title = "Conflict";
            detail = exception.getMessage();

        } else if (exception instanceof InsufficientStockException) {
            status = 409;
            title = "Conflict";
            detail = exception.getMessage();

        } else {
            reference = UUID.randomUUID().toString();
            detail = "An unexpected error occurred. Reference: " + reference;
        }

        Problem problem = new Problem(
                URI.create("about:blank"),
                title,
                status,
                detail,
                uriInfo.getRequestUri(),
                null,
                reference
        );

        return Response.status(status)
                .type("application/problem+json")
                .entity(problem)
                .build();
    }
}
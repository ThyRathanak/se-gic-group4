package edu.itc.shop.api.error;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValidationMapper
        implements ExceptionMapper<ConstraintViolationException> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(ConstraintViolationException exception) {

        List<Problem.Violation> errors = exception.getConstraintViolations()
                .stream()
                .map(this::toViolation)
                .sorted(Comparator.comparing(Problem.Violation::field))
                .toList();

        Problem problem = new Problem(
                URI.create("about:blank"),
                "Validation failed",
                400,
                "One or more fields have invalid values.",
                uriInfo.getRequestUri(),
                errors,
                null
        );

        return Response.status(Response.Status.BAD_REQUEST)
                .type("application/problem+json")
                .entity(problem)
                .build();
    }

    private Problem.Violation toViolation(
            ConstraintViolation<?> violation) {

        String field = violation.getPropertyPath().toString();

        return new Problem.Violation(
                field,
                violation.getMessage()
        );
    }
}
package edu.itc.shop.api.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class CorsFilter implements ContainerResponseFilter {

    private static final String ALLOWED_ORIGIN = "http://localhost:5173";

    @Override
    public void filter(
            ContainerRequestContext request,
            ContainerResponseContext response) {

        String origin = request.getHeaderString("Origin");

        if (ALLOWED_ORIGIN.equals(origin)) {
            response.getHeaders().putSingle(
                    "Access-Control-Allow-Origin", ALLOWED_ORIGIN);
            response.getHeaders().putSingle(
                    "Vary", "Origin");
            response.getHeaders().putSingle(
                    "Access-Control-Allow-Credentials", "true");
            response.getHeaders().putSingle(
                    "Access-Control-Allow-Headers",
                    "Content-Type, Accept, X-API-Key, X-Correlation-Id");
            response.getHeaders().putSingle(
                    "Access-Control-Allow-Methods",
                    "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        }
    }
}

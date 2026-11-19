package edu.itc.shop.api.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION - 100)
public class CorsPreflightFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext request) {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())
                && request.getHeaderString("Origin") != null
                && request.getHeaderString(
                        "Access-Control-Request-Method") != null) {

            request.abortWith(Response.noContent().build());
        }
    }
}

package edu.itc.shop.api.filter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class ApiKeyFilter implements ContainerRequestFilter {

    private static final String HEADER = "X-API-Key";
    private static final String DEFAULT_API_KEY = "dev-secret-123";

    private final String expectedApiKey = loadApiKey();

    private String loadApiKey() {
        String configuredKey = System.getenv("SHOP_API_KEY");

        if (configuredKey == null || configuredKey.isBlank()) {
            return DEFAULT_API_KEY;
        }

        return configuredKey;
    }

    @Override
    public void filter(ContainerRequestContext request) {

        String method = request.getMethod();

        // Only protect operations that modify data.
        if ("GET".equalsIgnoreCase(method)
                || "HEAD".equalsIgnoreCase(method)
                || "OPTIONS".equalsIgnoreCase(method)) {
            return;
        }

        String providedKey = request.getHeaderString(HEADER);

        if (providedKey == null || !MessageDigest.isEqual(
                expectedApiKey.getBytes(StandardCharsets.UTF_8),
                providedKey.getBytes(StandardCharsets.UTF_8))) {

            request.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity("Missing or invalid API key")
                            .type("text/plain")
                            .build()
            );
        }
    }
}

package edu.itc.shop.api.filter;

import java.util.UUID;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.USER)
public class CorrelationFilter
        implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String HEADER = "X-Correlation-Id";
    private static final String ID_PROPERTY = "correlationId";
    private static final String START_PROPERTY = "requestStartTime";

    @Override
    public void filter(ContainerRequestContext request) {
        String id = request.getHeaderString(HEADER);

        if (id == null || id.isBlank()
                || id.length() > 128
                || id.chars().anyMatch(Character::isISOControl)) {
            id = UUID.randomUUID().toString();
        }

        request.setProperty(ID_PROPERTY, id);
        request.setProperty(START_PROPERTY, System.nanoTime());
    }

    @Override
    public void filter(
            ContainerRequestContext request,
            ContainerResponseContext response) {

        String id = (String) request.getProperty(ID_PROPERTY);
        Long start = (Long) request.getProperty(START_PROPERTY);

        if (id != null) {
            response.getHeaders().putSingle(HEADER, id);
        }

        long durationMs = start == null
                ? 0
                : (System.nanoTime() - start) / 1_000_000;

        String method = request.getMethod();
        String path = request.getUriInfo().getRequestUri().getPath();
        int status = response.getStatus();

        System.out.printf(
                "[HTTP] %s %s -> %d | correlationId=%s | %d ms%n",
                method, path, status, id, durationMs
        );
    }
}

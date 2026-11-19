package edu.itc.shop.api.error;

import java.net.URI;
import java.util.List;

public record Problem(
        URI type,
        String title,
        int status,
        String detail,
        URI instance,
        List<Violation> errors,
        String reference
) {
    public record Violation(
            String field,
            String message
    ) {}
}
package edu.itc.shop.api.filter;

import java.io.IOException;
import java.util.Locale;
import java.util.zip.GZIPOutputStream;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;

@Provider
@Priority(Priorities.ENTITY_CODER)
public class GzipWriterInterceptor implements WriterInterceptor {

    @Context
    private HttpHeaders requestHeaders;

    @Override
    public void aroundWriteTo(WriterInterceptorContext context)
            throws IOException, WebApplicationException {

        String acceptEncoding = requestHeaders.getHeaderString(
                HttpHeaders.ACCEPT_ENCODING);

        if (!acceptsGzip(acceptEncoding)
                || context.getHeaders().containsKey("Content-Encoding")) {
            context.proceed();
            return;
        }

        context.getHeaders().putSingle("Content-Encoding", "gzip");
        context.getHeaders().remove("Content-Length");

        String vary = String.valueOf(
                context.getHeaders().getOrDefault("Vary", java.util.List.of()));

        if (!vary.contains("Accept-Encoding")) {
            context.getHeaders().putSingle(
                    "Vary",
                    vary.equals("[]") ? "Accept-Encoding"
                            : vary + ", Accept-Encoding");
        }

        GZIPOutputStream gzip = new GZIPOutputStream(
                context.getOutputStream());

        context.setOutputStream(gzip);
        context.proceed();
        gzip.finish();
    }

    private boolean acceptsGzip(String header) {
        if (header == null) {
            return false;
        }

        for (String encoding : header.split(",")) {
            String[] parts = encoding.trim().split(";");
            if (!parts[0].trim().equalsIgnoreCase("gzip")) {
                continue;
            }

            for (int i = 1; i < parts.length; i++) {
                String parameter = parts[i].trim()
                        .toLowerCase(Locale.ROOT);

                if (parameter.startsWith("q=")) {
                    try {
                        if (Double.parseDouble(
                                parameter.substring(2).trim()) <= 0) {
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            }

            return true;
        }

        return false;
    }
}

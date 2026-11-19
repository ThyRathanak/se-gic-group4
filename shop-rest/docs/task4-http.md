# Task 4 – HTTP Filters, Security and Compression

## 1. Overview

This task improves the Shop REST API by implementing request tracking, CORS support, API-key authentication, and gzip response compression.

## 2. Correlation ID and Request Logging

**File:** `CorrelationFilter.java`

- Reads the `X-Correlation-Id` request header.
- Generates a UUID if the header is missing or invalid.
- Returns the correlation ID in the response header.
- Logs the HTTP method, request path, response status, correlation ID, and request duration.

## 3. Cross-Origin Resource Sharing (CORS)

**File:** `CorsFilter.java`

Allows the frontend application at `http://localhost:5173` to communicate with the backend.

Allowed methods:
- GET
- POST
- PUT
- PATCH
- DELETE
- OPTIONS

Allowed request headers:
- Content-Type
- Accept
- X-API-Key
- X-Correlation-Id

The filter adds the appropriate CORS response headers.

## 4. CORS Preflight Requests

**File:** `CorsPreflightFilter.java`

Browsers may send an OPTIONS request before an actual cross-origin request.

The filter detects requests containing `Origin` and `Access-Control-Request-Method`, then responds with `204 No Content`.

## 5. API-Key Authentication

**File:** `ApiKeyFilter.java`

Protects operations that modify product data.

- GET, HEAD, and OPTIONS do not require an API key.
- POST, PUT, PATCH, and DELETE require the `X-API-Key` header.
- Missing or incorrect keys return `401 Unauthorized`.
- Correct keys allow the request to continue.

The default development key is `dev-secret-123`.

The application also supports the `SHOP_API_KEY` environment variable.

| Request | Expected result |
|---|---|
| POST without a key | 401 Unauthorized |
| POST with an incorrect key | 401 Unauthorized |
| POST with the correct key | Resource handles request |
| GET without a key | 200 OK |

## 6. Gzip Response Compression

**File:** `GzipWriterInterceptor.java`

Compresses responses when the client sends:

`Accept-Encoding: gzip`

The response includes:
- `Content-Encoding: gzip`
- `Vary: Accept-Encoding`

When the client requests `Accept-Encoding: identity`, the response remains uncompressed.

## 7. Testing

Run the following commands while the backend is running.

### 7.1 Correlation ID

```bash
# curl -i -H "X-Correlation-Id: my-test-123" \
# http://localhost:8080/api/products
# Task 5 — Pagination, caching and versioning

Start the service with `mvn exec:java` from `shop-rest`. The examples below use
the development API key configured by the project.

## Paginated product list

The in-memory repository starts with 33 products. Page numbers are zero-based;
this requests the second page, with ten products per page:

```bash
curl -i 'http://localhost:8080/api/products?page=1&size=10'
```

The response contains ten products, `X-Total-Count: 33`, and `Link` headers
for `first`, `prev`, `next`, and `last`. The links preserve `size=10`; the last
page number is `3`.

Expected response headers include:

```http
HTTP/1.1 200 OK
X-Total-Count: 33
Link: <http://localhost:8080/api/products?page=0&size=10>; rel="first"
Link: <http://localhost:8080/api/products?page=0&size=10>; rel="prev"
Link: <http://localhost:8080/api/products?page=2&size=10>; rel="next"
Link: <http://localhost:8080/api/products?page=3&size=10>; rel="last"
```

## Conditional GET and PUT

Fetch product 1 and note its `ETag` (the seed product starts at version 1):

```bash
curl -i -H 'Accept: application/vnd.shop.v1+json' \
  http://localhost:8080/api/products/1
```

The response includes `ETag: "1"` and `Cache-Control` directives `private`,
`max-age=60`, and `no-transform`.

Use that tag to get a bodyless `304 Not Modified` response:

```bash
curl -i -H 'Accept: application/vnd.shop.v1+json' \
  -H 'If-None-Match: "1"' \
  http://localhost:8080/api/products/1
```

All PUT examples use this valid product body and the development API key:

```bash
curl -i -X PUT http://localhost:8080/api/products/1 \
  -H 'X-API-Key: dev-secret-123' \
  -H 'Content-Type: application/json' \
  -d '{"name":"Kettle","sku":"KT-01","price":24.90,"stock":12}'
```

Without `If-Match`, the response is `428 Precondition Required` with
`Content-Type: application/problem+json`.

With a stale tag, the response is `412 Precondition Failed`:

```bash
curl -i -X PUT http://localhost:8080/api/products/1 \
  -H 'X-API-Key: dev-secret-123' \
  -H 'If-Match: "0"' \
  -H 'Content-Type: application/json' \
  -d '{"name":"Kettle","sku":"KT-01","price":24.90,"stock":12}'
```

With the current tag, the response is `200 OK` and returns the new `ETag`
(`"2"` for the initial seed state):

```bash
curl -i -X PUT http://localhost:8080/api/products/1 \
  -H 'X-API-Key: dev-secret-123' \
  -H 'If-Match: "1"' \
  -H 'Content-Type: application/json' \
  -d '{"name":"Kettle","sku":"KT-01","price":24.90,"stock":12}'
```

## Product representation versions

Both requests address product 1. The v1 representation keeps the numeric
price:

```bash
curl -i -H 'Accept: application/vnd.shop.v1+json' \
  http://localhost:8080/api/products/1
```

The v1 body contains fields such as `"price":24.90`.

The v2 representation uses a `Money` object and includes `available`:

```bash
curl -i -H 'Accept: application/vnd.shop.v2+json' \
  http://localhost:8080/api/products/1
```

The v2 body contains `price` as a money object, for example
`"price":{"amount":24.90,"currency":"EUR"}`, and an `available` boolean.

## Product reviews

Create a review; the response is `201 Created` with a `Location` header for
the new review:

```bash
curl -i -X POST http://localhost:8080/api/products/1/reviews \
  -H 'X-API-Key: dev-secret-123' \
  -H 'Content-Type: application/json' \
  -d '{"stars":5,"author":"Ada","text":"Excellent kettle."}'
```

List reviews for product 1:

```bash
curl -i http://localhost:8080/api/products/1/reviews
```

The locator checks that the product exists, so reviews for product 999 return
`404 Not Found`:

```bash
curl -i http://localhost:8080/api/products/999/reviews
```

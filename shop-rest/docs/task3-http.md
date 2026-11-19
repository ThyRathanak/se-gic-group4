# Task 3 – HTTP Tests

## 1. Filtered and Sorted Product List

**Request:**
```http
GET /api/products?minPrice=5%20EUR&inStock=true&sort=price,desc
```

**Response:**
- Status: `200 OK`
- Content-Type: `application/json`

```json
[
  {"id":1,"name":"Kettle","price":24.90,"sku":"KT-01","stock":12},
  {"id":2,"name":"Mug","price":6.50,"sku":"MG-01","stock":40}
]
```

## 2. Invalid Money Parameter

**Request:**
```http
GET /api/products?minPrice=abc
```

**Response:**
- Status: `400 Bad Request`
- Content-Type: `application/problem+json`

```json
{
  "detail": "Invalid money format. Use: amount currency",
  "instance": "http://localhost:8080/api/products?minPrice=abc",
  "status": 400,
  "title": "Bad Request",
  "type": "about:blank"
}
```

## 3. Invalid Sorting Field

**Request:**
```http
GET /api/products?sort=colour,asc
```

**Response:**
- Status: `400 Bad Request`
- Content-Type: `application/problem+json`

```json
{
  "detail": "Unknown sort field: colour",
  "instance": "http://localhost:8080/api/products?sort=colour,asc",
  "status": 400,
  "title": "Bad Request",
  "type": "about:blank"
}
```

## 4. Invalid POST Body

**Request:**
```http
POST /api/products
Content-Type: application/json
```

```json
{
  "name": "",
  "sku": "wrong",
  "price": 0,
  "stock": 5
}
```

**Response:**
- Status: `400 Bad Request`
- Content-Type: `application/problem+json`

```json
{
  "detail": "One or more fields have invalid values.",
  "errors": [
    {
      "field": "create.arg0.name",
      "message": "must not be blank"
    },
    {
      "field": "create.arg0.price",
      "message": "must be greater than or equal to 0.01"
    },
    {
      "field": "create.arg0.sku",
      "message": "must match \"[A-Z]{2}-\\d{2}\""
    }
  ],
  "instance": "http://localhost:8080/api/products",
  "status": 400,
  "title": "Validation failed",
  "type": "about:blank"
}
```

## 5. Product Not Found

**Request:**
```http
GET /api/products/99
```

**Response:**
- Status: `404 Not Found`
- Content-Type: `application/problem+json`

```json
{
  "detail": "Product 99 not found",
  "instance": "http://localhost:8080/api/products/99",
  "status": 404,
  "title": "Not Found",
  "type": "about:blank"
}
```

## 6. Unexpected Server Error

For this test, a temporary `IllegalStateException` was thrown from the GET-by-ID resource method.

**Request:**
```http
GET /api/products/1
```

**Response:**
- Status: `500 Internal Server Error`
- Content-Type: `application/problem+json`

```json
{
  "detail": "An unexpected error occurred. Reference: 02874e21-58c4-4603-87f0-c18510162568",
  "instance": "http://localhost:8080/api/products/1",
  "reference": "02874e21-58c4-4603-87f0-c18510162568",
  "status": 500,
  "title": "Internal Server Error",
  "type": "about:blank"
}
```

The temporary exception was removed after testing.
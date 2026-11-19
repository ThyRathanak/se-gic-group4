
# Task 2 — Full CRUD with JSON and XML

## 1. Create a product

POST /api/products

Expected: 201 Created

- Product is created.
- Location header points to the created product.
- Response body contains ProductDto.

## 2. Duplicate SKU

POST /api/products

Expected: 409 Conflict

- DuplicateSkuException is thrown when the SKU already exists.

## 3. Retrieve product as XML

GET /api/products/4
Accept: application/xml

Expected: 200 OK

- Response is an XML product representation.
- Product version is not exposed.

## 4. Create product using XML

POST /api/products
Content-Type: application/xml

Expected: 201 Created

- XML request body is converted into ProductDto.

## 5. Update product

PUT /api/products/4

Expected: 200 OK

- Product name, SKU, price and stock are replaced.

## 6. Update missing product

PUT /api/products/99

Expected: 404 Not Found

## 7. Update stock successfully

PATCH /api/products/1/stock
Body: {"delta": -5}

Expected: 200 OK

- Stock decreases by five.

## 8. Insufficient stock

PATCH /api/products/3/stock
Body: {"delta": -1}

Expected: 409 Conflict

- InsufficientStockException is thrown.

## 9. Delete product

DELETE /api/products/4

Expected: 204 No Content

## 10. Delete missing product

DELETE /api/products/4

Expected: 404 Not Found

## 11. Unsupported Content-Type

POST /api/products
Content-Type: text/plain

Expected: 415 Unsupported Media Type

- Jersey rejects the unsupported media type.

# API session

Captured from the dev-profile application at `http://localhost:18081` on 2026-11-12. All requests returned `HTTP/1.1 200` and `Content-Type: application/json`.

## Orders by status

```http
GET /api/orders?status=PAID&page=0&size=5&sort=placedAt,desc HTTP/1.1
Host: localhost:18081
```

```json
{"content":[{"customer":{"name":"Sophea Chan"},"id":2,"placedAt":"2026-11-11T14:16:20.367298Z","status":"PAID"},{"customer":{"name":"Vanna Kim"},"id":6,"placedAt":"2026-11-07T14:16:20.367298Z","status":"PAID"},{"customer":{"name":"Dara Sok"},"id":10,"placedAt":"2026-11-03T14:16:20.367298Z","status":"PAID"},{"customer":{"name":"Sophea Chan"},"id":14,"placedAt":"2026-10-30T14:16:20.367298Z","status":"PAID"},{"customer":{"name":"Vanna Kim"},"id":18,"placedAt":"2026-10-26T14:16:20.367298Z","status":"PAID"}],"empty":false,"first":true,"last":false,"number":0,"numberOfElements":5,"pageable":{"offset":0,"pageNumber":0,"pageSize":5,"paged":true,"sort":{"empty":false,"sorted":true,"unsorted":false},"unpaged":false},"size":5,"sort":{"empty":false,"sorted":true,"unsorted":false},"totalElements":8,"totalPages":2}
```

## Product search

```http
GET /api/products?q=Product&category=HARDWARE&maxPrice=100&inStock=true&page=0&size=5 HTTP/1.1
Host: localhost:18081
```

```json
{"content":[{"id":2,"sku":"DEV-002","name":"Dev Sample Product 2","amount":8.17,"stock":9},{"id":6,"sku":"DEV-006","name":"Dev Sample Product 6","amount":20.85,"stock":25},{"id":10,"sku":"DEV-010","name":"Dev Sample Product 10","amount":32.53,"stock":41}],"empty":false,"first":true,"last":true,"number":0,"numberOfElements":3,"pageable":{"offset":0,"pageNumber":0,"pageSize":5,"paged":true,"sort":{"empty":true,"sorted":false,"unsorted":true},"unpaged":false},"size":5,"sort":{"empty":true,"sorted":false,"unsorted":true},"totalElements":3,"totalPages":1}
```

## Customer orders with lines

```http
GET /api/customers/dara@example.com/orders HTTP/1.1
Host: localhost:18081
```

```json
[{"id":1,"status":"NEW","placedAt":"2026-11-12T14:16:20.367298Z","total":{"amount":27.68,"currency":"EUR"},"lines":[{"id":1,"productId":1,"sku":"DEV-001","name":"Dev Sample Product 1","quantity":1,"unitPrice":{"amount":5.00,"currency":"EUR"}},{"id":2,"productId":3,"sku":"DEV-003","name":"Dev Sample Product 3","quantity":2,"unitPrice":{"amount":11.34,"currency":"EUR"}}]},{"id":4,"status":"CANCELLED","placedAt":"2026-11-09T14:16:20.367298Z","total":{"amount":168.99,"currency":"EUR"},"lines":[{"id":10,"productId":10,"sku":"DEV-010","name":"Dev Sample Product 10","quantity":4,"unitPrice":{"amount":32.53,"currency":"EUR"}},{"id":11,"productId":12,"sku":"DEV-012","name":"Dev Sample Product 12","quantity":1,"unitPrice":{"amount":38.87,"currency":"EUR"}}]},{"id":7,"status":"SHIPPED","placedAt":"2026-11-06T14:16:20.367298Z","total":{"amount":186.50,"currency":"EUR"},"lines":[{"id":19,"productId":7,"sku":"DEV-007","name":"Dev Sample Product 7","quantity":3,"unitPrice":{"amount":23.02,"currency":"EUR"}},{"id":20,"productId":9,"sku":"DEV-009","name":"Dev Sample Product 9","quantity":4,"unitPrice":{"amount":29.36,"currency":"EUR"}}]},{"id":10,"status":"PAID","placedAt":"2026-11-03T14:16:20.367298Z","total":{"amount":91.57,"currency":"EUR"},"lines":[{"id":28,"productId":4,"sku":"DEV-004","name":"Dev Sample Product 4","quantity":2,"unitPrice":{"amount":14.51,"currency":"EUR"}},{"id":29,"productId":6,"sku":"DEV-006","name":"Dev Sample Product 6","quantity":3,"unitPrice":{"amount":20.85,"currency":"EUR"}}]},{"id":13,"status":"NEW","placedAt":"2026-10-31T14:16:20.367298Z","total":{"amount":27.68,"currency":"EUR"},"lines":[{"id":37,"productId":1,"sku":"DEV-001","name":"Dev Sample Product 1","quantity":1,"unitPrice":{"amount":5.00,"currency":"EUR"}},{"id":38,"productId":3,"sku":"DEV-003","name":"Dev Sample Product 3","quantity":2,"unitPrice":{"amount":11.34,"currency":"EUR"}}]},{"id":16,"status":"CANCELLED","placedAt":"2026-10-28T14:16:20.367298Z","total":{"amount":168.99,"currency":"EUR"},"lines":[{"id":46,"productId":10,"sku":"DEV-010","name":"Dev Sample Product 10","quantity":4,"unitPrice":{"amount":32.53,"currency":"EUR"}},{"id":47,"productId":12,"sku":"DEV-012","name":"Dev Sample Product 12","quantity":1,"unitPrice":{"amount":38.87,"currency":"EUR"}}]},{"id":19,"status":"SHIPPED","placedAt":"2026-10-25T14:16:20.367298Z","total":{"amount":186.50,"currency":"EUR"},"lines":[{"id":55,"productId":7,"sku":"DEV-007","name":"Dev Sample Product 7","quantity":3,"unitPrice":{"amount":23.02,"currency":"EUR"}},{"id":56,"productId":9,"sku":"DEV-009","name":"Dev Sample Product 9","quantity":4,"unitPrice":{"amount":29.36,"currency":"EUR"}}]},{"id":22,"status":"PAID","placedAt":"2026-10-22T14:16:20.367298Z","total":{"amount":91.57,"currency":"EUR"},"lines":[{"id":64,"productId":4,"sku":"DEV-004","name":"Dev Sample Product 4","quantity":2,"unitPrice":{"amount":14.51,"currency":"EUR"}},{"id":65,"productId":6,"sku":"DEV-006","name":"Dev Sample Product 6","quantity":3,"unitPrice":{"amount":20.85,"currency":"EUR"}}]},{"id":25,"status":"NEW","placedAt":"2026-10-19T14:16:20.367298Z","total":{"amount":27.68,"currency":"EUR"},"lines":[{"id":73,"productId":1,"sku":"DEV-001","name":"Dev Sample Product 1","quantity":1,"unitPrice":{"amount":5.00,"currency":"EUR"}},{"id":74,"productId":3,"sku":"DEV-003","name":"Dev Sample Product 3","quantity":2,"unitPrice":{"amount":11.34,"currency":"EUR"}}]},{"id":28,"status":"CANCELLED","placedAt":"2026-10-16T14:16:20.367298Z","total":{"amount":168.99,"currency":"EUR"},"lines":[{"id":82,"productId":10,"sku":"DEV-010","name":"Dev Sample Product 10","quantity":4,"unitPrice":{"amount":32.53,"currency":"EUR"}},{"id":83,"productId":12,"sku":"DEV-012","name":"Dev Sample Product 12","quantity":1,"unitPrice":{"amount":38.87,"currency":"EUR"}}]}]
```

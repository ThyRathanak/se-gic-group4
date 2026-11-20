# Task 1 – HTTP transcript

```text
$ mvn -q compile exec:java
shop-rest listening on http://localhost:8080/api/

$ curl -i localhost:8080/api/products
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 184

[{"id":1,"name":"Kettle","price":24.90,"sku":"KT-01","stock":12},{"id":2,"name":"Mug","price":6.50,"sku":"MG-01","stock":40},{"id":3,"name":"Tea","price":4.20,"sku":"TE-01","stock":0}]

$ curl -i localhost:8080/api/products/2
HTTP/1.1 200 OK
Content-Type: application/json
Content-Length: 59

{"id":2,"name":"Mug","price":6.50,"sku":"MG-01","stock":40}

$ curl -i localhost:8080/api/products/99
HTTP/1.1 404 Not Found
Content-Length: 0

$ curl -i localhost:8080/api/products/abc
HTTP/1.1 404 Not Found
Content-Length: 0

$ curl -i -X POST localhost:8080/api/products -H 'Content-Type: application/json' \
       -d '{"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}'
HTTP/1.1 201 Created
Location: http://localhost:8080/api/products/4
Content-Type: application/json

{"id":4,"name":"Teapot","price":31.00,"sku":"TP-01","stock":5}

$ curl -i -X POST localhost:8080/api/products -H 'Content-Type: application/json' \
       -d '{"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}'
HTTP/1.1 409 Conflict

$ curl -s localhost:8080/api/products/4 -H 'Accept: application/xml'
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<product id="4"><name>Teapot</name><sku>TP-01</sku><price>31.00</price><stock>5</stock></product>

$ curl -i -X PATCH localhost:8080/api/products/3/stock -H 'Content-Type: application/json' -d '{"delta":-1}'
HTTP/1.1 409 Conflict

$ curl -i -X DELETE localhost:8080/api/products/4
HTTP/1.1 204 No Content

$ curl -i -X DELETE localhost:8080/api/products/4
HTTP/1.1 404 Not Found

$ curl -i -X POST localhost:8080/api/products -H 'Content-Type: text/plain' -d 'Teapot'
HTTP/1.1 415 Unsupported Media Type
```

Run the command from the `shop-rest` directory. The `pom.xml` already configures
`edu.itc.shop.Main`, so do not add a `-Dexec.mainClass` argument. If you need to
override it in PowerShell, quote the entire Maven property argument, for example:
`mvn -q compile exec:java "-Dexec.mainClass=edu.itc.shop.Main"`.

`/products/99` matches the `{id: \d+}` template, but the id is unknown, so the resource throws
`NotFoundException` (our explicit 404). `/products/abc` matches no template at all, so Jersey returns 404 itself.
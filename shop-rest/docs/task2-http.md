# Task 2 – Full CRUD HTTP transcript

Start the application from the `shop-rest` directory:

```powershell
mvn -q compile exec:java
```

The in-memory repository starts with products 1–3. The first request creates
product 4:

```text
$ curl -i -X POST localhost:8080/api/products -H 'Content-Type: application/json' \
       -d '{"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}'
HTTP/1.1 201 Created
Location: http://localhost:8080/api/products/4
Content-Type: application/json

{"id":4,"name":"Teapot","price":31.00,"sku":"TP-01","stock":5}

$ curl -i -X POST localhost:8080/api/products -H 'Content-Type: application/json' \
       -d '{"name":"Teapot","sku":"TP-01","price":31.00,"stock":5}'
HTTP/1.1 409 Conflict

$ curl -i -X POST localhost:8080/api/products -H 'Content-Type: application/xml' \
       -H 'Accept: application/xml' \
       -d '<product><name>Teapot</name><sku>TP-XML</sku><price>29.50</price><stock>3</stock></product>'
HTTP/1.1 201 Created
Location: http://localhost:8080/api/products/5
Content-Type: application/xml

<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<product id="5"><name>Teapot</name><sku>TP-XML</sku><price>29.50</price><stock>3</stock></product>

$ curl -i localhost:8080/api/products/5 -H 'Accept: application/xml'
HTTP/1.1 200 OK
Content-Type: application/xml

<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<product id="5"><name>Teapot</name><sku>TP-XML</sku><price>29.50</price><stock>3</stock></product>

$ curl -i -X PATCH localhost:8080/api/products/3/stock -H 'Content-Type: application/json' -d '{"delta":5}'
HTTP/1.1 200 OK
Content-Type: application/json

{"id":3,"name":"Tea","price":4.20,"sku":"TE-01","stock":5}

$ curl -i -X PATCH localhost:8080/api/products/3/stock -H 'Content-Type: application/json' -d '{"delta":-6}'
HTTP/1.1 409 Conflict

$ curl -i -X DELETE localhost:8080/api/products/4
HTTP/1.1 204 No Content

$ curl -i -X DELETE localhost:8080/api/products/4
HTTP/1.1 404 Not Found

$ curl -i -X POST localhost:8080/api/products -H 'Content-Type: text/plain' -d 'Teapot'
HTTP/1.1 415 Unsupported Media Type
```

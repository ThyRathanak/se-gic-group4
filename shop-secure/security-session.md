# Task 1 – Security session

```text
$ curl -i localhost:8080/api/products
HTTP/1.1 200
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Content-Length: 158

[{"name":"USB-C cable","price":7.90,"stock":25,"id":1},{"name":"Keyboard","price":49.90,"stock":10,"id":2},{"name":"Monitor","price":199.00,"stock":4,"id":3}]

$ curl -i -X POST localhost:8080/api/products -H 'Content-Type: application/json' -d '{"name":"Mouse","price":19.9,"stock":5}'
HTTP/1.1 401
WWW-Authenticate: Basic realm="Realm"
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/problem+json;charset=UTF-8
Content-Length: 120

{"type":"about:blank","title":"Unauthorized","status":401,"detail":"Authentication required","instance":"/api/products"}

$ curl -i -u alice:alice -X DELETE localhost:8080/api/products/1
HTTP/1.1 403
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/problem+json;charset=UTF-8
Content-Length: 109

{"type":"about:blank","title":"Forbidden","status":403,"detail":"Access denied","instance":"/api/products/1"}

$ curl -i -u admin:admin -X DELETE localhost:8080/api/products/1
HTTP/1.1 204
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
```

# Task 2 – Database users and registration

The registration integration test queried the H2 `users` table after creating `bob`:

```text
ID  USERNAME  PASSWORD_HASH                                                   ENABLED
3   bob       {bcrypt}$2a$10$4AT.auNos2xOE0DMUL8h4O.cx1w9L1VKEDv6nwFP.uyvLpdvb8/RS  TRUE
```

```text
$ curl -i -X POST localhost:8080/api/register -H 'Content-Type: application/json' \
  -d '{"username":"bob","password":"Correct-Horse-9"}'
HTTP/1.1 201
{"username":"bob"}

$ curl -i -u bob:Correct-Horse-9 localhost:8080/api/me
HTTP/1.1 200
{"name":"bob","roles":["ROLE_CUSTOMER"]}
```

The checked-in seed-password values are development-only; override them with
`SHOP_SEED_ADMIN_PASSWORD` and `SHOP_SEED_CUSTOMER_PASSWORD` outside development.

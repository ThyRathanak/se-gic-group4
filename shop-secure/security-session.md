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

The application now requires `SHOP_SEED_ADMIN_PASSWORD`,
`SHOP_SEED_CUSTOMER_PASSWORD`, and `JWT_SECRET` from the environment. The
`.env.example` file lists these names without storing any values. Test-only
credentials are kept under `src/test`.

## Stored review XSS check

Using the local development server, I logged in as `alice`, posted the supplied
review, and fetched both representations. The bearer token and environment
values are omitted here.

```text
$ curl -i -X POST localhost:8080/api/reviews -H 'Authorization: Bearer <alice-token>' -H 'Content-Type: application/json' -d '{"productId":1,"stars":5,"text":"<img src=x onerror=\"document.title=\u0027pwned\u0027\">"}'
HTTP/1.1 201
X-Content-Type-Options: nosniff
Content-Type: application/json

{"productId":1,"username":"alice","stars":5,"text":"<img src=x onerror=\"document.title='pwned'\">","id":1}

$ curl -i localhost:8080/api/products/1/reviews
HTTP/1.1 200
X-Content-Type-Options: nosniff
Content-Type: application/json

[{"productId":1,"username":"alice","stars":5,"text":"<img src=x onerror=\"document.title='pwned'\">","id":1}]

$ curl -i localhost:8080/products/1/reviews.html
HTTP/1.1 200
X-Content-Type-Options: nosniff
Content-Type: text/html;charset=UTF-8

<p>&lt;img src=x onerror=&quot;document.title=&#39;pwned&#39;&quot;&gt;</p>
```

The API response is JSON, not HTML; Jackson escapes JSON syntax such as the
attribute's quote characters, while `nosniff` prevents MIME-type guessing. The
HTML template uses `th:text`, which encodes the markup so the browser displays
the submitted string as text.

The unmerged `xss-demo-unsafe` branch changes this binding to `th:utext` for
comparison. With the current Content-Security-Policy, the inline handler is
blocked as defense in depth, but the browser still interprets the submitted
markup as an image element. The safe and unsafe rendering screenshots are
`docs/images/review-thtext.png` and `docs/images/review-thutext.png`.

**Safe `th:text` output:**

![The submitted img markup displayed as literal text by Thymeleaf th:text](docs/images/review-thtext.png)

**Unsafe `th:utext` output on the unmerged branch:**

![The submitted img markup interpreted as an image element by Thymeleaf th:utext](docs/images/review-thutext.png)

## Missing JWT secret fails startup

This transcript supplied the two seed passwords but left `JWT_SECRET` unset:

```text
$ Remove-Item Env:JWT_SECRET
$ java -jar target/shop-secure-0.0.1-SNAPSHOT.jar
APPLICATION FAILED TO START

Binding to target edu.itc.shop.config.SecurityProps failed:
    Property: shop.security.jwtSecret
    Reason: size must be between 32 and 2147483647
```

`SecurityProps` is `@Validated`; its `@NotBlank` and 32-character minimum
reject a missing or too-short JWT secret during application startup.

## Security event logs

Authentication and authorization events are written through the `SECURITY`
logger. `RequestIdFilter` accepts a safe `X-Request-Id` value (or generates
one) before bearer authentication, adds it to MDC as `rid`, and returns it in
the response header.

```text
2026-11-27 16:21:09.858 WARN  [http-nio-18080-exec-5] SECURITY [rid=auth-fail-task5-final] - authentication_failure user=alice cause=BadCredentialsException
2026-11-27 16:21:09.943 INFO  [http-nio-18080-exec-6] SECURITY [rid=auth-success-task5-final] - authentication_success user=alice
2026-11-27 16:21:09.999 WARN  [http-nio-18080-exec-7] SECURITY [rid=access-denied-task5-final] - authorization_denied user=alice
```

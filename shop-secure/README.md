# shop-secure

## Task 1 – Default security and the first filter chain

### Default behaviour (no SecurityConfig yet)
Start-up log:
```text
WARN  .s.a.UserDetailsServiceAutoConfiguration :

Using generated security password: a23ce2f2-2d90-4e7b-af14-672bb24ebabf

This generated password is for development use only. Your security configuration must be updated before running your application in production.

INFO  r$InitializeUserDetailsManagerConfigurer : Global AuthenticationManager configured with UserDetailsService bean with name inMemoryUserDetailsManager
```

```text
$ curl -i localhost:8080/api/products
HTTP/1.1 401
WWW-Authenticate: Basic realm="Realm", charset="UTF-8"
X-Content-Type-Options: nosniff
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
X-Frame-Options: DENY
Content-Length: 0

$ curl -i -u user:a23ce2f2-2d90-4e7b-af14-672bb24ebabf localhost:8080/api/products
HTTP/1.1 200
Content-Type: application/json
X-Content-Type-Options: nosniff
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
X-Frame-Options: DENY

[{"name":"USB-C cable","price":7.90,"stock":25,"id":1},{"name":"Keyboard","price":49.90,"stock":10,"id":2},{"name":"Monitor","price":199.00,"stock":4,"id":3}]
```

**Which auto-configuration did this?** The application defined no `UserDetailsService`, so Spring Boot's
`UserDetailsServiceAutoConfiguration` created an `InMemoryUserDetailsManager` with one user `user` and a random
UUID password, printed in the log. `SecurityAutoConfiguration` (`SpringBootWebSecurityConfiguration`) then
registered a default `SecurityFilterChain` that requires authentication for **every** request, with form login and
HTTP Basic enabled, plus the default security headers (`X-Frame-Options`, `Cache-Control`, ...).
Note: because the OAuth2 resource-server starter is on the classpath, `UserDetailsServiceAutoConfiguration`
backs off by default (see `--debug`); I started the app with `--spring.security.user.name=user` to make it create
the in-memory user and print the generated password.


### Explicit SecurityFilterChain (`config/SecurityConfig`)
One `SecurityFilterChain` bean, lambda DSL only:
- `GET /api/products/**` → `permitAll()` (public catalogue)
- `DELETE /api/products/**` → `hasRole("ADMIN")`
- every other `/api/**` → `authenticated()` (HTTP Basic for now)
- `anyRequest()` → `denyAll()` (last rule)

Form login is disabled and sessions are `STATELESS`. CSRF is disabled because the chain accepts no
cookie-based credentials (only Basic / Bearer headers). Two temporary in-memory users (`admin/admin` ADMIN,
`alice/alice` CUSTOMER) are replaced in Task 2. The `authenticationEntryPoint` (401) and the
`accessDeniedHandler` (403) write `application/problem+json`; `web/ApiExceptionHandler` returns `ProblemDetail`
for not-found and validation errors. Transcript: `security-session.md`.
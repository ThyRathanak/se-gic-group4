# CSRF strategy

## Current API chain

`SecurityConfig.api` is the only filter chain. It is stateless and accepts JWT
credentials in an `Authorization: Bearer ...` header through the OAuth2 resource
server. HTTP Basic and form login are disabled, and the chain does not use a
session cookie. `/api/auth/**`, `POST /api/register`, and `GET /api/products/**`
are public; other `/api/**` routes require a valid Bearer token. The login
endpoint accepts a username and password in its request body and returns a
token; it does not authenticate later requests with a cookie.

CSRF protection is disabled because browsers do not attach an explicitly set
Bearer header automatically to cross-site requests. That decision depends on
clients keeping the token out of automatically sent cookies. If authentication
later moves to a session cookie, CSRF protection must be enabled for
state-changing requests because browsers attach that cookie automatically.

## If a same-origin SPA uses a session cookie

Configure the chain with Spring Security's cookie token repository:

```java
.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
```

The repository writes the token to the `XSRF-TOKEN` cookie so same-origin
JavaScript can read it. Send that value in the `X-XSRF-TOKEN` header on
state-changing requests:

```javascript
const xsrfToken = decodeURIComponent(
  document.cookie.match(/(?:^|; )XSRF-TOKEN=([^;]*)/)?.[1] ?? ""
);

await fetch("/api/orders", {
  method: "POST",
  credentials: "same-origin",
  headers: {
    "Content-Type": "application/json",
    "X-XSRF-TOKEN": xsrfToken
  },
  body: JSON.stringify({ productId: 1, quantity: 1 })
});
```

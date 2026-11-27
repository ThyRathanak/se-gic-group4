# Lab-05: Security in Java Web Applications

## Project: `shop-secure`

**Course:** Software Engineering  
**Topics:** Spring Security, Authentication, Authorization, JWT, CORS, CSRF, XSS  
**Framework:** Spring Boot 4.1.1  
**Security:** Spring Security 7.1.0  
**Java:** JDK 25  
**Build Tool:** Maven 3.9

---

# 1. Introduction

This lab focuses on securing a Java web application using Spring Security and JWT (JSON Web Token).

The project `shop-secure` is a product catalogue application. Throughout this lab, security features are added step by step to protect API endpoints, manage users, control access based on roles, and secure sensitive information.

### Objectives

- Understand Spring Security's default security configuration.
- Configure authentication and authorization using `SecurityFilterChain`.
- Store users and encrypted passwords in a database.
- Implement role-based access control.
- Protect service methods using method-level security.
- Implement JWT authentication for stateless APIs.
- Configure CSRF, CORS, and security headers.
- Prevent stored Cross-Site Scripting (XSS).
- Protect application secrets and log security events.

### Project Structure

```text
shop-secure/
├── src/
│   ├── main/
│   │   ├── java/edu/itc/shop/
│   │   │   ├── config/
│   │   │   ├── security/
│   │   │   ├── web/
│   │   │   │   └── dto/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   └── domain/
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── docs/
├── security-session.md
├── .env.example
├── .gitignore
├── pom.xml
└── README.md
```

---

# 2. Task 1 – Default Security and the First Filter Chain

## 2.1 Default Behaviour (No SecurityConfig Yet)

When the application starts without a custom `SecurityConfig`, Spring Security automatically protects the application.

Startup log:

```text
WARN  .s.a.UserDetailsServiceAutoConfiguration :

Using generated security password: a23ce2f2-2d90-4e7b-af14-672bb24ebabf

This generated password is for development use only.
Your security configuration must be updated before running
your application in production.

INFO  r$InitializeUserDetailsManagerConfigurer :
Global AuthenticationManager configured with UserDetailsService
bean with name inMemoryUserDetailsManager
```

### Test without authentication

```bash
curl -i localhost:8080/api/products
```

Response:

```http
HTTP/1.1 401
WWW-Authenticate: Basic realm="Realm", charset="UTF-8"
X-Content-Type-Options: nosniff
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
X-Frame-Options: DENY
Content-Length: 0
```

The application returns **401 Unauthorized** because the request does not provide authentication credentials.

### Test with authentication

```bash
curl -i -u user:a23ce2f2-2d90-4e7b-af14-672bb24ebabf localhost:8080/api/products
```

Response:

```http
HTTP/1.1 200
Content-Type: application/json
```

```json
[
  {
    "name": "USB-C cable",
    "price": 7.9,
    "stock": 25,
    "id": 1
  },
  {
    "name": "Keyboard",
    "price": 49.9,
    "stock": 10,
    "id": 2
  },
  {
    "name": "Monitor",
    "price": 199.0,
    "stock": 4,
    "id": 3
  }
]
```

### Which auto-configuration produced this behaviour?

Spring Boot uses the following auto-configurations:

**1. UserDetailsServiceAutoConfiguration**

- Creates an `InMemoryUserDetailsManager`.
- Creates a default user named `user`.
- Generates a random UUID password.
- Prints the generated password in the startup log.

**2. SecurityAutoConfiguration**

- Registers the default security configuration.
- Protects every request.
- Enables form login and HTTP Basic authentication.
- Adds default security headers.

Note: Because the OAuth2 resource-server starter is on the classpath, `UserDetailsServiceAutoConfiguration` backs off by default. I used `--spring.security.user.name=user` to make Spring Boot create the in-memory user and print the generated password.

## 2.2 Explicit SecurityFilterChain

After observing the default behaviour, I created:

`config/SecurityConfig.java`

The configuration uses one `SecurityFilterChain` bean with the lambda DSL.

### Authorization rules

| HTTP Request              | Permission          |
| ------------------------- | ------------------- |
| GET `/api/products/**`    | Public access       |
| DELETE `/api/products/**` | ADMIN only          |
| Other `/api/**`           | Authenticated users |
| Any other request         | Denied              |

### Temporary users

| Username | Password | Role     |
| -------- | -------- | -------- |
| admin    | admin    | ADMIN    |
| alice    | alice    | CUSTOMER |

These temporary users are used only in Task 1 and are replaced with database users in Task 2.

### Security configuration features

- `permitAll()` allows public product catalogue access.
- `hasRole("ADMIN")` restricts product deletion.
- `authenticated()` requires login.
- `denyAll()` blocks unmatched requests.
- HTTP Basic is enabled temporarily.
- Form login is disabled.
- Session management uses `STATELESS`.
- CSRF is disabled because the API accepts only Basic/Bearer authorization headers, not cookie-based credentials.

### Error handling

The application uses custom exception handling:

- **401 Unauthorized:** Authentication is required.
- **403 Forbidden:** The user is authenticated but does not have permission.
- **ProblemDetail:** Provides a consistent error response using `application/problem+json`.

`web/ApiExceptionHandler` also handles not-found and validation errors.

### Verification

| Test                    | Expected HTTP Status |
| ----------------------- | -------------------: |
| Anonymous GET products  |                  200 |
| Anonymous POST products |                  401 |
| CUSTOMER DELETE product |                  403 |
| ADMIN DELETE product    |                  204 |

All curl transcripts are recorded in:

`security-session.md`

**Git tag:** `task-1`

---

# 3. Task 2 – Users and Passwords in the Database

## 3.1 Objective

Replace temporary in-memory users with database users using Spring Data JPA and BCrypt password hashing.

## 3.2 Implementation

Create the following components:

| Component                          | Responsibility                   |
| ---------------------------------- | -------------------------------- |
| `domain/Role`                      | Defines CUSTOMER and ADMIN roles |
| `domain/UserAccount`               | Stores user information          |
| `repository/UserAccountRepository` | Accesses user records            |
| `config/PasswordConfig`            | Configures PasswordEncoder       |
| `security/JpaUserDetailsService`   | Loads users from the database    |
| `service/AccountService`           | Handles registration             |
| `web/RegisterController`           | Provides registration API        |

### Password encryption

Use:

```java
PasswordEncoderFactories.createDelegatingPasswordEncoder()
```

Passwords are stored as BCrypt hashes instead of plain text.

Example:

```text
{bcrypt}$2a$10$...
```

The application must never return or log the original password or password hash.

### User registration

Endpoint:

```http
POST /api/register
```

Username validation:

- Minimum 3 characters.
- Maximum 40 characters.
- Only lowercase letters, digits, underscores and dots.

Password validation:

- Minimum 12 characters.
- Maximum 72 characters.
- Must contain uppercase letters.
- Must contain lowercase letters.
- Must contain at least one digit.

### Test invalid registration

```bash
curl -i -X POST localhost:8080/api/register \
-H 'Content-Type: application/json' \
-d '{"username":"bob","password":"short"}'
```

Expected:

```http
HTTP/1.1 400
Content-Type: application/problem+json
```

### Test successful registration

```bash
curl -i -X POST localhost:8080/api/register \
-H 'Content-Type: application/json' \
-d '{"username":"bob","password":"Correct-Horse-9"}'
```

Expected:

```http
HTTP/1.1 201
```

```json
{
  "username": "bob"
}
```

### Verify authentication

```bash
curl -i -u bob:Correct-Horse-9 localhost:8080/api/me
```

Expected:

```http
HTTP/1.1 200
```

```json
{
  "name": "bob",
  "roles": ["ROLE_CUSTOMER"]
}
```

### Database verification

The users table should store encrypted passwords.

Example:

| ID  | USERNAME | PASSWORD_HASH        | ENABLED |
| --- | -------- | -------------------- | ------- |
| 3   | bob      | `{bcrypt}$2a$10$...` | TRUE    |

The application also seeds two default development users:

- admin: ADMIN and CUSTOMER
- alice: CUSTOMER

Seed passwords are configured through application properties rather than hardcoded credentials.

**Git tag:** `task-2`

---

# 4. Task 3 – Roles and Method Security

## 4.1 Objective

Implement role-based authorization and protect business operations using Spring Security method-level annotations.

## 4.2 Order Management

Create an `Order` entity with the following information:

| Field     | Description       |
| --------- | ----------------- |
| id        | Order ID          |
| customer  | Customer username |
| productId | Product ID        |
| quantity  | Order quantity    |
| status    | Order status      |

Order statuses:

- NEW
- SHIPPED
- CANCELLED

Create:

- `OrderRepository`
- `OrderService`
- `OrderController`
- `MethodSecurityConfig`

## 4.3 Method Security Rules

Enable method security using:

```java
@EnableMethodSecurity
```

Configure a role hierarchy where ADMIN implies CUSTOMER.

The five service methods use these security rules:

| Method    | Security Rule                            |
| --------- | ---------------------------------------- |
| place()   | Customer can place orders for themselves |
| history() | ADMIN or order owner                     |
| get()     | ADMIN or order owner                     |
| ship()    | ADMIN only                               |
| all()     | ADMIN only                               |

### Important annotations

**@PreAuthorize**

Checks authorization before executing a method.

Example:

```java
@PreAuthorize("#customerId == authentication.name")
public Order place(String customerId, PlaceOrder cmd) {
    // Create order
}
```

**@PostAuthorize**

Checks authorization after the method executes.

Example:

```java
@PostAuthorize(
    "hasRole('ADMIN') or returnObject.customer == authentication.name"
)
```

This protects individual orders by checking their owner.

## 4.4 Testing

The customer identity must come from the authenticated user, not from the request body.

Expected behaviour:

| User  | Action             | Result  |
| ----- | ------------------ | ------- |
| alice | Read own history   | Allowed |
| alice | Read bob's history | 403     |
| admin | Read bob's history | Allowed |
| alice | Ship an order      | 403     |
| admin | Ship an order      | Allowed |

### Test order creation

```bash
curl -i -u alice:Alice-pass-123 \
-X POST localhost:8080/api/orders \
-H 'Content-Type: application/json' \
-d '{"productId":2,"quantity":1}'
```

Expected:

```http
HTTP/1.1 201
```

### Customer tries to access another customer's order

```bash
curl -i -u bob:Correct-Horse-9 \
localhost:8080/api/orders/1
```

Expected:

```http
HTTP/1.1 403
```

### Admin accesses the order

```bash
curl -i -u admin:Admin-pass-123 \
localhost:8080/api/orders/1
```

Expected:

```http
HTTP/1.1 200
```

### Automated tests

Run:

```bash
./mvnw -q test -Dtest='Order*SecurityTest'
```

Expected:

```text
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
```

The tests use `@WithMockUser` to verify security rules.

**Git tag:** `task-3`

---

# 5. Task 4 – JWT Login and a Stateless API

## 5.1 Objective

Replace HTTP Basic authentication with JWT authentication.

JWT allows users to log in once and receive a signed token. They can then send that token with every protected API request.

## 5.2 JWT Configuration

Create:

- `config/SecurityProps`
- `config/JwtConfig`
- `web/AuthController`
- `web/dto/LoginRequest`
- `web/dto/TokenResponse`

Generate a secret using:

```bash
export JWT_SECRET=$(openssl rand -base64 48)
```

The secret must not be committed to Git.

### JWT settings

| Property   | Value            |
| ---------- | ---------------- |
| Algorithm  | HS256            |
| Issuer     | shop-api         |
| Token type | Bearer           |
| Expiration | 15 minutes       |
| Subject    | Username         |
| Roles      | User authorities |
| JWT ID     | Random UUID      |

The application uses:

- `NimbusJwtEncoder` to create tokens.
- `NimbusJwtDecoder` to validate tokens.
- `JwtAuthenticationConverter` to map roles.
- `DaoAuthenticationProvider` to authenticate usernames and passwords.

## 5.3 Login Endpoint

Endpoint:

```http
POST /api/auth/login
```

Example:

```bash
curl -s -X POST localhost:8080/api/auth/login \
-H 'Content-Type: application/json' \
-d '{"username":"alice","password":"Alice-pass-123"}'
```

Expected response structure:

```json
{
  "token": "JWT_TOKEN",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

## 5.4 Access Protected APIs

Save the token:

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login \
-H 'Content-Type: application/json' \
-d '{"username":"alice","password":"Alice-pass-123"}' | jq -r .token)
```

Access the orders endpoint:

```bash
curl -i localhost:8080/api/orders \
-H "Authorization: Bearer $TOKEN"
```

Expected:

```http
HTTP/1.1 200
```

### Invalid or expired token

An invalid or expired token must return:

```http
HTTP/1.1 401
Content-Type: application/problem+json
```

The application must not reveal sensitive authentication details.

### Important security features

- HTTP Basic is removed.
- Authentication uses Bearer tokens.
- The API remains stateless.
- No session cookie is created.
- JWT expiration is validated.
- Roles are extracted from the JWT claims.
- Invalid tokens return 401.

### JWT payload

Decode the JWT and record its header and payload in `security-session.md`.

The JWT payload is readable because it is Base64URL-encoded, not encrypted. This is acceptable because sensitive information such as passwords and secrets must never be included in the token.

### Automated testing

Create `AuthFlowIT` to verify:

1. Login returns a JWT.
2. A valid token accesses protected endpoints.
3. An expired token returns 401.
4. Invalid credentials return 401.

**Git tag:** `task-4`

---

# 6. Task 5 – Security Hardening

## 6.1 Objective

Improve application security by configuring CSRF, CORS, security headers, XSS protection, environment secrets and security event logging.

## 6.2 CSRF Strategy

CSRF (Cross-Site Request Forgery) is an attack where an attacker tricks an authenticated browser into sending unwanted requests.

For this API, CSRF is disabled because authentication uses Bearer tokens in the Authorization header and does not use cookie-based credentials.

Create:

```text
docs/csrf.md
```

The document should explain:

- Which credentials each filter chain accepts.
- Why CSRF is disabled for the Bearer-only API.
- How to enable CSRF if a session-cookie-based SPA is introduced.
- How to use `CookieCsrfTokenRepository.withHttpOnlyFalse()`.

Example frontend request:

```javascript
fetch("/api/orders", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    "X-XSRF-TOKEN": csrfToken,
  },
  credentials: "same-origin",
  body: JSON.stringify(order),
});
```

## 6.3 CORS Configuration

CORS (Cross-Origin Resource Sharing) controls which external origins can access the API.

Configure the following:

| Setting         | Configuration               |
| --------------- | --------------------------- |
| Allowed origin  | http://localhost:5173       |
| Allowed methods | GET, POST, PUT, DELETE      |
| Allowed headers | Authorization, Content-Type |
| Exposed headers | Location                    |
| Max age         | 3600 seconds                |

Only the specified frontend origin should be allowed.

### Preflight test

```bash
curl -i -X OPTIONS localhost:8080/api/orders \
-H 'Origin: http://localhost:5173' \
-H 'Access-Control-Request-Method: POST' \
-H 'Access-Control-Request-Headers: authorization,content-type'
```

Expected:

```http
HTTP/1.1 200
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Methods: GET,POST,PUT,DELETE
Access-Control-Allow-Headers: authorization, content-type
Access-Control-Max-Age: 3600
Vary: Origin
```

An origin such as `http://evil.example` must not receive an `Access-Control-Allow-Origin` header.

## 6.4 Security Headers

Configure the following security headers:

| Header                          | Purpose                           |
| ------------------------------- | --------------------------------- |
| X-Content-Type-Options: nosniff | Prevents MIME-type sniffing       |
| X-Frame-Options: DENY           | Prevents framing                  |
| Strict-Transport-Security       | Enforces HTTPS                    |
| Content-Security-Policy         | Restricts allowed content sources |
| Referrer-Policy                 | Controls referrer information     |

CSP configuration:

```text
default-src 'self';
frame-ancestors 'none';
object-src 'none'
```

HSTS configuration:

```text
max-age=31536000; includeSubDomains
```

### Verify headers

```bash
curl -I localhost:8080/api/products
```

The response should include the configured security headers.

## 6.5 Stored XSS Protection

XSS (Cross-Site Scripting) is a security vulnerability where attackers inject malicious scripts into a web application.

Create a Review entity with:

- id
- productId
- author
- stars
- text

Create:

```text
web/ReviewController
web/dto/ReviewRequest
domain/Review
service/ReviewService
repository/ReviewRepository
```

Endpoints:

| Method | Endpoint                    | Permission    |
| ------ | --------------------------- | ------------- |
| POST   | /api/reviews                | Authenticated |
| GET    | /api/products/{id}/reviews  | Public        |
| GET    | /products/{id}/reviews.html | Public        |

### XSS test payload

```html
<img src="x" onerror="document.title='pwned'" />
```

Post the review as an authenticated user.

The application must safely return and render the review.

### Safe HTML rendering

Use Thymeleaf:

```html
<p th:text="${review.text}"></p>
```

`th:text` escapes HTML content so the browser displays the text instead of executing the injected HTML.

Avoid:

```html
<p th:utext="${review.text}"></p>
```

`th:utext` renders unescaped HTML and can allow malicious scripts to execute.

The lab also requires a separate demonstration branch showing the unsafe `th:utext` behaviour.

## 6.6 Secrets Management

Sensitive information must not be stored directly in source code.

Use environment variables:

```text
JWT_SECRET
SHOP_SEED_ADMIN_PASSWORD
SHOP_SEED_CUSTOMER_PASSWORD
```

Create:

```text
.env.example
```

Example:

```env
JWT_SECRET=
SHOP_SEED_ADMIN_PASSWORD=
SHOP_SEED_CUSTOMER_PASSWORD=
```

Add `.env` to `.gitignore`.

Search for possible hardcoded secrets:

```bash
git grep -n -i -E "password|secret" -- '*.yml' '*.properties' '*.java'
```

The application should fail at startup if `JWT_SECRET` is missing or blank.

## 6.7 Security Event Logging

Create:

```text
security/SecurityEventLogger.java
security/RequestIdFilter.java
```

Log the following events:

1. Authentication success.
2. Authentication failure.
3. Authorization denied.

Use a logger named:

```java
LoggerFactory.getLogger("SECURITY");
```

Add a request ID using `OncePerRequestFilter` and MDC.

The request ID should be included in the logging pattern:

```text
%X{rid}
```

Example logs:

```text
WARN  rid=7f3c9a12 SECURITY : login.failed user=alice reason=BadCredentialsException

INFO  rid=8a10c0de SECURITY : login.ok user=alice type=UsernamePasswordAuthenticationToken

WARN  rid=91be77f0 SECURITY : access.denied user=alice object=OrderService.ship(1)
```

### Final verification

Run:

```bash
./mvnw -q verify
```

The application should build successfully from a clean clone using environment variables.

**Git tag:** `task-5`

---

# 7. Challenge – Advanced Security

The final challenge extends the application with additional security features.

Required topics:

1. Refresh-token rotation.
2. API rate limiting.
3. Account lockout.
4. Security audit log.
5. OWASP security checklist.

These features improve protection against token misuse, brute-force attacks and unauthorized activities.

---

# 8. Important Security Concepts

| Concept             | Explanation                                                       |
| ------------------- | ----------------------------------------------------------------- |
| Authentication      | Verifies who the user is                                          |
| Authorization       | Determines what the user can access                               |
| Spring Security     | Framework for application security                                |
| SecurityFilterChain | Defines security rules for HTTP requests                          |
| BCrypt              | Password hashing algorithm                                        |
| Role-based access   | Controls access according to user roles                           |
| JWT                 | Signed token used for authentication                              |
| Stateless API       | Server does not maintain a login session                          |
| CSRF                | Attack that tricks a user's browser into making unwanted requests |
| CORS                | Controls cross-origin browser requests                            |
| XSS                 | Attack involving injected malicious scripts                       |
| Security Headers    | HTTP headers that improve browser security                        |

---

# 9. Git Commands and Submission

Commit after completing each task so the grader can review the progress.

```bash
git add .
git commit -m "Complete Task 1 - Default Security"
git tag task-1
```

Repeat for the remaining tasks:

```bash
git tag task-2
git tag task-3
git tag task-4
git tag task-5
```

Check tags:

```bash
git tag
```

Push commits and tags:

```bash
git push origin HEAD
git push origin --tags
```

---

# 10. Final Verification Checklist

- [x] Task 1: Default security and SecurityFilterChain.
- [x] Task 2: Database users and BCrypt passwords.
- [x] Task 3: Roles and method security.
- [x] Task 4: JWT login and stateless API.
- [x] Task 5: CSRF, CORS, headers, XSS and secrets.
- [x] Challenge: Refresh tokens, rate limiting, lockout and audit logging.
- [x] All curl transcripts saved in `security-session.md`.
- [x] Automated tests pass.
- [x] Git tags created for every task.
- [x] `./mvnw -q verify` succeeds from a clean clone.

---

# 11. Conclusion

In this lab, the `shop-secure` application is developed step by step with Spring Security.

Starting from the default security configuration, the application is extended with database authentication, BCrypt password hashing, role-based authorization and JWT authentication.

Finally, additional security measures such as CORS, CSRF strategy, security headers, XSS prevention, environment-based secrets and security event logging are introduced.

These techniques help build a more secure Java web application and provide practical experience with modern web application security.

package edu.itc.shop;

import edu.itc.shop.domain.UserAccount;
import edu.itc.shop.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "shop.seed.admin-password=AdminPass123",
                "shop.seed.customer-password=CustomerPass123"
        })
class ShopSecureApplicationTests {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UserAccountRepository users;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void registrationValidatesPersistsBcryptAndAuthenticatesWithCustomerRole() throws Exception {
        HttpResponse<String> invalid = post("/api/register", "{\"username\":\"bob\",\"password\":\"short\"}");
        assertEquals(400, invalid.statusCode());
        assertTrue(invalid.headers().firstValue("Content-Type").orElse("")
                .startsWith("application/problem+json"));
        assertTrue(invalid.body().contains("\"errors\":["));
        assertTrue(invalid.body().contains(
                "\"field\":\"password\",\"message\":\"size must be between 12 and 72\""));
        assertTrue(invalid.body().contains(
                "\"field\":\"password\",\"message\":\"must contain an upper-case letter, a lower-case letter and a digit\""));

        String requestBody = "{\"username\":\"bob\",\"password\":\"Correct-Horse-9\"}";
        HttpResponse<String> registered = post("/api/register", requestBody);
        assertEquals(201, registered.statusCode());
        assertEquals("{\"username\":\"bob\"}", registered.body());

        String hash = jdbc.queryForObject(
                "select password_hash from users where username = ?", String.class, "bob");
        assertTrue(hash.matches("\\{bcrypt}\\$2a\\$10\\$.*"));
        UserAccount account = users.findByUsername("bob").orElseThrow();
        assertFalse(account.toString().contains(hash));

        HttpResponse<String> duplicate = post("/api/register", requestBody);
        assertEquals(409, duplicate.statusCode());

        HttpResponse<String> me = get("/api/me", "bob", "Correct-Horse-9");
        assertEquals(200, me.statusCode());
        assertTrue(me.body().contains("\"name\":\"bob\""));
        assertTrue(me.body().contains("\"roles\":[\"ROLE_CUSTOMER\"]"), me.body());

        HttpResponse<String> admin = get("/api/me", "admin", "AdminPass123");
        assertEquals(200, admin.statusCode());
        assertTrue(admin.body().contains("\"roles\":[\"ROLE_ADMIN\",\"ROLE_CUSTOMER\"]"), admin.body());
    }

    @Test
    void seedsAdminAndCustomerAccounts() {
        assertTrue(Boolean.TRUE.equals(jdbc.queryForObject(
                "select enabled from users where username = ?", Boolean.class, "admin")));
        assertTrue(Boolean.TRUE.equals(jdbc.queryForObject(
                "select enabled from users where username = ?", Boolean.class, "alice")));
        assertEquals(2, jdbc.queryForObject(
                "select count(*) from user_roles where user_id = "
                        + "(select id from users where username = 'admin')", Integer.class));
    }

    private HttpResponse<String> post(String path, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path, String username, String password) throws Exception {
        String credentials = Base64.getEncoder().encodeToString(
                (username + ":" + password).getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Authorization", "Basic " + credentials)
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}

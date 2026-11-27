
package edu.itc.shop;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.context.TestPropertySource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "JWT_SECRET=012345678901234567890123456789012345678901234567",
        "spring.profiles.active=dev"
})
class AuthFlowIT {

    @LocalServerPort
    int port;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtEncoder jwtEncoder;

    private final HttpClient client = HttpClient.newHttpClient();

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void loginThenAccessOrdersWithJwt() throws Exception {

        // 1. Login as Alice.
        String loginJson = """
                {
                    "username": "alice",
                    "password": "TestCustomerPassword-123"
                }
                """;

        HttpRequest loginRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                .build();

        HttpResponse<String> loginResponse =
                client.send(loginRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, loginResponse.statusCode());

        // 2. Read the returned token.
        JsonNode responseBody = objectMapper.readTree(loginResponse.body());

        String token = responseBody.get("token").asText();

        assertFalse(token.isBlank());
        assertEquals("Bearer", responseBody.get("tokenType").asText());
        assertEquals(900, responseBody.get("expiresIn").asInt());

        // 3. Use the token to access protected orders.
        HttpRequest ordersRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "/api/orders"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> ordersResponse =
                client.send(ordersRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, ordersResponse.statusCode());
    }

    @Test
    void expiredTokenIsRejected() throws Exception {

        // Create a correctly signed token that expired one minute ago.
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("shop-api")
                .subject("alice")
                .issuedAt(now.minusSeconds(120))
                .expiresAt(now.minusSeconds(60))
                .id("expired-test-token")
                .claim("roles", List.of("ROLE_CUSTOMER"))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        String expiredToken = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

        // Send the expired token to a protected endpoint.
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl() + "/api/orders"))
                .header("Authorization", "Bearer " + expiredToken)
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(401, response.statusCode());
        assertTrue(response.body().contains("expired"));
    }
}

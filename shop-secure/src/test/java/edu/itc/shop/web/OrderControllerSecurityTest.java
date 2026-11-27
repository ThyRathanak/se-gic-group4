
package edu.itc.shop.web;

import edu.itc.shop.config.MethodSecurityConfig;
import edu.itc.shop.config.SecurityConfig;
import edu.itc.shop.repository.ProductRepository;
import edu.itc.shop.repository.UserAccountRepository;
import edu.itc.shop.service.AccountService;
import edu.itc.shop.service.OrderService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import({SecurityConfig.class, MethodSecurityConfig.class})
class OrderControllerSecurityTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    OrderService orders;

    @MockitoBean
    ProductRepository products;

    @MockitoBean
    UserAccountRepository users;

    @MockitoBean
    AccountService accounts;

    // Required by the JWT resource-server configuration.
    @MockitoBean
    JwtDecoder jwtDecoder;

    @MockitoBean
    JwtAuthenticationConverter jwtAuthenticationConverter;

    @Test
    void anonymousUserCannotShip() throws Exception {
        mvc.perform(post("/api/orders/1/ship"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void customerCannotShip() throws Exception {
        mvc.perform(post("/api/orders/1/ship")
                        .with(jwt()
                                .jwt(token -> token
                                        .subject("alice")
                                        .claim("roles", List.of("ROLE_CUSTOMER")))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanShip() throws Exception {
        mvc.perform(post("/api/orders/1/ship")
                        .with(jwt()
                                .jwt(token -> token
                                        .subject("admin")
                                        .claim("roles", List.of("ROLE_ADMIN")))
                                .authorities(
                                        new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());

        verify(orders).ship(1L);
    }

    @Test
    void malformedBearerTokenReturnsProblemJson() throws Exception {
        when(jwtDecoder.decode(anyString()))
                .thenThrow(new BadJwtException("Malformed token"));

        mvc.perform(post("/api/orders/1/ship")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string(
                        "WWW-Authenticate",
                        org.hamcrest.Matchers.containsString("invalid_token")));
    }
}

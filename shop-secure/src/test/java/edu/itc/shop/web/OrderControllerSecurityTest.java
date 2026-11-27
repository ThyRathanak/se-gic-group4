package edu.itc.shop.web;

import edu.itc.shop.repository.ProductRepository;
import edu.itc.shop.repository.UserAccountRepository;
import edu.itc.shop.service.AccountService;
import edu.itc.shop.config.SecurityConfig;
import edu.itc.shop.config.MethodSecurityConfig;
import edu.itc.shop.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    void anonymousUserCannotShip() throws Exception {
        mvc.perform(post("/api/orders/1/ship"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "alice", roles = "CUSTOMER")
    void customerCannotShip() throws Exception {
        mvc.perform(post("/api/orders/1/ship"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanShip() throws Exception {
        mvc.perform(post("/api/orders/1/ship"))
                .andExpect(status().isNoContent());

        verify(orders).ship(1L);
    }
}
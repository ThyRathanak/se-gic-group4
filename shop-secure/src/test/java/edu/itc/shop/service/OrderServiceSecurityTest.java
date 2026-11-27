package edu.itc.shop.service;

import edu.itc.shop.repository.OrderRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest
class OrderServiceSecurityTest {

    @Autowired
    OrderService service;

    @MockitoBean
    OrderRepository orders;

    @Test
    @WithMockUser(username = "alice", roles = "CUSTOMER")
    void aliceCanReadHerOwnHistory() {
        when(orders.findByCustomer("alice"))
                .thenReturn(List.of());

        assertDoesNotThrow(() -> service.history("alice"));

        verify(orders).findByCustomer("alice");
    }

    @Test
    @WithMockUser(username = "alice", roles = "CUSTOMER")
    void aliceCannotReadBobsHistory() {
        assertThrows(
                AccessDeniedException.class,
                () -> service.history("bob")
        );

        verifyNoInteractions(orders);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanReadBobsHistory() {
        when(orders.findByCustomer("bob"))
                .thenReturn(List.of());

        assertDoesNotThrow(() -> service.history("bob"));

        verify(orders).findByCustomer("bob");
    }

    @Test
    @WithMockUser(username = "alice", roles = "CUSTOMER")
    void aliceCannotShipOrders() {
        assertThrows(
                AccessDeniedException.class,
                () -> service.ship(1L)
        );

        verifyNoInteractions(orders);
    }

    @Test
    void noUserCannotAccessProtectedMethods() {
        assertThrows(
                AuthenticationCredentialsNotFoundException.class,
                () -> service.history("alice")
        );

        verifyNoInteractions(orders);
    }
}
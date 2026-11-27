package edu.itc.shop.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventLogger {

    private static final Logger SECURITY = LoggerFactory.getLogger("SECURITY");

    @EventListener
    public void authenticationSucceeded(AuthenticationSuccessEvent event) {
        SECURITY.info("authentication_success user={}", event.getAuthentication().getName());
    }

    @EventListener
    public void authenticationFailed(AbstractAuthenticationFailureEvent event) {
        SECURITY.warn("authentication_failure user={} cause={}",
                event.getAuthentication().getName(),
                event.getException().getClass().getSimpleName());
    }

    @EventListener
    public void authorizationDenied(AuthorizationDeniedEvent<?> event) {
        String username = event.getAuthentication().get().getName();
        SECURITY.warn("authorization_denied user={}", username);
    }
}


package edu.itc.shop.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.config.Customizer;
import edu.itc.shop.security.RequestIdFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain api(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/products/*/reviews.html").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().denyAll())

                // Remove HTTP Basic and form login.
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())

                // This API uses Bearer tokens, not cookie-based authentication.
                .csrf(csrf -> csrf.disable())

                .cors(Customizer.withDefaults())

                // Do not create HTTP sessions.
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31_536_000))
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; frame-ancestors 'none'; object-src 'none'"))
                        .referrerPolicy(referrer -> referrer.policy(
                                ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(SecurityConfig::unauthorized)
                        .accessDeniedHandler(SecurityConfig::forbidden))

                // JWT resource server.
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(SecurityConfig::invalidBearerToken))

                .addFilterBefore(new RequestIdFilter(), BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    /** 401: No valid authentication was provided. */
    private static void unauthorized(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException ex
    ) throws IOException {

        response.setHeader("WWW-Authenticate", "Bearer");
        writeProblem(request, response, 401,
                "Unauthorized", "Authentication required");
    }

    /** 401: A supplied Bearer token is invalid or expired. */
    private static void invalidBearerToken(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException ex
    ) throws IOException {

        response.setHeader(
                "WWW-Authenticate",
                "Bearer error=\"invalid_token\""
        );

        writeProblem(request, response, 401,
                "Unauthorized", "Invalid or expired access token");
    }

    /** 403: Authenticated, but not permitted to access the resource. */
    private static void forbidden(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException ex
    ) throws IOException {

        writeProblem(request, response, 403,
                "Forbidden", "Access denied");
    }

    private static void writeProblem(
            HttpServletRequest request,
            HttpServletResponse response,
            int status,
            String title,
            String detail
    ) throws IOException {

        response.setStatus(status);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                "{\"type\":\"about:blank\",\"title\":\"%s\",\"status\":%d,\"detail\":\"%s\",\"instance\":\"%s\"}"
                        .formatted(title, status, detail,
                                request.getRequestURI())
        );
    }
}

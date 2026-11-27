package edu.itc.shop.web;

import edu.itc.shop.config.SecurityProps;
import edu.itc.shop.web.dto.LoginRequest;
import edu.itc.shop.web.dto.TokenResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtEncoder encoder;
    private final SecurityProps props;

    public AuthController(
            AuthenticationManager authManager,
            JwtEncoder encoder,
            SecurityProps props
    ) {
        this.authManager = authManager;
        this.encoder = encoder;
        this.props = props;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest req) {

        try {
            Authentication auth = authManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            req.username(),
                            req.password()
                    )
            );

            Instant now = Instant.now();

            List<String> authorities = auth.getAuthorities()
                    .stream()
                    .map(authority -> authority.getAuthority())
                    .toList();

            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer("shop-api")
                    .subject(auth.getName())
                    .issuedAt(now)
                    .expiresAt(now.plus(props.accessTokenTtl()))
                    .id(UUID.randomUUID().toString())
                    .claim("roles", authorities)
                    .build();

            JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

            String token = encoder.encode(
                    JwtEncoderParameters.from(header, claims)
            ).getTokenValue();

            return ResponseEntity.ok(
                    new TokenResponse(
                            token,
                            "Bearer",
                            props.accessTokenTtl().toSeconds()
                    )
            );

        } catch (AuthenticationException ex) {

            ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.UNAUTHORIZED,
                    "Bad credentials"
            );

            problem.setTitle("Unauthorized");
            problem.setInstance(java.net.URI.create("/api/auth/login"));

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .body(problem);
        }
    }
}
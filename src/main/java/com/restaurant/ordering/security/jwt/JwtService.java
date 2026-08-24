package com.restaurant.ordering.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service responsible for issuing signed JWT access tokens.
 *
 * <p>Generated tokens contain issuer, issued/expiry timestamps, the account email
 * as subject and the restaurant application role as a custom claim.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;
    private final JwtEncoder jwtEncoder;

    /**
     * Generates an HS256-signed token for an authenticated user.
     * @param email email stored as the JWT subject
     * @param role role stored in the custom role claim
     * @return serialized signed JWT
     */
    public String generateToken(String email, String role) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(jwtProperties.expirationMinutes() * 60))
                .subject(email)
                .claim("role", role)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}

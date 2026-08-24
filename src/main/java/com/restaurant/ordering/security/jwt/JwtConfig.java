package com.restaurant.ordering.security.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Configures JWT signing and validation components used by Spring Security.
 *
 * <p>The application uses a shared HMAC-SHA256 secret for both token encoding and
 * decoding. The decoder also validates the configured issuer.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@Configuration
public class JwtConfig {

    /**
     * Converts the configured text secret into an HMAC key.
     * @param properties JWT application properties
     * @return HMAC-SHA256 secret key
     */
    @Bean
    public SecretKey jwtSecretKey(JwtProperties properties) {
        return new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    /**
     * Creates the component used to sign outgoing tokens.
     * @param secretKey HMAC signing key
     * @return configured JWT encoder
     */
    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {
        return NimbusJwtEncoder.withSecretKey(secretKey).algorithm(MacAlgorithm.HS256).build();
    }

    /**
     * Creates the component used to verify incoming Bearer tokens.
     * @param secretKey HMAC verification key
     * @param properties JWT settings including expected issuer
     * @return configured JWT decoder and validator
     */
    @Bean
    public JwtDecoder jwtDecoder(SecretKey secretKey, JwtProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuer()));
        return decoder;
    }
}

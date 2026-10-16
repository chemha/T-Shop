package org.tshop.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Settings under {@code tshop.jwt.*}.
 *
 * @param issuer     value of the {@code iss} claim, checked when a token comes back
 * @param tokenTtl   how long a token is valid
 * @param publicKey  RSA public key in PEM format (X.509). If both keys are empty a new key pair is
 *                   generated at startup, which is fine locally but invalidates every token on restart
 * @param privateKey RSA private key in PEM format (PKCS#8)
 */
@ConfigurationProperties("tshop.jwt")
public record JwtProperties(
        @DefaultValue("tshop") String issuer,
        @DefaultValue("1h") Duration tokenTtl,
        String publicKey,
        String privateKey) {

    boolean hasKeys() {
        return publicKey != null && !publicKey.isBlank() && privateKey != null && !privateKey.isBlank();
    }
}

package org.tshop.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/** The prod profile reads the RSA keys as PEM text from environment variables. */
class JwtConfigTests {

    JwtConfig config = new JwtConfig();

    @Test
    void loadsPemKeysAndSignsTokensThatItCanVerify() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair original = generator.generateKeyPair();
        // Same format as: openssl genpkey -algorithm RSA ... / openssl rsa -pubout ...
        JwtProperties properties = new JwtProperties("tshop", Duration.ofHours(1),
                pem("PUBLIC KEY", original.getPublic().getEncoded()),
                pem("PRIVATE KEY", original.getPrivate().getEncoded()));

        KeyPair loaded = config.jwtKeyPair(properties);
        String token = config.jwtEncoder(loaded).encode(JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256).build(),
                JwtClaimsSet.builder().issuer("tshop").subject("1").expiresAt(Instant.now().plusSeconds(60)).build()))
                .getTokenValue();

        assertThat(loaded.getPublic()).isEqualTo(original.getPublic());
        assertThat(config.jwtDecoder(loaded, properties).decode(token).getSubject()).isEqualTo("1");
    }

    private static String pem(String type, byte[] der) {
        return "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(der)
                + "\n-----END " + type + "-----\n";
    }
}

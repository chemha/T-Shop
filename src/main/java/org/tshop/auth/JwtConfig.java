package org.tshop.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Tokens are signed with RS256: the private key signs, the public key verifies. Only this
 * application issues and checks tokens, so a shared secret (HS256) would also work, but with RSA
 * other services could verify tokens knowing only the public key.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(JwtProperties.class)
class JwtConfig {

    private static final Logger log = LoggerFactory.getLogger(JwtConfig.class);

    @Bean
    KeyPair jwtKeyPair(JwtProperties properties) throws NoSuchAlgorithmException {
        if (properties.hasKeys()) {
            RSAPublicKey publicKey = RsaKeyConverters.x509().convert(toStream(properties.publicKey()));
            RSAPrivateKey privateKey = RsaKeyConverters.pkcs8().convert(toStream(properties.privateKey()));
            return new KeyPair(publicKey, privateKey);
        }
        log.warn("No JWT keys configured (tshop.jwt.public-key / private-key): generating a temporary key pair. "
                + "Tokens will stop working when the application restarts.");
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        return generator.generateKeyPair();
    }

    @Bean
    JwtEncoder jwtEncoder(KeyPair keyPair) {
        return NimbusJwtEncoder.withKeyPair((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate()).build();
    }

    @Bean
    JwtDecoder jwtDecoder(KeyPair keyPair, JwtProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) keyPair.getPublic()).build();
        // Checks the signature (always), the expiry and that we issued the token
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuer()));
        return decoder;
    }

    private static ByteArrayInputStream toStream(String pem) {
        return new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8));
    }
}

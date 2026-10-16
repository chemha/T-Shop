package org.tshop.auth;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.tshop.customer.Customer;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class TokenService {

    static final String ROLES_CLAIM = "roles";

    private final JwtEncoder encoder;
    private final JwtProperties properties;
    private final Clock clock;

    TokenService(JwtEncoder encoder, JwtProperties properties, Clock clock) {
        this.encoder = encoder;
        this.properties = properties;
        this.clock = clock;
    }

    public record Token(String value, long expiresInSeconds) {
    }

    /**
     * The subject is the customer id, not the email: the email can change, the id can't.
     * The role travels in the token, so a role change applies from the next login.
     */
    public Token issue(Customer customer) {
        Instant now = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(customer.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(properties.tokenTtl()))
                .claim(ROLES_CLAIM, List.of(customer.getRole().name()))
                .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        String value = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new Token(value, properties.tokenTtl().toSeconds());
    }
}

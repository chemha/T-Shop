package org.tshop.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.tshop.ApiTest;
import org.tshop.TestAccounts;
import org.tshop.TestAccounts.Account;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
class SecurityTests {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    JwtEncoder appEncoder;

    @Autowired
    TestAccounts accounts;

    @Test
    void protectedEndpointsNeedAToken() {
        assertThat(mvc.get().uri("/api/v1/me"))
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .hasHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
    }

    @Test
    void validTokenGivesAccess() {
        Account ana = accounts.customer();

        assertThat(mvc.get().uri("/api/v1/me").header(HttpHeaders.AUTHORIZATION, ana.bearer()))
                .hasStatusOk()
                .bodyJson().extractingPath("$.id").isEqualTo(ana.id().intValue());
    }

    @Test
    void customerCannotPromoteThemselvesByEditingTheToken() {
        // A JWT is header.payload.signature, and the payload is only Base64, not encrypted:
        // anyone can read and edit it. The signature is what makes editing it useless.
        Account ana = accounts.customer();
        String[] parts = ana.token().split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        String promoted = payload.replace("\"CUSTOMER\"", "\"ADMIN\"");
        assertThat(promoted).isNotEqualTo(payload);
        String tampered = parts[0] + "." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(promoted.getBytes(StandardCharsets.UTF_8)) + "." + parts[2];

        assertThat(get("/api/v1/admin/customers", tampered)).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() throws Exception {
        // Someone who knows the token format but not our private key can't forge one,
        // not even for an admin
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair attackerKeys = generator.generateKeyPair();
        JwtEncoder attacker = NimbusJwtEncoder.withKeyPair(
                (RSAPublicKey) attackerKeys.getPublic(), (RSAPrivateKey) attackerKeys.getPrivate()).build();

        String forged = sign(attacker, claims("tshop", Instant.now().plus(1, ChronoUnit.HOURS)));

        assertThat(get("/api/v1/admin/customers", forged)).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void expiredTokenIsRejected() {
        // The default validator allows 60 seconds of clock skew
        String expired = sign(appEncoder, claims("tshop", Instant.now().minus(2, ChronoUnit.MINUTES)));

        assertThat(get("/api/v1/me", expired)).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void tokenFromAnotherIssuerIsRejected() {
        String foreign = sign(appEncoder, claims("someone-else", Instant.now().plus(1, ChronoUnit.HOURS)));

        assertThat(get("/api/v1/me", foreign)).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void customersCannotUseAdminEndpoints() {
        Account ana = accounts.customer();

        assertThat(get("/api/v1/admin/customers", ana.token()))
                .hasStatus(HttpStatus.FORBIDDEN)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(mvc.post().uri("/api/v1/categories").header(HttpHeaders.AUTHORIZATION, ana.bearer())
                .contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Hacked"}"""))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    void catalogueAndDocumentationArePublic() {
        assertThat(mvc.get().uri("/openapi.yaml")).hasStatusOk();
        assertThat(mvc.get().uri("/swagger-ui/index.html")).hasStatusOk();
        assertThat(mvc.get().uri("/api/v1/categories")).hasStatusOk();
        assertThat(mvc.get().uri("/api/v1/products")).hasStatusOk();
    }

    @Test
    void healthChecksArePublic() {
        assertThat(mvc.get().uri("/actuator/health")).hasStatusOk()
                .bodyJson().extractingPath("$.status").isEqualTo("UP");
        assertThat(mvc.get().uri("/actuator/health/readiness")).hasStatusOk();
        assertThat(mvc.get().uri("/actuator/info")).hasStatusOk()
                .bodyJson().extractingPath("$.build.artifact").isEqualTo("tshop");
    }

    @Test
    void pathsOutsideTheApiAreDenied() {
        assertThat(mvc.get().uri("/actuator/env")).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    private MvcTestResult get(String uri, String token) {
        return mvc.get().uri(uri).header(HttpHeaders.AUTHORIZATION, "Bearer " + token).exchange();
    }

    private static JwtClaimsSet claims(String issuer, Instant expiresAt) {
        return JwtClaimsSet.builder()
                .issuer(issuer)
                .subject("1")
                .issuedAt(expiresAt.minus(1, ChronoUnit.HOURS))
                .expiresAt(expiresAt)
                .claim("roles", List.of("ADMIN"))
                .build();
    }

    private static String sign(JwtEncoder encoder, JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}

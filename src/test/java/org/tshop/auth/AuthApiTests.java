package org.tshop.auth;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.tshop.ApiTest;
import org.tshop.TestAccounts;
import org.tshop.customer.CustomerRepository;

import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
class AuthApiTests {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    CustomerRepository customers;

    @Autowired
    JwtDecoder jwtDecoder;

    @Autowired
    TestAccounts accounts;

    @Test
    void registersACustomer() {
        String email = TestAccounts.uniqueEmail();

        MvcTestResult result = register(email, "a-long-password");

        assertThat(result).hasStatus(HttpStatus.CREATED)
                .bodyJson().extractingPath("$.email").isEqualTo(email);
        assertThat(result).bodyJson().extractingPath("$.role").isEqualTo("CUSTOMER");
        assertThat(result).bodyJson().doesNotHavePath("$.password");
        assertThat(customers.findByEmail(email)).hasValueSatisfying(customer ->
                assertThat(customer.getPasswordHash()).startsWith("{bcrypt}$2a$"));
    }

    @Test
    void registrationCannotChooseTheRole() {
        // Mass assignment: an extra "role" field in the JSON must be ignored, not bound
        String email = TestAccounts.uniqueEmail();
        String body = """
                {"firstName": "Eve", "lastName": "Hacker", "birthDate": "1990-01-01",
                 "email": "%s", "password": "a-long-password", "role": "ADMIN"}""".formatted(email);

        assertThat(mvc.post().uri("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson().extractingPath("$.role").isEqualTo("CUSTOMER");
    }

    @Test
    void emailCanOnlyBeRegisteredOnce() {
        String email = TestAccounts.uniqueEmail();
        register(email, "a-long-password");

        assertThat(register(email.toUpperCase(), "another-password"))
                .hasStatus(HttpStatus.CONFLICT)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    }

    @Test
    void invalidRegistrationListsTheWrongFields() {
        MvcTestResult result = register("not-an-email", "short");

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath("$.errors[*].field")
                .asArray().containsExactlyInAnyOrder("email", "password");
    }

    @Test
    void loginReturnsASignedTokenWithTheRole() throws Exception {
        // Demo admin from the seed data: its BCrypt hash comes from v1 without the {bcrypt} prefix
        MvcTestResult result = login("admin@admin.admin", "admin");

        assertThat(result).hasStatusOk()
                .bodyJson().extractingPath("$.tokenType").isEqualTo("Bearer");
        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
        Jwt jwt = jwtDecoder.decode(token);
        assertThat(jwt.getClaimAsStringList("roles")).containsExactly("ADMIN");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("tshop");
    }

    @Test
    void emailIsCaseInsensitiveAtLogin() {
        TestAccounts.Account account = accounts.customer();

        assertThat(login(account.email().toUpperCase(), TestAccounts.PASSWORD)).hasStatusOk();
    }

    @Test
    void wrongPasswordAndUnknownEmailGetTheSameAnswer() {
        TestAccounts.Account account = accounts.customer();

        for (MvcTestResult result : new MvcTestResult[]{
                login(account.email(), "wrong-password"),
                login("nobody-" + account.email(), TestAccounts.PASSWORD)}) {
            assertThat(result).hasStatus(HttpStatus.UNAUTHORIZED)
                    .hasHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                    .bodyJson().extractingPath("$.detail").isEqualTo("Invalid email or password");
        }
    }

    private MvcTestResult register(String email, String password) {
        String body = """
                {"firstName": "Ana", "lastName": "García", "birthDate": "1990-05-17", "email": "%s", "password": "%s"}"""
                .formatted(email, password);
        return mvc.post().uri("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body).exchange();
    }

    private MvcTestResult login(String email, String password) {
        String body = """
                {"email": "%s", "password": "%s"}""".formatted(email, password);
        return mvc.post().uri("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(body).exchange();
    }
}

package org.tshop.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.tshop.ApiTest;
import org.tshop.TestAccounts;
import org.tshop.TestAccounts.Account;

import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
class AccountApiTests {

    @Autowired
    MockMvcTester mvc;

    @Autowired
    TestAccounts accounts;

    @Test
    void customerUpdatesTheirProfile() {
        Account ana = accounts.customer();
        String newEmail = TestAccounts.uniqueEmail();

        MvcTestResult result = updateProfile(ana, newEmail);

        assertThat(result).hasStatusOk().bodyJson().extractingPath("$.lastName").isEqualTo("Updated");
        // The token is still valid: it identifies the customer by id, not by email
        assertThat(mvc.get().uri("/api/v1/me").header(HttpHeaders.AUTHORIZATION, ana.bearer()))
                .hasStatusOk().bodyJson().extractingPath("$.email").isEqualTo(newEmail);
    }

    @Test
    void cannotTakeSomeoneElsesEmail() {
        Account ana = accounts.customer();
        Account bob = accounts.customer();

        assertThat(updateProfile(ana, bob.email())).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void changingThePasswordNeedsTheCurrentOne() {
        Account ana = accounts.customer();

        assertThat(changePassword(ana, "not-my-password", "a-brand-new-password"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.errors[0].field").isEqualTo("currentPassword");

        assertThat(changePassword(ana, TestAccounts.PASSWORD, "a-brand-new-password"))
                .hasStatus(HttpStatus.NO_CONTENT);
        assertThat(login(ana.email(), "a-brand-new-password")).hasStatusOk();
        assertThat(login(ana.email(), TestAccounts.PASSWORD)).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    private MvcTestResult updateProfile(Account account, String email) {
        return mvc.put().uri("/api/v1/me").header(HttpHeaders.AUTHORIZATION, account.bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"firstName": "Ana", "lastName": "Updated", "birthDate": "1990-05-17", "email": "%s"}"""
                        .formatted(email))
                .exchange();
    }

    private MvcTestResult changePassword(Account account, String current, String newPassword) {
        return mvc.put().uri("/api/v1/me/password").header(HttpHeaders.AUTHORIZATION, account.bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"currentPassword": "%s", "newPassword": "%s"}""".formatted(current, newPassword))
                .exchange();
    }

    private MvcTestResult login(String email, String password) {
        return mvc.post().uri("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "%s"}""".formatted(email, password))
                .exchange();
    }
}

package org.tshop.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.tshop.ApiClient;
import org.tshop.ApiTest;
import org.tshop.TestAccounts;
import org.tshop.TestAccounts.Account;

import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
class AddressApiTests {

    static final String ADDRESS = """
            {"country": "Spain", "postalCode": "28001", "street": "Gran Via", "houseNumber": "1", "apartment": "2B"}""";

    @Autowired
    ApiClient api;

    @Autowired
    TestAccounts accounts;

    @Test
    void customerManagesTheirAddresses() {
        Account ana = accounts.customer();

        long id = ApiClient.id(api.post(ana, ADDRESS, "/api/v1/me/addresses"));
        assertThat(api.put(ana, ADDRESS.replace("Gran Via", "Alcala"), "/api/v1/me/addresses/{id}", id))
                .hasStatusOk().bodyJson().extractingPath("$.street").isEqualTo("Alcala");
        assertThat(api.get(ana, "/api/v1/me/addresses"))
                .bodyJson().extractingPath("$[*].street").asArray().containsExactly("Alcala");

        assertThat(api.delete(ana, "/api/v1/me/addresses/{id}", id)).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(api.get(ana, "/api/v1/me/addresses")).bodyJson().extractingPath("$").asArray().isEmpty();
    }

    @Test
    void addressesOfOtherCustomersDontExist() {
        // In v1 any logged-in user could read or change any address by changing the id (IDOR)
        Account ana = accounts.customer();
        Account mallory = accounts.customer();
        long anasAddress = ApiClient.id(api.post(ana, ADDRESS, "/api/v1/me/addresses"));

        assertThat(api.put(mallory, ADDRESS, "/api/v1/me/addresses/{id}", anasAddress)).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(api.delete(mallory, "/api/v1/me/addresses/{id}", anasAddress)).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(api.get(mallory, "/api/v1/me/addresses")).bodyJson().extractingPath("$").asArray().isEmpty();
        // ...and Ana's address is untouched
        assertThat(api.get(ana, "/api/v1/me/addresses")).bodyJson().extractingPath("$[0].street").isEqualTo("Gran Via");
    }

    @Test
    void requiredFieldsAreChecked() {
        assertThat(api.post(accounts.customer(), """
                {"country": "", "street": "Gran Via"}""", "/api/v1/me/addresses"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.errors[*].field").asArray().containsExactlyInAnyOrder("country", "postalCode");
    }
}

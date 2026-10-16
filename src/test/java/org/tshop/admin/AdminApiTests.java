package org.tshop.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.tshop.ApiClient;
import org.tshop.ApiTest;
import org.tshop.TestAccounts;
import org.tshop.TestAccounts.Account;
import org.tshop.TestCatalog;

import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
class AdminApiTests {

    @Autowired
    ApiClient api;

    @Autowired
    TestAccounts accounts;

    @Autowired
    TestCatalog catalog;

    @Test
    void customersArePaginated() {
        Account admin = accounts.admin();

        assertThat(api.get(admin, "/api/v1/admin/customers?size=2"))
                .hasStatusOk()
                .bodyJson().extractingPath("$.content").asArray().hasSize(2);
    }

    @Test
    void promotedCustomerGetsAdminRightsAtNextLogin() {
        Account admin = accounts.admin();
        Account ana = accounts.customer();

        assertThat(api.put(admin, """
                {"role": "ADMIN"}""", "/api/v1/admin/customers/{id}/role", ana.id()))
                .hasStatusOk().bodyJson().extractingPath("$.role").isEqualTo("ADMIN");

        // Her current token still says CUSTOMER: roles travel in the token
        assertThat(api.get(ana, "/api/v1/admin/customers")).hasStatus(HttpStatus.FORBIDDEN);
        Account anaAfterLogin = new Account(ana.id(), ana.email(), ApiClient.read(api.post(null, """
                {"email": "%s", "password": "%s"}""".formatted(ana.email(), TestAccounts.PASSWORD), "/api/v1/auth/login"),
                "$.accessToken"));
        assertThat(api.get(anaAfterLogin, "/api/v1/admin/customers")).hasStatusOk();
    }

    @Test
    void adminsCannotLockThemselvesOut() {
        Account admin = accounts.admin();

        assertThat(api.put(admin, """
                {"role": "CUSTOMER"}""", "/api/v1/admin/customers/{id}/role", admin.id())).hasStatus(HttpStatus.CONFLICT);
        assertThat(api.delete(admin, "/api/v1/admin/customers/{id}", admin.id())).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void customersWithOrdersAreKept() {
        Account admin = accounts.admin();
        Account withOrders = accounts.customer();
        Account withoutOrders = accounts.customer();
        long address = ApiClient.id(api.post(withOrders, """
                {"country": "Spain", "postalCode": "28001", "street": "Gran Via"}""", "/api/v1/me/addresses"));
        api.put(withOrders, """
                {"quantity": 1}""", "/api/v1/me/cart/items/{id}", catalog.product("1.00", 5).getId());
        api.post(withOrders, """
                {"addressId": %d, "paymentMethod": "CASH", "deliveryMethod": "STANDARD"}""".formatted(address), "/api/v1/me/orders");

        assertThat(api.delete(admin, "/api/v1/admin/customers/{id}", withOrders.id())).hasStatus(HttpStatus.CONFLICT);
        assertThat(api.delete(admin, "/api/v1/admin/customers/{id}", withoutOrders.id())).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(api.get(admin, "/api/v1/admin/customers/{id}", withoutOrders.id())).hasStatus(HttpStatus.NOT_FOUND);
    }
}

package org.tshop.cart;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.tshop.ApiClient;
import org.tshop.ApiTest;
import org.tshop.TestAccounts;
import org.tshop.TestAccounts.Account;
import org.tshop.TestCatalog;
import org.tshop.catalog.Product;
import org.tshop.catalog.ProductRepository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
class CartApiTests {

    @Autowired
    ApiClient api;

    @Autowired
    TestAccounts accounts;

    @Autowired
    TestCatalog catalog;

    @Autowired
    ProductRepository products;

    @Test
    void cartAddsUpTheLines() {
        Account ana = accounts.customer();
        Product mouse = catalog.product("19.99", 10);
        Product cable = catalog.product("4.50", 10);

        setItem(ana, mouse, 3);
        MvcTestResult cart = setItem(ana, cable, 2);

        assertThat(cart).hasStatusOk().bodyJson().extractingPath("$.total").isEqualTo(68.97);
        assertThat(cart).bodyJson().extractingPath("$.items[*].subtotal").asArray().containsExactly(59.97, 9.0);
    }

    @Test
    void settingAnItemAgainReplacesTheQuantity() {
        Account ana = accounts.customer();
        Product mouse = catalog.product("10.00", 10);

        setItem(ana, mouse, 3);

        assertThat(setItem(ana, mouse, 1)).bodyJson().extractingPath("$.items[*].quantity").asArray().containsExactly(1);
    }

    @Test
    void cannotAddMoreThanTheStock() {
        Account ana = accounts.customer();
        Product lastTwo = catalog.product("10.00", 2);

        assertThat(setItem(ana, lastTwo, 3)).hasStatus(HttpStatus.CONFLICT)
                .bodyJson().extractingPath("$.detail").asString().startsWith("Only 2 unit(s)");
        assertThat(api.put(ana, """
                {"quantity": 1}""", "/api/v1/me/cart/items/999999")).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void cartShowsCurrentPriceAndStock() {
        // The v1 cart stored whole products in the session and kept showing the old price
        Account ana = accounts.customer();
        Product mouse = catalog.product("10.00", 5);
        setItem(ana, mouse, 4);

        Product changed = products.findById(mouse.getId()).orElseThrow();
        changed.changePrice(new BigDecimal("12.00"));
        changed.changeStock(3);
        products.save(changed);

        MvcTestResult cart = api.get(ana, "/api/v1/me/cart");
        assertThat(cart).bodyJson().extractingPath("$.items[0].unitPrice").isEqualTo(12.0);
        assertThat(cart).bodyJson().extractingPath("$.items[0].inStock").isEqualTo(false);
    }

    @Test
    void removingAndClearing() {
        Account ana = accounts.customer();
        Product mouse = catalog.product("10.00", 5);
        Product cable = catalog.product("1.00", 5);
        setItem(ana, mouse, 1);
        setItem(ana, cable, 1);

        assertThat(api.delete(ana, "/api/v1/me/cart/items/{id}", mouse.getId()))
                .hasStatusOk().bodyJson().extractingPath("$.items[*].productId").asArray().containsExactly(cable.getId().intValue());
        assertThat(api.delete(ana, "/api/v1/me/cart")).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(api.get(ana, "/api/v1/me/cart")).bodyJson().extractingPath("$.total").isEqualTo(0);
    }

    @Test
    void eachCustomerOnlySeesTheirCart() {
        Account ana = accounts.customer();
        Account bob = accounts.customer();
        setItem(ana, catalog.product("10.00", 5), 1);

        assertThat(api.get(bob, "/api/v1/me/cart")).bodyJson().extractingPath("$.items").asArray().isEmpty();
    }

    private MvcTestResult setItem(Account account, Product product, int quantity) {
        return api.put(account, """
                {"quantity": %d}""".formatted(quantity), "/api/v1/me/cart/items/{id}", product.getId());
    }
}

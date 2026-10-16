package org.tshop.order;

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
class OrderApiTests {

    @Autowired
    ApiClient api;

    @Autowired
    TestAccounts accounts;

    @Autowired
    TestCatalog catalog;

    @Autowired
    ProductRepository products;

    // ---------------------------------------------------------------- Checkout

    @Test
    void checkoutTurnsTheCartIntoAnOrder() {
        Account ana = accounts.customer();
        Product mouse = catalog.product("19.99", 5);
        long address = addAddress(ana, "Gran Via");
        addToCart(ana, mouse, 2);

        MvcTestResult order = placeOrder(ana, address, "CARD");

        assertThat(order).hasStatus(HttpStatus.CREATED).bodyJson().extractingPath("$.status").isEqualTo("PENDING");
        assertThat(order).bodyJson().extractingPath("$.total").isEqualTo(39.98);
        assertThat(order).bodyJson().extractingPath("$.items[0].productName").isEqualTo(mouse.getName());
        assertThat(order).bodyJson().extractingPath("$.shippingAddress.street").isEqualTo("Gran Via");
        assertThat(catalog.stockOf(mouse.getId())).isEqualTo(3);
        assertThat(api.get(ana, "/api/v1/me/cart")).bodyJson().extractingPath("$.items").asArray().isEmpty();
    }

    @Test
    void ordersKeepThePriceAndAddressOfTheirMoment() {
        Account ana = accounts.customer();
        Product mouse = catalog.product("10.00", 5);
        long address = addAddress(ana, "Gran Via");
        addToCart(ana, mouse, 1);
        long orderId = ApiClient.id(placeOrder(ana, address, "CASH"));

        Product changed = products.findById(mouse.getId()).orElseThrow();
        changed.changePrice(new BigDecimal("99.00"));
        products.save(changed);
        api.put(ana, address("Alcala"), "/api/v1/me/addresses/{id}", address);

        MvcTestResult order = api.get(ana, "/api/v1/me/orders/{id}", orderId);
        assertThat(order).bodyJson().extractingPath("$.items[0].unitPrice").isEqualTo(10.0);
        assertThat(order).bodyJson().extractingPath("$.shippingAddress.street").isEqualTo("Gran Via");
    }

    @Test
    void emptyCartCannotBeOrdered() {
        Account ana = accounts.customer();

        assertThat(placeOrder(ana, addAddress(ana, "Gran Via"), "CARD"))
                .hasStatus(HttpStatus.CONFLICT)
                .bodyJson().extractingPath("$.detail").isEqualTo("The cart is empty");
    }

    @Test
    void cannotShipToSomeoneElsesAddress() {
        Account ana = accounts.customer();
        Account bob = accounts.customer();
        addToCart(ana, catalog.product("10.00", 5), 1);

        assertThat(placeOrder(ana, addAddress(bob, "Bob Street"), "CARD")).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void checkoutIsAllOrNothing() {
        Account ana = accounts.customer();
        Product plenty = catalog.product("10.00", 10);
        Product scarce = catalog.product("10.00", 5);
        long address = addAddress(ana, "Gran Via");
        addToCart(ana, plenty, 2);
        addToCart(ana, scarce, 4);
        // Someone else buys most of the scarce product after Ana added it to her cart
        Product sold = products.findById(scarce.getId()).orElseThrow();
        sold.reserveStock(3);
        products.save(sold);

        assertThat(placeOrder(ana, address, "CARD")).hasStatus(HttpStatus.CONFLICT)
                .bodyJson().extractingPath("$.detail").asString().contains("Only 2 unit(s)");

        // The first line had already been reserved in memory when the second failed: the rollback
        // undid it. In v1, half an order could stay in the database.
        assertThat(catalog.stockOf(plenty.getId())).isEqualTo(10);
        assertThat(api.get(ana, "/api/v1/me/cart")).bodyJson().extractingPath("$.items").asArray().hasSize(2);
        assertThat(api.get(ana, "/api/v1/me/orders")).bodyJson().extractingPath("$.page.totalElements").isEqualTo(0);
    }

    // ---------------------------------------------------------------- Customer's orders

    @Test
    void customersOnlySeeTheirOwnOrders() {
        Account ana = accounts.customer();
        Account mallory = accounts.customer();
        long orderId = orderOf(ana, catalog.product("10.00", 5), 1);

        assertThat(api.get(mallory, "/api/v1/me/orders/{id}", orderId)).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(api.post(mallory, null, "/api/v1/me/orders/{id}/cancel", orderId)).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(api.get(mallory, "/api/v1/me/orders")).bodyJson().extractingPath("$.content").asArray().isEmpty();
    }

    @Test
    void ordersAreListedNewestFirst() {
        Account ana = accounts.customer();
        Product product = catalog.product("1.00", 10);
        long first = orderOf(ana, product, 1);
        long second = orderOf(ana, product, 2);

        assertThat(api.get(ana, "/api/v1/me/orders"))
                .bodyJson().extractingPath("$.content[*].id").asArray().containsExactly((int) second, (int) first);
    }

    @Test
    void cancellingAPendingOrderReturnsTheStock() {
        Account ana = accounts.customer();
        Product mouse = catalog.product("10.00", 5);
        long orderId = orderOf(ana, mouse, 2);
        assertThat(catalog.stockOf(mouse.getId())).isEqualTo(3);

        assertThat(api.post(ana, null, "/api/v1/me/orders/{id}/cancel", orderId))
                .hasStatusOk().bodyJson().extractingPath("$.status").isEqualTo("CANCELLED");
        assertThat(catalog.stockOf(mouse.getId())).isEqualTo(5);
        assertThat(api.post(ana, null, "/api/v1/me/orders/{id}/cancel", orderId)).hasStatus(HttpStatus.CONFLICT);
    }

    // ---------------------------------------------------------------- Admin

    @Test
    void adminTakesAnOrderToDelivery() {
        Account ana = accounts.customer();
        Account admin = accounts.admin();
        long orderId = orderOf(ana, catalog.product("10.00", 5), 1); // paid with CARD

        assertThat(changeStatus(admin, orderId, "CONFIRMED")).hasStatusOk()
                .bodyJson().extractingPath("$.paymentStatus").isEqualTo("PAID");
        // Once confirmed, only the shop can cancel it
        assertThat(api.post(ana, null, "/api/v1/me/orders/{id}/cancel", orderId)).hasStatus(HttpStatus.CONFLICT);
        assertThat(changeStatus(admin, orderId, "SHIPPED")).hasStatusOk();
        assertThat(changeStatus(admin, orderId, "DELIVERED")).hasStatusOk();
        assertThat(changeStatus(admin, orderId, "CANCELLED")).hasStatus(HttpStatus.CONFLICT)
                .bodyJson().extractingPath("$.detail").isEqualTo("An order can't go from DELIVERED to CANCELLED");
    }

    @Test
    void adminCancellingAPaidOrderRefundsItAndReturnsTheStock() {
        Account admin = accounts.admin();
        Product mouse = catalog.product("10.00", 5);
        long orderId = orderOf(accounts.customer(), mouse, 2);
        changeStatus(admin, orderId, "CONFIRMED");

        assertThat(changeStatus(admin, orderId, "CANCELLED"))
                .bodyJson().extractingPath("$.paymentStatus").isEqualTo("REFUNDED");
        assertThat(catalog.stockOf(mouse.getId())).isEqualTo(5);
    }

    @Test
    void adminFiltersOrdersByStatus() {
        Account admin = accounts.admin();
        long orderId = orderOf(accounts.customer(), catalog.product("10.00", 5), 1);
        changeStatus(admin, orderId, "CONFIRMED");
        changeStatus(admin, orderId, "SHIPPED");

        assertThat(api.get(admin, "/api/v1/admin/orders?status=SHIPPED&size=100"))
                .hasStatusOk().bodyJson().extractingPath("$.content[*].id").asArray().contains((int) orderId);
        assertThat(api.get(admin, "/api/v1/admin/orders?status=PENDING&size=100"))
                .bodyJson().extractingPath("$.content[*].id").asArray().doesNotContain((int) orderId);
    }

    // ---------------------------------------------------------------- Helpers

    private long orderOf(Account account, Product product, int quantity) {
        long address = addAddress(account, "Gran Via");
        addToCart(account, product, quantity);
        MvcTestResult order = placeOrder(account, address, "CARD");
        assertThat(order).hasStatus(HttpStatus.CREATED);
        return ApiClient.id(order);
    }

    private long addAddress(Account account, String street) {
        return ApiClient.id(api.post(account, address(street), "/api/v1/me/addresses"));
    }

    private void addToCart(Account account, Product product, int quantity) {
        assertThat(api.put(account, """
                {"quantity": %d}""".formatted(quantity), "/api/v1/me/cart/items/{id}", product.getId())).hasStatusOk();
    }

    private MvcTestResult placeOrder(Account account, long addressId, String payment) {
        return api.post(account, """
                {"addressId": %d, "paymentMethod": "%s", "deliveryMethod": "STANDARD"}""".formatted(addressId, payment),
                "/api/v1/me/orders");
    }

    private MvcTestResult changeStatus(Account admin, long orderId, String status) {
        return api.put(admin, """
                {"status": "%s"}""".formatted(status), "/api/v1/admin/orders/{id}/status", orderId);
    }

    private static String address(String street) {
        return """
                {"country": "Spain", "postalCode": "28001", "street": "%s", "houseNumber": "1"}""".formatted(street);
    }
}

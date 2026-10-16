package org.tshop.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.tshop.ApiTest;
import org.tshop.TestAccounts;
import org.tshop.TestAccounts.Account;
import org.tshop.TestCatalog;
import org.tshop.cart.CartService;
import org.tshop.catalog.InsufficientStockException;
import org.tshop.catalog.Product;
import org.tshop.customer.AddressService;
import org.tshop.customer.AddressService.AddressData;
import org.tshop.order.OrderService.Checkout;
import org.tshop.shared.error.ConflictException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Several customers check out the same product at the same moment, each on its own thread with
 * its own transaction, as concurrent HTTP requests would. The v1 code checked the stock and then
 * subtracted it in separate steps, so all of them could succeed and the stock went negative.
 */
@ApiTest
class CheckoutConcurrencyTests {

    @Autowired
    OrderService orders;

    @Autowired
    CartService cart;

    @Autowired
    AddressService addresses;

    @Autowired
    TestAccounts accounts;

    @Autowired
    TestCatalog catalog;

    sealed interface Outcome {
        record Placed(Order order) implements Outcome {
        }

        record Rejected(RuntimeException reason) implements Outcome {
        }
    }

    @Test
    void lastUnitGoesToExactlyOneCustomer() throws Exception {
        Product lastUnit = catalog.product("10.00", 1);

        List<Outcome> outcomes = checkoutAtOnce(lastUnit, 2);

        assertThat(outcomes).filteredOn(Outcome.Placed.class::isInstance).hasSize(1);
        // The loser retried with fresh data and got the real reason, not a technical error
        assertThat(outcomes).filteredOn(Outcome.Rejected.class::isInstance)
                .singleElement()
                .satisfies(outcome -> assertThat(((Outcome.Rejected) outcome).reason())
                        .isInstanceOf(InsufficientStockException.class));
        assertThat(catalog.stockOf(lastUnit.getId())).isZero();
    }

    @Test
    void stockIsNeverOversold() throws Exception {
        Product threeLeft = catalog.product("10.00", 3);

        List<Outcome> outcomes = checkoutAtOnce(threeLeft, 8);

        assertThat(outcomes).filteredOn(Outcome.Placed.class::isInstance).hasSize(3);
        assertThat(catalog.stockOf(threeLeft.getId())).isZero();
        // Every rejection is a 409 for the client: either "not enough stock" or, if it lost
        // the race on every retry, "modified at the same time, try again"
        assertThat(outcomes).filteredOn(Outcome.Rejected.class::isInstance)
                .hasSize(5)
                .allSatisfy(outcome -> assertThat(((Outcome.Rejected) outcome).reason())
                        .isInstanceOfAny(ConflictException.class,
                                org.springframework.orm.ObjectOptimisticLockingFailureException.class));
    }

    /** Fills one cart per customer, then releases all the checkouts at the same instant. */
    private List<Outcome> checkoutAtOnce(Product product, int customers) throws Exception {
        List<Account> buyers = new ArrayList<>();
        List<Checkout> checkouts = new ArrayList<>();
        for (int i = 0; i < customers; i++) {
            Account buyer = accounts.customer();
            Long addressId = addresses.create(buyer.id(), new AddressData("Spain", "28001", "Gran Via", "1", null)).getId();
            cart.setItem(buyer.id(), product.getId(), 1);
            buyers.add(buyer);
            checkouts.add(new Checkout(addressId, PaymentMethod.CARD, DeliveryMethod.STANDARD));
        }

        CountDownLatch start = new CountDownLatch(1);
        List<Future<Outcome>> futures = new ArrayList<>();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < customers; i++) {
                Long customerId = buyers.get(i).id();
                Checkout checkout = checkouts.get(i);
                futures.add(executor.submit(() -> {
                    start.await();
                    try {
                        return new Outcome.Placed(orders.placeOrder(customerId, checkout));
                    } catch (RuntimeException e) {
                        return new Outcome.Rejected(e);
                    }
                }));
            }
            start.countDown();
        }
        List<Outcome> outcomes = new ArrayList<>();
        for (Future<Outcome> future : futures) {
            outcomes.add(future.get());
        }
        return outcomes;
    }
}

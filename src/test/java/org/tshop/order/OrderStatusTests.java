package org.tshop.order;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStatusTests {

    static final ShippingAddress ADDRESS = new ShippingAddress("Spain", "28001", "Gran Via", "1", null);

    /** Every status pair: the table is the whole state machine at a glance. */
    @ParameterizedTest(name = "{0} -> {1}: {2}")
    @CsvSource({
            "PENDING,   CONFIRMED, true",
            "PENDING,   CANCELLED, true",
            "PENDING,   SHIPPED,   false",
            "PENDING,   DELIVERED, false",
            "CONFIRMED, SHIPPED,   true",
            "CONFIRMED, CANCELLED, true",
            "CONFIRMED, DELIVERED, false",
            "CONFIRMED, PENDING,   false",
            "SHIPPED,   DELIVERED, true",
            "SHIPPED,   CANCELLED, false",
            "DELIVERED, CANCELLED, false",
            "CANCELLED, PENDING,   false",
            "CANCELLED, CONFIRMED, false"})
    void transitions(OrderStatus from, OrderStatus to, boolean allowed) {
        assertThat(from.canMoveTo(to)).isEqualTo(allowed);
    }

    /** Payment status after walking the order through the given statuses. */
    @ParameterizedTest(name = "{0} through {1}: {2}")
    @CsvSource({
            "CARD, CONFIRMED,                    PAID",
            "CASH, CONFIRMED,                    PENDING",
            "CASH, CONFIRMED SHIPPED DELIVERED,  PAID",
            "CARD, CONFIRMED CANCELLED,          REFUNDED",
            "CASH, CONFIRMED CANCELLED,          PENDING",
            "CARD, CANCELLED,                    PENDING"})
    void paymentFollowsTheOrder(PaymentMethod method, String path, PaymentStatus expected) {
        Order order = new Order(1L, method, DeliveryMethod.STANDARD, ADDRESS);

        for (String status : path.split(" ")) {
            order.moveTo(OrderStatus.valueOf(status));
        }

        assertThat(order.getPaymentStatus()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"SHIPPED", "DELIVERED", "PENDING"})
    void invalidTransitionsAreRejected(OrderStatus next) {
        Order order = new Order(1L, PaymentMethod.CARD, DeliveryMethod.STANDARD, ADDRESS);

        assertThatThrownBy(() -> order.moveTo(next))
                .isInstanceOf(InvalidOrderTransitionException.class)
                .hasMessage("An order can't go from PENDING to " + next);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }
}

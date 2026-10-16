package org.tshop.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTests {

    static final ShippingAddress ADDRESS = new ShippingAddress("Spain", "28001", "Gran Via", "1", null);

    @Test
    void newOrderIsPendingAndEmpty() {
        Order order = new Order(1L, PaymentMethod.CARD, DeliveryMethod.STANDARD, ADDRESS);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(order.getTotal()).isZero();
    }

    @Test
    void totalIsTheSumOfTheLines() {
        Order order = new Order(1L, PaymentMethod.CARD, DeliveryMethod.STANDARD, ADDRESS);

        order.addItem(10L, "Mouse", new BigDecimal("19.99"), 3);
        order.addItem(11L, "Keyboard", new BigDecimal("0.10"), 1);

        // BigDecimal keeps cents exact: with double, 19.99 * 3 + 0.10 is 60.06999999999999
        assertThat(order.getTotal()).isEqualByComparingTo("60.07");
        assertThat(order.getItems()).extracting(OrderItem::subtotal)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("59.97"), new BigDecimal("0.10"));
    }
}

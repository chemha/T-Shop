package org.tshop.order;

import org.tshop.shared.error.ConflictException;

public class InvalidOrderTransitionException extends ConflictException {

    public InvalidOrderTransitionException(OrderStatus from, OrderStatus to) {
        super("An order can't go from %s to %s".formatted(from, to));
    }
}

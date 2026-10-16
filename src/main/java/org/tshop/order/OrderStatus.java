package org.tshop.order;

/**
 * <pre>
 * PENDING ──► CONFIRMED ──► SHIPPED ──► DELIVERED
 *    │            │
 *    └────────────┴──► CANCELLED
 * </pre>
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean canMoveTo(OrderStatus next) {
        // No "default" branch: the switch must cover every status, so adding a new one is a
        // compile error here until its transitions are decided
        return switch (this) {
            case PENDING -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == SHIPPED || next == CANCELLED;
            case SHIPPED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}

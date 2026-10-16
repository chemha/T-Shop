package org.tshop.cart;

import jakarta.persistence.Embeddable;

/** Composite primary key of a cart line: one row per customer and product. */
@Embeddable
public record CartItemId(Long customerId, Long productId) {
}

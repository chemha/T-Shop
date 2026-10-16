package org.tshop.cart;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * A product in a customer's cart. Only the quantity is stored: price and stock are always read
 * from the product, so the cart never shows stale data (the v1 cart kept whole products in the
 * HTTP session).
 */
@Entity
public class CartItem {

    @EmbeddedId
    private CartItemId id;

    private int quantity;

    @CreationTimestamp
    private Instant addedAt;

    protected CartItem() {
    }

    public CartItem(Long customerId, Long productId, int quantity) {
        this.id = new CartItemId(customerId, productId);
        changeQuantity(quantity);
    }

    public void changeQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = quantity;
    }

    public CartItemId getId() {
        return id;
    }

    public Long getProductId() {
        return id.productId();
    }

    public int getQuantity() {
        return quantity;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    // The id is assigned by the application (not the database), so it can always be compared
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof CartItem other && id.equals(other.getId()));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

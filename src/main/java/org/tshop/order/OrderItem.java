package org.tshop.order;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A line of an order. The product name and price are copied when the order is placed, so the
 * order keeps showing what was actually bought even if the product changes or is deleted.
 */
@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Null once the product has been deleted from the catalogue. */
    private Long productId;

    private String productName;

    private BigDecimal unitPrice;

    private int quantity;

    protected OrderItem() {
    }

    OrderItem(Long productId, String productName, BigDecimal unitPrice, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.productId = Objects.requireNonNull(productId);
        this.productName = Objects.requireNonNull(productName);
        this.unitPrice = Objects.requireNonNull(unitPrice);
        this.quantity = quantity;
    }

    public BigDecimal subtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof OrderItem other && id != null && id.equals(other.getId()));
    }

    @Override
    public int hashCode() {
        return OrderItem.class.hashCode();
    }
}

package org.tshop.catalog;

import org.tshop.shared.error.ConflictException;

public class InsufficientStockException extends ConflictException {

    private final Long productId;

    public InsufficientStockException(Product product, int requested) {
        super("Only %d unit(s) of '%s' left, %d requested".formatted(product.getStock(), product.getName(), requested));
        this.productId = product.getId();
    }

    public Long getProductId() {
        return productId;
    }
}

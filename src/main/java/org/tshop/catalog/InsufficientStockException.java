package org.tshop.catalog;

public class InsufficientStockException extends RuntimeException {

    private final Long productId;

    public InsufficientStockException(Product product, int requested) {
        super("Only %d unit(s) of '%s' left, %d requested".formatted(product.getStock(), product.getName(), requested));
        this.productId = product.getId();
    }

    public Long getProductId() {
        return productId;
    }
}

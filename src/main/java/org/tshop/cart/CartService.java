package org.tshop.cart;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tshop.catalog.InsufficientStockException;
import org.tshop.catalog.Product;
import org.tshop.catalog.ProductRepository;
import org.tshop.shared.error.NotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CartService {

    /** Same limit as CartItemRequest.quantity in openapi.yaml. */
    static final int MAX_QUANTITY = 99;

    private final CartItemRepository items;
    private final ProductRepository products;

    CartService(CartItemRepository items, ProductRepository products) {
        this.items = items;
        this.products = products;
    }

    /** The cart as the customer sees it, priced with the current product data. */
    public record CartView(List<Line> lines, BigDecimal total) {

        public boolean isEmpty() {
            return lines.isEmpty();
        }
    }

    public record Line(Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal subtotal,
                       boolean inStock) {
    }

    @Transactional(readOnly = true)
    public CartView get(Long customerId) {
        List<CartItem> cartItems = items.findByIdCustomerIdOrderByAddedAtAsc(customerId);
        // One query for all the products, instead of one per line
        Map<Long, Product> productsById = products.findAllById(cartItems.stream().map(CartItem::getProductId).toList())
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        List<Line> lines = cartItems.stream()
                .map(item -> toLine(item, productsById.get(item.getProductId())))
                .toList();
        BigDecimal total = lines.stream().map(Line::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartView(lines, total);
    }

    /** Adds the product or replaces its quantity. The stock is checked again at checkout. */
    @Transactional
    public CartView setItem(Long customerId, Long productId, int quantity) {
        Product product = products.findById(productId).orElseThrow(() -> new NotFoundException("Product", productId));
        if (!product.hasStock(quantity)) {
            throw new InsufficientStockException(product, quantity);
        }
        items.findById(new CartItemId(customerId, productId)).ifPresentOrElse(
                item -> item.changeQuantity(quantity),
                () -> items.save(new CartItem(customerId, productId, quantity)));
        return get(customerId);
    }

    public record GuestItem(Long productId, int quantity) {
    }

    /**
     * Adds the cart a visitor filled before logging in. Unlike {@link #setItem}, nothing is
     * rejected: the visitor can't fix anything at this point, so quantities are capped at the
     * stock and unavailable products are skipped. The cart then shows what was kept.
     */
    @Transactional
    public CartView merge(Long customerId, List<GuestItem> guestItems) {
        Map<Long, Integer> requested = guestItems.stream()
                .collect(Collectors.toMap(GuestItem::productId, GuestItem::quantity, Integer::sum));
        Map<Long, CartItem> existing = items.findByIdCustomerIdOrderByAddedAtAsc(customerId).stream()
                .collect(Collectors.toMap(CartItem::getProductId, Function.identity()));

        for (Product product : products.findAllById(requested.keySet())) {
            CartItem current = existing.get(product.getId());
            int wanted = requested.get(product.getId()) + (current == null ? 0 : current.getQuantity());
            int quantity = Math.min(Math.min(wanted, MAX_QUANTITY), product.getStock());
            if (quantity <= 0) {
                continue;
            }
            if (current == null) {
                items.save(new CartItem(customerId, product.getId(), quantity));
            } else {
                current.changeQuantity(quantity);
            }
        }
        return get(customerId);
    }

    @Transactional
    public CartView removeItem(Long customerId, Long productId) {
        items.deleteById(new CartItemId(customerId, productId));
        return get(customerId);
    }

    @Transactional
    public void clear(Long customerId) {
        items.deleteAllByCustomerId(customerId);
    }

    private static Line toLine(CartItem item, Product product) {
        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new Line(product.getId(), product.getName(), product.getPrice(), item.getQuantity(), subtotal,
                product.hasStock(item.getQuantity()));
    }
}

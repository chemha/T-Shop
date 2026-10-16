package org.tshop.order;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.tshop.cart.CartItem;
import org.tshop.cart.CartItemRepository;
import org.tshop.catalog.Product;
import org.tshop.catalog.ProductRepository;
import org.tshop.customer.Address;
import org.tshop.customer.AddressService;
import org.tshop.shared.error.ConflictException;
import org.tshop.shared.error.NotFoundException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    static final int MAX_CHECKOUT_ATTEMPTS = 3;
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"));

    private final OrderRepository orders;
    private final CartItemRepository cart;
    private final ProductRepository products;
    private final AddressService addresses;
    private final TransactionTemplate transaction;

    OrderService(OrderRepository orders, CartItemRepository cart, ProductRepository products, AddressService addresses,
                 TransactionTemplate transaction) {
        this.orders = orders;
        this.cart = cart;
        this.products = products;
        this.addresses = addresses;
        this.transaction = transaction;
    }

    public record Checkout(Long addressId, PaymentMethod paymentMethod, DeliveryMethod deliveryMethod) {
    }

    // ---------------------------------------------------------------- Checkout

    /**
     * Turns the cart into an order. If another checkout took the same products at the same time,
     * the optimistic lock makes this transaction fail; it is then retried from scratch with fresh
     * stock, so the customer either gets the order or a clear "not enough stock".
     * <p>
     * The retry loop must be outside the transaction: a failed transaction can't be reused, each
     * attempt needs a new one.
     */
    public Order placeOrder(Long customerId, Checkout checkout) {
        for (int attempt = 1; ; attempt++) {
            try {
                return Objects.requireNonNull(transaction.execute(status -> placeOrderOnce(customerId, checkout)));
            } catch (ObjectOptimisticLockingFailureException e) {
                if (attempt == MAX_CHECKOUT_ATTEMPTS) {
                    throw e;
                }
                log.info("Checkout of customer {} hit a concurrent stock update, retrying ({}/{})",
                        customerId, attempt, MAX_CHECKOUT_ATTEMPTS);
            }
        }
    }

    /** One attempt, all or nothing: stock, order and cart change together or not at all. */
    private Order placeOrderOnce(Long customerId, Checkout checkout) {
        List<CartItem> cartItems = cart.findByIdCustomerIdOrderByAddedAtAsc(customerId);
        if (cartItems.isEmpty()) {
            throw new ConflictException("The cart is empty");
        }
        Address address = addresses.get(customerId, checkout.addressId());
        Map<Long, Product> productsById = products.findAllById(cartItems.stream().map(CartItem::getProductId).toList())
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        Order order = new Order(customerId, checkout.paymentMethod(), checkout.deliveryMethod(), new ShippingAddress(
                address.getCountry(), address.getPostalCode(), address.getStreet(), address.getHouseNumber(), address.getApartment()));
        for (CartItem item : cartItems) {
            Product product = productsById.get(item.getProductId());
            if (product == null) {
                // Deleted between reading the cart and reading the products
                throw new ConflictException("A product in the cart is no longer available");
            }
            product.reserveStock(item.getQuantity()); // throws if there isn't enough: everything is rolled back
            order.addItem(product.getId(), product.getName(), product.getPrice(), item.getQuantity());
        }
        orders.save(order);
        cart.deleteAllByCustomerId(customerId);
        // Send the UPDATEs now, inside the try of placeOrder, so a version conflict surfaces here
        // as an exception we can retry (deleteAllByCustomerId already flushed, this is a safety net)
        orders.flush();
        log.info("Order {} placed by customer {}: {} line(s), total {}", order.getId(), customerId,
                order.getItems().size(), order.getTotal());
        return order;
    }

    // ---------------------------------------------------------------- Customer's orders

    @Transactional(readOnly = true)
    public Page<Order> listForCustomer(Long customerId, int page, int size) {
        return initialized(orders.findByCustomerId(customerId, PageRequest.of(page, size, NEWEST_FIRST)));
    }

    @Transactional(readOnly = true)
    public Order getForCustomer(Long customerId, Long orderId) {
        return initialized(findForCustomer(customerId, orderId));
    }

    /** Customers can only cancel orders that haven't been confirmed yet. */
    @Transactional
    public Order cancelForCustomer(Long customerId, Long orderId) {
        Order order = findForCustomer(customerId, orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ConflictException("Only pending orders can be cancelled. Please contact us.");
        }
        return changeStatus(order, OrderStatus.CANCELLED);
    }

    // ---------------------------------------------------------------- Admin

    @Transactional(readOnly = true)
    public Page<Order> listAll(@Nullable OrderStatus status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, NEWEST_FIRST);
        return initialized(status == null ? orders.findAll(pageRequest) : orders.findByStatus(status, pageRequest));
    }

    @Transactional(readOnly = true)
    public Order get(Long orderId) {
        return initialized(find(orderId));
    }

    @Transactional
    public Order changeStatus(Long orderId, OrderStatus next) {
        return changeStatus(find(orderId), next);
    }

    // ---------------------------------------------------------------- Helpers

    private Order changeStatus(Order order, OrderStatus next) {
        OrderStatus previous = order.getStatus();
        order.moveTo(next);
        if (next == OrderStatus.CANCELLED) {
            returnStock(order);
        }
        log.info("Order {} moved from {} to {}", order.getId(), previous, next);
        return initialized(order);
    }

    /** Products deleted since the order was placed are skipped: there's nothing to return them to. */
    private void returnStock(Order order) {
        List<Long> productIds = order.getItems().stream().map(OrderItem::getProductId).filter(Objects::nonNull).toList();
        Map<Long, Product> productsById = products.findAllById(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        for (OrderItem item : order.getItems()) {
            Product product = productsById.get(item.getProductId());
            if (product != null) {
                product.releaseStock(item.getQuantity());
            }
        }
    }

    private Order find(Long orderId) {
        return orders.findById(orderId).orElseThrow(() -> new NotFoundException("Order", orderId));
    }

    private Order findForCustomer(Long customerId, Long orderId) {
        return orders.findByIdAndCustomerId(orderId, customerId).orElseThrow(() -> new NotFoundException("Order", orderId));
    }

    /**
     * Loads the lazy order lines while the transaction is still open. With open-in-view disabled
     * the controller runs after the transaction, where touching an unloaded collection throws
     * LazyInitializationException. Batch fetching makes this one query for the whole page.
     */
    private static Order initialized(Order order) {
        order.getItems().size();
        return order;
    }

    private static Page<Order> initialized(Page<Order> page) {
        page.forEach(OrderService::initialized);
        return page;
    }
}

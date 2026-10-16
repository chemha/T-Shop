package org.tshop.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * An order and its lines. The lines belong to the order: they are saved and deleted with it and
 * only changed through it, which keeps the total always consistent with them.
 */
@Entity
@Table(name = "orders") // "order" is a reserved word in SQL
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    private DeliveryMethod deliveryMethod;

    @Embedded
    private ShippingAddress shippingAddress;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private List<OrderItem> items = new ArrayList<>();

    private BigDecimal total;

    @Version
    private Long version;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    protected Order() {
    }

    public Order(Long customerId, PaymentMethod paymentMethod, DeliveryMethod deliveryMethod, ShippingAddress shippingAddress) {
        this.customerId = Objects.requireNonNull(customerId);
        this.paymentMethod = Objects.requireNonNull(paymentMethod);
        this.deliveryMethod = Objects.requireNonNull(deliveryMethod);
        this.shippingAddress = Objects.requireNonNull(shippingAddress);
        this.status = OrderStatus.PENDING;
        this.paymentStatus = PaymentStatus.PENDING;
        this.total = BigDecimal.ZERO;
    }

    public void addItem(Long productId, String productName, BigDecimal unitPrice, int quantity) {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Items can only be added to a pending order");
        }
        OrderItem item = new OrderItem(productId, productName, unitPrice, quantity);
        items.add(item);
        total = total.add(item.subtotal());
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public DeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public BigDecimal getTotal() {
        return total;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Order other && id != null && id.equals(other.getId()));
    }

    @Override
    public int hashCode() {
        return Order.class.hashCode();
    }
}

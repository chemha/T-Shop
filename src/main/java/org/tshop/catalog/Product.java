package org.tshop.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // LAZY: the category is only loaded when needed. Queries that always need it
    // fetch it in the same SELECT (see ProductRepository)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    private String name;

    private BigDecimal price;

    private String brand;

    private String color;

    private BigDecimal weightKg;

    // The naming strategy would map "volumeL" to "volumel": a trailing single capital isn't split
    @Column(name = "volume_l")
    private BigDecimal volumeL;

    private int stock;

    /**
     * Optimistic locking. Every UPDATE runs {@code ... WHERE id = ? AND version = ?} and increments
     * the version. If another transaction changed the row in the meantime, no row matches and
     * Hibernate throws instead of silently overwriting that change.
     */
    @Version
    private Long version;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    protected Product() {
    }

    public Product(Category category, String name, BigDecimal price, int stock) {
        this.category = Objects.requireNonNull(category);
        this.name = Objects.requireNonNull(name);
        changePrice(price);
        changeStock(stock);
    }

    public void update(Category category, String name, BigDecimal price, String brand, String color,
                       BigDecimal weightKg, BigDecimal volumeL, int stock) {
        this.category = Objects.requireNonNull(category);
        this.name = Objects.requireNonNull(name);
        changePrice(price);
        describe(brand, color, weightKg, volumeL);
        changeStock(stock);
    }

    public void describe(String brand, String color, BigDecimal weightKg, BigDecimal volumeL) {
        this.brand = brand;
        this.color = color;
        this.weightKg = weightKg;
        this.volumeL = volumeL;
    }

    public void changePrice(BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException("Price must be zero or positive");
        }
        this.price = price;
    }

    public void changeStock(int stock) {
        if (stock < 0) {
            throw new IllegalArgumentException("Stock can't be negative");
        }
        this.stock = stock;
    }

    public boolean hasStock(int quantity) {
        return stock >= quantity;
    }

    /** Takes units out of stock for an order. */
    public void reserveStock(int quantity) {
        requirePositive(quantity);
        if (!hasStock(quantity)) {
            throw new InsufficientStockException(this, quantity);
        }
        stock -= quantity;
    }

    /** Puts units back, e.g. when an order is cancelled. */
    public void releaseStock(int quantity) {
        requirePositive(quantity);
        stock += quantity;
    }

    private static void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public Long getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getBrand() {
        return brand;
    }

    public String getColor() {
        return color;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public BigDecimal getVolumeL() {
        return volumeL;
    }

    public int getStock() {
        return stock;
    }

    public Long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Product other && id != null && id.equals(other.getId()));
    }

    @Override
    public int hashCode() {
        return Product.class.hashCode();
    }
}

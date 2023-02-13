package org.tshop.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product")
@Getter
@Setter
public class Product {
    @Id
    @Column(name = "product_id", columnDefinition = "INT UNSIGNED not null")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoy_id", nullable = false)
    private Category categoy;

    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "brand", length = 30)
    private String brand;

    @Column(name = "color", length = 30)
    private String color;

    @Column(name = "weight", precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(name = "volume", precision = 10, scale = 2)
    private BigDecimal volume;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}
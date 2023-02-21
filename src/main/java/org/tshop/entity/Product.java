package org.tshop.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INT UNSIGNED not null")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

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

    @OneToMany(mappedBy = "product")
    private Set<Good> goods = new LinkedHashSet<>();

}
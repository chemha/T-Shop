package org.tshop.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "product")
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INT UNSIGNED not null")
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Size(max = 50)
    @NotNull
    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @NotNull
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private Float price;

    @Size(max = 30)
    @Column(name = "brand", length = 30)
    private String brand;

    @Size(max = 30)
    @Column(name = "color", length = 30)
    private String color;

    @Column(name = "weight", precision = 10, scale = 2)
    private Float weight;

    @Column(name = "volume", precision = 10, scale = 2)
    private Float volume;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @OneToMany(mappedBy = "product")
    private Set<GoodEntity> goods = new LinkedHashSet<>();

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof ProductEntity)) {
            return false;
        }

        ProductEntity p = (ProductEntity) o;
        return Objects.equals(id, p.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
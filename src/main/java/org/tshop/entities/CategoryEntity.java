package org.tshop.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "category")
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INT UNSIGNED not null")
    private Integer id;

    @Size(max = 45)
    @NotNull
    @Column(name = "title", nullable = false, length = 45)
    private String title;

    @OneToMany(mappedBy = "category")
    private Set<ProductEntity> products = new LinkedHashSet<>();

}
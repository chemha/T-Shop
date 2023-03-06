package org.tshop.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Category")
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INT UNSIGNED not null")
    private Long id;

    @Column(name = "title", nullable = false, length = 45)
    private String title;

    @OneToMany(mappedBy = "category")
    private Set<ProductEntity> productEntities = new LinkedHashSet<>();

}
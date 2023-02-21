package org.tshop.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INT UNSIGNED not null")
    private Long id;

    @Column(name = "title", nullable = false, length = 45)
    private String title;

    @OneToMany(mappedBy = "category")
    private Set<Product> products = new LinkedHashSet<>();

}
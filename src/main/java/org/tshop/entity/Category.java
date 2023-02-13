package org.tshop.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "category")
@Getter
@Setter
public class Category {
    @Id
    @Column(name = "category_id", columnDefinition = "INT UNSIGNED not null")
    private Long id;

    @Column(name = "title", nullable = false, length = 45)
    private String title;
}
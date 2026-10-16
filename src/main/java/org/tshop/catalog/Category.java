package org.tshop.catalog;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    protected Category() {
    }

    public Category(String name) {
        rename(name);
    }

    public void rename(String name) {
        this.name = Objects.requireNonNull(name).strip();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Category other && id != null && id.equals(other.getId()));
    }

    @Override
    public int hashCode() {
        return Category.class.hashCode();
    }
}

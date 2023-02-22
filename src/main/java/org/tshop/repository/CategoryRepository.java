package org.tshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tshop.entity.Category;
import org.tshop.entity.Client;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findCategoryById(long id);
    Optional<Category> findCategoryByTitle(String title);
}

package org.tshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tshop.entity.Category;
import org.tshop.entity.Product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findProductById(long id);

}

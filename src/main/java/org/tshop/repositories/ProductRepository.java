package org.tshop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tshop.entities.ProductEntity;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Integer> {
    Optional<ProductEntity> findProductById(Integer id);
}

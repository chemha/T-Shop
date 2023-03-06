package org.tshop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tshop.entities.OrderEntity;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findProductById(Long id);

}

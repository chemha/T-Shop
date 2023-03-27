package org.tshop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tshop.entities.GoodEntity;

import java.util.List;
import java.util.Optional;

public interface GoodRepository extends JpaRepository<GoodEntity, Integer> {
    List<GoodEntity> findByOrderId(Integer order_id);
}

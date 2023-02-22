package org.tshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tshop.entity.Category;
import org.tshop.entity.Good;
import org.tshop.entity.Order;

import java.util.Optional;
import java.util.UUID;

public interface GoodRepository extends JpaRepository<Good, UUID> {
    Optional<Good> findGoodById(long id);

}

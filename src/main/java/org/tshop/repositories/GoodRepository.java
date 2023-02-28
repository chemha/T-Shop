package org.tshop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tshop.entities.Good;

import java.util.Optional;
import java.util.UUID;

public interface GoodRepository extends JpaRepository<Good, Long> {
    Optional<Good> findGoodById(Long id);

}

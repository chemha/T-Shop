package org.tshop.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Order lines are loaded lazily and in batches (hibernate.default_batch_fetch_size in
// application.yaml): a page of 20 orders needs 1 query for the orders and 1 for all their lines.
// A JOIN FETCH would not work here: Hibernate can't apply LIMIT to a query that fetches a
// collection, so it would load every order into memory and paginate there.
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    Optional<Order> findByIdAndCustomerId(Long id, Long customerId);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    boolean existsByCustomerId(Long customerId);
}

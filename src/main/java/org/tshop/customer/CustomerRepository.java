package org.tshop.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /** Expects an email already normalized with {@link Customer#normalizeEmail(String)}. */
    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);
}

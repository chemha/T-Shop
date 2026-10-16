package org.tshop.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByCustomerIdOrderById(Long customerId);

    /**
     * Looks an address up by id <em>and</em> owner. Someone else's address is simply "not found",
     * which also hides whether that id exists.
     */
    Optional<Address> findByIdAndCustomerId(Long id, Long customerId);
}

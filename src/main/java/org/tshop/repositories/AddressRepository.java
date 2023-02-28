package org.tshop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tshop.entities.ClientAddress;

import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<ClientAddress, Long> {
    Optional<ClientAddress> findAddressById(Long id);

}

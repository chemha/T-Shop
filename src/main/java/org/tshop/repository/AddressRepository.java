package org.tshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tshop.entity.Client;
import org.tshop.entity.ClientAddress;

import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<ClientAddress, UUID> {
    Optional<ClientAddress> findAddressById(int id);

}

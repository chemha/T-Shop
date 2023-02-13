package org.tshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tshop.entity.ClientAddress;

import java.util.Optional;
import java.util.UUID;

public interface AdressRepository extends JpaRepository<ClientAddress, UUID> {
    Optional<ClientAddress> findAdressById(int id);
}

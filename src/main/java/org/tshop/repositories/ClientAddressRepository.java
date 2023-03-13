package org.tshop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tshop.entities.ClientAddressEntity;

import java.util.Optional;

public interface ClientAddressRepository extends JpaRepository<ClientAddressEntity, Integer> {
}

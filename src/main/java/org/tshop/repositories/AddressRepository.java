package org.tshop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tshop.entities.ClientAddressEntity;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<ClientAddressEntity, Long> {
    Optional<ClientAddressEntity> findAddressById(Long id);

}

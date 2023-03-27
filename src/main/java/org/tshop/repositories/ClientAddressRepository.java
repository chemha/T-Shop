package org.tshop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tshop.entities.ClientAddressEntity;
import org.tshop.entities.ClientEntity;

import java.util.List;
import java.util.Optional;

public interface ClientAddressRepository extends JpaRepository<ClientAddressEntity, Integer> {
    List<ClientAddressEntity> findByClientId(Integer client_id);
}

package org.tshop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tshop.entities.ClientEntity;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<ClientEntity, Long> {
    Optional<ClientEntity> findClientById(Long id);
    Optional<ClientEntity> findClientByEmail(String email);

}

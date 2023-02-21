package org.tshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tshop.entity.Client;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    Optional<Client> findClientById(long id);
    Optional<Client> findClientByEmail(String email);
    Optional<Client> findClientByName(String name);
    Optional<Client> findClientBySurname(String surname);
}

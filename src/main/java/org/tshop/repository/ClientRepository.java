package org.tshop.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.tshop.entity.Client;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends PagingAndSortingRepository<Client, UUID> {
    Optional<Client> findClientById(int id);
    Optional<Client> findClientByEmail(String email);
    Optional<Client> findClientByName(String name);
    Optional<Client> findClientBySurname(String surname);
}

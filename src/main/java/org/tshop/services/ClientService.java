package org.tshop.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.tshop.entities.Client;
import org.tshop.entities.ClientAddress;
import org.tshop.repositories.AddressRepository;
import org.tshop.repositories.ClientRepository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {
    private final ClientRepository clientRepository;
    private final AddressRepository addressRepository;
    @Transactional
    public void createClient(final String name, final String surname, final LocalDate birthdate, final String email, final String password, final boolean admin) {
        final Client client = new Client();
        client.setName(name);
        client.setSurname(surname);
        client.setBirthdate(birthdate);
        client.setEmail(email);
        client.setPassword(password);
        client.setAdmin(admin);
        clientRepository.save(client);
    }

    @Transactional
    public void updateClient(final Client client) {
        clientRepository.save(client);
    }

    @Transactional
    public void delete(Long id) {
        Client client = findById(id);
        clientRepository.delete(client);
    }

    @Transactional
    public Collection<ClientAddress> findAllClientAddress(final Long id) {
        final Optional<Client> clientById = clientRepository.findClientById(id);
        if (clientById.isPresent()) {
            Hibernate.initialize(clientById.get().getClientAddresses());
            return clientById.get().getClientAddresses();
        } else {
            return Collections.emptyList();
        }
    }

    @Transactional
    public Client findByEmail(final String email) {
        return clientRepository.findClientByEmail(email).orElse(null);
    }

    @Transactional
    public Client findById(final Long id) {
        return clientRepository.findClientById(id).orElse(null);
    }
}

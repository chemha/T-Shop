package org.tshop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.tshop.entity.Client;
import org.tshop.entity.ClientAddress;
import org.tshop.repository.AddressRepository;
import org.tshop.repository.ClientRepository;

import javax.transaction.Transactional;
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
    public void createClient(final String email, final String password) {
        final Client client = new Client();
        client.setEmail(email);
        client.setPassword(password);
        clientRepository.save(client);
    }

    @Transactional
    public void modifyClient(final Client client) {
        clientRepository.save(client);
    }
    @Transactional
    public Collection<ClientAddress> findAllClientAddress(final int id) {
        final Optional<Client> clientById = clientRepository.findClientById(id);
        if (clientById.isPresent()) {
            Hibernate.initialize(clientById.get().getClientAddresses());
            return clientById.get().getClientAddresses();
        } else {
            return Collections.emptyList();
        }
    }

    @Transactional
    public Client findClientByEmail(final String email) {
        return clientRepository.findClientByEmail(email).orElse(null);
    }
}

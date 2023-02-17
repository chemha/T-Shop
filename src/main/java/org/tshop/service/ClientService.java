package org.tshop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.tshop.entity.Client;
import org.tshop.entity.ClientAddress;
import org.tshop.repository.AdressRepository;
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
    private final AdressRepository adressRepository;
    @Transactional
    public Client createClient(final String email, final String password) {
        final Client client = new Client();
        client.setEmail(email);
        client.setPassword(password);
        clientRepository.save(client);
        return client;
    }
    @Transactional
    public Collection<ClientAddress> findAllClientAddress(final int id) {
        final Optional<Client> clientById = clientRepository.findClientById(id);
        if (clientById.isPresent()) {
            Hibernate.initialize(clientById.get().getAddresses());
            return clientById.get().getAddresses();
        } else {
            return Collections.emptyList();
        }
    }

}

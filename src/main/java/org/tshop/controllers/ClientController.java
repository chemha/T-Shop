package org.tshop.controllers;

import lombok.RequiredArgsConstructor;
import org.tshop.api.ClientsApi;
import org.tshop.models.Client;
import org.tshop.models.ClientProperties;
import org.tshop.models.Clients;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.tshop.services.ClientService;

@RestController
@RequiredArgsConstructor
public class ClientController implements ClientsApi {
    private final ClientService clientService;
    @Override
    public ResponseEntity<Void> createClient(ClientProperties body) {
        return null;
    }

    @Override
    public ResponseEntity<Void> deleteClient(Long id) {
        clientService.deleteClient(clientService.findClientById(id););
    }

    @Override
    public ResponseEntity<Client> getClient(Long id) {
        return null;
    }

    @Override
    public ResponseEntity<Clients> listClients(Integer pageNumber) {
        return null;
    }

    @Override
    public ResponseEntity<Client> updateClient(Long id, ClientProperties body) {
        return null;
    }
}

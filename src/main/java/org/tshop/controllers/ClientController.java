package org.tshop.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tshop.api.ClientsApi;
import org.tshop.dto.ClientDto;
import org.tshop.dto.ClientPropertiesDto;
import org.tshop.dto.ClientSearchResultDto;
import org.tshop.dto.ClientSearchResultDto;
import org.tshop.entities.ClientEntity;
import org.tshop.mappers.ClientMapper;
import org.tshop.repositories.ClientRepository;
import org.tshop.services.ClientService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClientController implements ClientsApi {
    private final ClientService clientService;

    @Override
    @PostMapping("/clients")
    public ResponseEntity<Void> createClient(@RequestBody ClientPropertiesDto properties) {
        clientService.create(properties);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @DeleteMapping("/clients/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @Override
    @GetMapping("/clients/{id}")
    public ResponseEntity<ClientDto> getClient(@PathVariable Long id) {
        ClientDto dto = clientService.get(id);
        return ResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<ClientSearchResultDto> getClientSearchResult(Integer pageNumber, Integer pageSize) {
        ClientSearchResultDto dto = clientService.getSearchResult(pageNumber, pageSize);
        return ResponseEntity.ok().body(dto);
    }

    @Override
    @PutMapping("/clients/{id}")
    public ResponseEntity<ClientDto> updateClient(Long id, ClientPropertiesDto properties) {
        final ClientDto dto = clientService.update(id, properties);
        return ResponseEntity.ok().body(dto);
    }
}

package org.tshop.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tshop.api.ClientsApi;
import org.tshop.dto.ClientDto;
import org.tshop.dto.ClientPropertiesDto;
import org.tshop.dto.ClientSearchResultDto;
import org.tshop.services.ClientService;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clients")
public class ClientController implements ClientsApi {
    private final ClientService clientService;

    @Override
    @PostMapping
    public ResponseEntity<Void> createClient(@RequestBody ClientPropertiesDto properties) {
        clientService.create(properties);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Integer id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getClient(@PathVariable Integer id) {
        ClientDto dto = clientService.get(id);
        return ResponseEntity.ok(dto);
    }

    @Override
    @GetMapping
    public ResponseEntity<ClientSearchResultDto> getClientSearchResult(Integer pageNumber, Integer pageSize) {
        ClientSearchResultDto dto = clientService.getSearchResult(pageNumber, pageSize);
        return ResponseEntity.ok().body(dto);
    }

    @Override
    @PutMapping("{id}")
    public ResponseEntity<ClientDto> updateClient(Integer id, ClientPropertiesDto properties) {
        final ClientDto dto = clientService.update(id, properties);
        return ResponseEntity.ok().body(dto);
    }
}

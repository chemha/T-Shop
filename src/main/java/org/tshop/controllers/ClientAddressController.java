package org.tshop.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tshop.api.ClientAddressesApi;
import org.tshop.dto.*;
import org.tshop.services.ClientAddressService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/client-addresses")
public class ClientAddressController implements ClientAddressesApi {
    private final ClientAddressService service;

    @Override
    public ResponseEntity<Void> createClientAddress(ClientAddressPropertiesDto properties) {
        service.create(properties);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Void> deleteClientAddress(Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ClientAddressDto> getClientAddress(Integer id) {
        ClientAddressDto dto = service.get(id);
        return ResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<ClientAddressSearchResultDto> getClientAddressSearchResult(Integer pageNumber, Integer pageSize) {
        ClientAddressSearchResultDto dto = service.getSearchResult(pageNumber, pageSize);
        return ResponseEntity.ok().body(dto);
    }

    @Override
    public ResponseEntity<ClientAddressDto> updateClientAddress(Integer id, ClientAddressPropertiesDto properties) {
        final ClientAddressDto dto = service.update(id, properties);
        return ResponseEntity.ok().body(dto);
    }
}

package org.tshop.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.tshop.api.ClientAddressesApi;
import org.tshop.dto.*;
import org.tshop.services.ClientAddressService;

import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
@RequestMapping("/client-addresses")
public class ClientAddressController implements ClientAddressesApi {
    private final ClientAddressService service;

    @GetMapping("index")
    public String index(HttpSession session, Model model) {
        model.addAttribute("addresses", service.findByClientId((Integer) session.getAttribute("userId")));
        return "addresses/index";
    }

    @PostMapping
    @Override
    public ResponseEntity<ClientAddressDto> createClientAddress(@RequestBody ClientAddressPropertiesDto properties) {
        final ClientAddressDto dto = service.create(properties);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> deleteClientAddress(@PathVariable Integer id) {
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
    @PutMapping("/{id}")
    @Override
    public ResponseEntity<ClientAddressDto> updateClientAddress(@PathVariable Integer id, @RequestBody ClientAddressPropertiesDto properties) {
        final ClientAddressDto dto = service.update(id, properties);
        return ResponseEntity.ok().body(dto);
    }
}

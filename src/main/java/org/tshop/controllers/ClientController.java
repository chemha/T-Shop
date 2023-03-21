package org.tshop.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.tshop.api.ClientsApi;
import org.tshop.dto.ClientDto;
import org.tshop.dto.ClientPropertiesDto;
import org.tshop.dto.ClientSearchResultDto;
import org.tshop.entities.ClientEntity;
import org.tshop.services.ClientService;

import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
@RequestMapping("/clients")
public class ClientController implements ClientsApi {
    private final ClientService service;

    @GetMapping("/login")
    public String login() {
        return "clients/login";
    }

    @PostMapping("/login")
    public String singIn(final ClientEntity client, final HttpSession session) {
        ClientEntity clientEntity = service.findByEmail(client.getEmail());

        session.setAttribute("userId", clientEntity.getId());
        session.setAttribute("admin", clientEntity.getAdmin());
        session.setAttribute("userName", clientEntity.getName());
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/register")
    public String register() {
        return "clients/register";
    }

    @Override
    @PostMapping
    public ResponseEntity<ClientDto> createClient(@RequestBody ClientPropertiesDto properties) {
        final ClientDto dto = service.create(properties);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }


    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getClient(@PathVariable Integer id) {
        ClientDto dto = service.get(id);
        return ResponseEntity.ok(dto);
    }

    @Override
    @GetMapping
    public ResponseEntity<ClientSearchResultDto> getClientSearchResult( Integer pageNumber, Integer pageSize) {
        ClientSearchResultDto dto = service.getSearchResult(pageNumber, pageSize);
        return ResponseEntity.ok().body(dto);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<ClientDto> updateClient(@PathVariable Integer id, ClientPropertiesDto properties) {
        final ClientDto dto = service.update(id, properties);
        return ResponseEntity.ok().body(dto);
    }
}

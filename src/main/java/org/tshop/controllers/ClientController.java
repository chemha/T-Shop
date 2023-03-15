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
import java.security.Principal;
import java.time.LocalDate;

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
    public String access(final ClientEntity client, final HttpSession session) {
        ClientEntity clientEntity = service.findByEmail(client.getEmail());
        session.setAttribute("id", clientEntity.getId());
        session.setAttribute("admin", clientEntity.getAdmin());
        return "redirect:/";
    }

    @GetMapping("/register")
    public String register() {
        return "clients/register";
    }

    @Override
    @PostMapping
    public ResponseEntity<Void> createClient(@RequestBody final ClientPropertiesDto properties) {
        service.create(properties);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable final Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }


    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getClient(@PathVariable final Integer id) {
        ClientDto dto = service.get(id);
        return ResponseEntity.ok(dto);
    }

    @Override
    @GetMapping
    public ResponseEntity<ClientSearchResultDto> getClientSearchResult(final Integer pageNumber, final Integer pageSize) {
        ClientSearchResultDto dto = service.getSearchResult(pageNumber, pageSize);
        return ResponseEntity.ok().body(dto);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<ClientDto> updateClient(@PathVariable final Integer id, final ClientPropertiesDto properties) {
        final ClientDto dto = service.update(id, properties);
        return ResponseEntity.ok().body(dto);
    }
}

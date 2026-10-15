package org.tshop.controllers;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.tshop.api.ClientsApi;
import org.tshop.dto.ClientDto;
import org.tshop.dto.ClientPropertiesDto;
import org.tshop.dto.ClientSearchResultDto;
import org.tshop.entities.ClientEntity;
import org.tshop.repositories.ClientRepository;
import org.tshop.services.ClientService;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/clients")
public class ClientController implements ClientsApi {
    private final ClientService service;
    private final ClientRepository repository;
    private final Logger logger = LoggerFactory.getLogger(ClientController.class);

    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("clients", service.findAll());
        return "clients/index";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        ClientEntity entity = service.loadEntity((Integer)session.getAttribute("userId"));
        String birthdate = entity.getBirthdate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        model.addAttribute("client", entity);
        model.addAttribute("birthdate", birthdate);
        return "clients/profile";
    }

    @PostMapping("/edit")
    public String edit(HttpSession session, @RequestParam String name, @RequestParam String surname, @RequestParam String birthdate, @RequestParam String email) {
        ClientEntity client = service.loadEntity((Integer) session.getAttribute("userId"));
        client.setName(name);
        client.setSurname(surname);
        client.setBirthdate(LocalDate.parse(birthdate));
        client.setEmail(email);

        repository.save(client);

        logger.info("Client with id {} has been updated", client.getId());

        return "redirect:/clients/profile";
    }

    @PostMapping("/password")
    public String edit(HttpSession session, @RequestParam String password) {
        service.changePassword((Integer) session.getAttribute("userId"), password);
        return "redirect:/clients/profile";
    }

    @GetMapping("/login")
    public String login() {
        return "clients/login";
    }

    // POST /clients/login and /clients/logout are handled by Spring Security (see SecurityConfiguration)

    @GetMapping("/register")
    public String register() {
        return "clients/register";
    }

    @Override
    @PostMapping
    public ResponseEntity<ClientDto> createClient(@RequestBody ClientPropertiesDto properties) {
        // Only an admin may create other admins; public registration always creates regular clients
        Object isAdmin = RequestContextHolder.currentRequestAttributes().getAttribute("admin", RequestAttributes.SCOPE_SESSION);
        if (!Boolean.TRUE.equals(isAdmin)) properties.setAdmin(false);

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

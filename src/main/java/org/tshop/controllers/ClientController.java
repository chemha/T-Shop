package org.tshop.controllers;

import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.tshop.api.ClientsApi;
import org.tshop.dto.ClientDto;
import org.tshop.dto.ClientPropertiesDto;
import org.tshop.dto.ClientSearchResultDto;
import org.tshop.entities.ClientEntity;
import org.tshop.entities.ProductEntity;
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
    @Autowired
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

    @PostMapping("/login")
    public String singIn(final ClientEntity client, final HttpSession session) {
        ClientEntity clientEntity = service.findByEmail(client.getEmail());

        session.setAttribute("userId", clientEntity.getId());
        session.setAttribute("admin", clientEntity.getAdmin());
        session.setAttribute("userName", clientEntity.getName());

        /*
        Authentication authentication = new UsernamePasswordAuthenticationToken(clientEntity, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        */

        logger.info("Client with email {} has logged in", client.getEmail());

        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.setAttribute("userId", null);
        session.setAttribute("admin", null);
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

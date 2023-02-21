package org.tshop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.tshop.entity.Client;
import org.tshop.repository.ClientRepository;
import org.tshop.service.AddressService;
import org.tshop.service.ClientService;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class Main implements CommandLineRunner {

    private final ClientService clientservice;
    private final ClientRepository clientRepository;
    private final AddressService addressService;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        final String email = "test42";
        log.info("Joining thread, you can press Ctrl+C to shutdown application");
        log.info("Inserting client...");
        clientservice.createClient(email, "test1");
        log.info("Finding client...");
        Client found = clientservice.findClientByEmail(email);
        log.info("If found, email is:" + found.getEmail() + "and password is" + found.getPassword());
        log.info("Modifiying...");
        clientservice.modifyClient(found);
        found = clientservice.findClientByEmail(email);
        log.info("If found, email is:" + found.getEmail() + "and password is" + found.getPassword());


        Thread.currentThread().join();
    }
}

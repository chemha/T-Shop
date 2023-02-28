package org.tshop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.tshop.entities.Client;
import org.tshop.entities.ClientAddress;
import org.tshop.repositories.ClientRepository;
import org.tshop.services.ClientAddressService;
import org.tshop.services.ClientService;

import java.util.Set;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class Main implements CommandLineRunner {

    private final ClientService clientservice;
    private final ClientRepository clientRepository;
    private final ClientAddressService clientAddressService;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Thread.currentThread().join();
    }

    public void testAddress() {
        Set<ClientAddress> addresses = clientservice.findClientByEmail("testingAddress").getClientAddresses();

        for (ClientAddress value : addresses) {
            log.info(value.getStreet());
        }
    }

    public void test() {
        final String email = "testingAddress";
        log.info("Joining thread, you can press Ctrl+C to shutdown application");
        log.info("Inserting client...");
        clientservice.createClient(email, "test1");
        log.info("Finding client...");
        Client found = clientservice.findClientByEmail(email);
        log.info("If found, email is:" + found.getEmail() + "and password is" + found.getPassword());
        log.info("Modifiying...");
        found.setPassword("modified");
        clientservice.updateClient(found);
        found = clientservice.findClientByEmail(email);
        log.info("If found, email is:" + found.getEmail() + "and password is" + found.getPassword());
        log.info("Creating address...");
        clientAddressService.createAddress(found, "test", "1234", "test", null, null);

    }
}

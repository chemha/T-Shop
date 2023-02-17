package com.example.springdatalecture;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.tshop.entity.ClientAddress;
import org.tshop.service.AddressService;
import org.tshop.service.ClientService;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class Main implements CommandLineRunner {

    private final ClientService clientservice;
    private final AddressService addressService;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Joining thread, you can press Ctrl+C to shutdown application");
        addressService.createAddress(clientservice.createClient("prueba1", "prueba1"), "prueba1", "1234", "prueba1");

        for (final ClientAddress address : clientservice.findAllClientAddress(1)) {
            log.info(address.getStreet());
        }

        Thread.currentThread().join();
    }
}

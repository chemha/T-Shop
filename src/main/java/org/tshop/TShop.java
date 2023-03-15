package org.tshop;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class TShop {
    public static void main(String[] args) {
        SpringApplication.run(TShop.class, args);
    }
}

package org.tshop;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tshop.auth.TokenService;
import org.tshop.customer.Customer;
import org.tshop.customer.CustomerRepository;
import org.tshop.customer.Role;

import java.time.LocalDate;
import java.util.UUID;

/** Creates customers straight in the database, each with a unique email and a valid token. */
@TestComponent
public class TestAccounts {

    public static final String PASSWORD = "correct-horse-battery";

    private final CustomerRepository customers;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;

    TestAccounts(CustomerRepository customers, PasswordEncoder passwordEncoder, TokenService tokens) {
        this.customers = customers;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    public record Account(Long id, String email, String token) {

        public String bearer() {
            return "Bearer " + token;
        }
    }

    public Account customer() {
        return create(Role.CUSTOMER);
    }

    public Account admin() {
        return create(Role.ADMIN);
    }

    public static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    private Account create(Role role) {
        Customer customer = new Customer("Test", "User", LocalDate.of(1990, 1, 1), uniqueEmail(), passwordEncoder.encode(PASSWORD));
        customer.changeRole(role);
        customer = customers.save(customer);
        return new Account(customer.getId(), customer.getEmail(), tokens.issue(customer).value());
    }
}

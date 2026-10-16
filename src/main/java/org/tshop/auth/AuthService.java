package org.tshop.auth;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tshop.customer.Customer;
import org.tshop.customer.CustomerRepository;

import java.util.Optional;

@Service
class AuthService {

    private final CustomerRepository customers;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;
    /** Hash of a random value, compared against when the email doesn't exist. */
    private final String dummyHash;

    AuthService(CustomerRepository customers, PasswordEncoder passwordEncoder, TokenService tokens) {
        this.customers = customers;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
        this.dummyHash = passwordEncoder.encode("dummy-password-for-timing");
    }

    @Transactional(readOnly = true)
    TokenService.Token login(String email, String password) {
        Optional<Customer> customer = customers.findByEmail(Customer.normalizeEmail(email));
        // BCrypt is slow on purpose (~100 ms). If unknown emails returned immediately, an attacker
        // could find out which emails are registered by timing the responses, so the password is
        // always checked, against a dummy hash if needed
        String hash = customer.map(Customer::getPasswordHash).orElse(dummyHash);
        boolean passwordMatches = passwordEncoder.matches(password, hash);
        if (customer.isEmpty() || !passwordMatches) {
            // Same message in both cases, for the same reason
            throw new BadCredentialsException("Invalid email or password");
        }
        return tokens.issue(customer.get());
    }
}

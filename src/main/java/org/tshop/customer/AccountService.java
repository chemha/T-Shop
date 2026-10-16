package org.tshop.customer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tshop.shared.error.InvalidFieldException;
import org.tshop.shared.error.NotFoundException;

import java.time.LocalDate;

/** Registration and the customer's own profile. */
@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final CustomerRepository customers;
    private final PasswordEncoder passwordEncoder;

    AccountService(CustomerRepository customers, PasswordEncoder passwordEncoder) {
        this.customers = customers;
        this.passwordEncoder = passwordEncoder;
    }

    public record ProfileData(String firstName, String lastName, LocalDate birthDate, String email) {
    }

    /** Public registration always creates a CUSTOMER; only an admin can promote someone. */
    @Transactional
    public Customer register(ProfileData profile, String password) {
        String email = Customer.normalizeEmail(profile.email());
        if (customers.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }
        Customer customer = customers.save(new Customer(profile.firstName(), profile.lastName(), profile.birthDate(),
                email, passwordEncoder.encode(password)));
        log.info("Customer {} registered", customer.getId());
        return customer;
    }

    @Transactional(readOnly = true)
    public Customer get(Long customerId) {
        return customers.findById(customerId).orElseThrow(() -> new NotFoundException("Customer", customerId));
    }

    @Transactional
    public Customer updateProfile(Long customerId, ProfileData profile) {
        Customer customer = get(customerId);
        String email = Customer.normalizeEmail(profile.email());
        if (!email.equals(customer.getEmail()) && customers.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }
        // No save() needed: the entity is managed, so Hibernate writes the changes on commit
        customer.updateProfile(profile.firstName(), profile.lastName(), profile.birthDate(), email);
        return customer;
    }

    @Transactional
    public void changePassword(Long customerId, String currentPassword, String newPassword) {
        Customer customer = get(customerId);
        if (!passwordEncoder.matches(currentPassword, customer.getPasswordHash())) {
            throw new InvalidFieldException("currentPassword", "is incorrect");
        }
        customer.changePasswordHash(passwordEncoder.encode(newPassword));
        log.info("Customer {} changed their password", customerId);
    }
}

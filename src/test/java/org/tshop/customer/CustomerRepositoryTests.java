package org.tshop.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.tshop.PersistenceTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@PersistenceTest
class CustomerRepositoryTests {

    @Autowired
    CustomerRepository customers;

    @Autowired
    AddressRepository addresses;

    @Autowired
    TestEntityManager em;

    @Test
    void emailIsStoredInLowerCase() {
        customers.save(customer("  Ana.Garcia@Example.COM "));

        assertThat(customers.findByEmail("ana.garcia@example.com"))
                .hasValueSatisfying(c -> {
                    assertThat(c.getRole()).isEqualTo(Role.CUSTOMER);
                    assertThat(c.getCreatedAt()).isNotNull();
                });
    }

    @Test
    void emailMustBeUnique() {
        customers.saveAndFlush(customer("ana@example.com"));

        // The database's UNIQUE constraint is the last line of defence, even if two
        // registrations with the same email arrive at the same time
        assertThatThrownBy(() -> customers.saveAndFlush(customer("ANA@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void addressesAreOnlyFoundThroughTheirOwner() {
        Customer ana = customers.save(customer("ana@example.com"));
        Customer bob = customers.save(customer("bob@example.com"));
        Address anasAddress = addresses.save(new Address(ana.getId(), "Spain", "28001", "Gran Via", "1", "2B"));
        em.flush();

        assertThat(addresses.findByIdAndCustomerId(anasAddress.getId(), ana.getId())).isPresent();
        assertThat(addresses.findByIdAndCustomerId(anasAddress.getId(), bob.getId())).isEmpty();
        assertThat(addresses.findByCustomerIdOrderById(bob.getId())).isEmpty();
    }

    @Test
    void deletingACustomerDeletesTheirAddresses() {
        Customer ana = customers.save(customer("ana@example.com"));
        Address address = addresses.save(new Address(ana.getId(), "Spain", "28001", "Gran Via", "1", null));
        em.flush();

        customers.delete(ana);
        em.flush();
        em.clear();

        // ON DELETE CASCADE in the schema
        assertThat(addresses.findById(address.getId())).isEmpty();
    }

    static Customer customer(String email) {
        return new Customer("Ana", "García", LocalDate.of(1990, 5, 17), email, "{noop}not-a-real-hash");
    }
}

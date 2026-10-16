package org.tshop.customer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;

@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private LocalDate birthDate;

    private String email;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    /** Required by JPA. */
    protected Customer() {
    }

    public Customer(String firstName, String lastName, LocalDate birthDate, String email, String passwordHash) {
        updateProfile(firstName, lastName, birthDate, email);
        changePasswordHash(passwordHash);
        this.role = Role.CUSTOMER;
    }

    public void updateProfile(String firstName, String lastName, LocalDate birthDate, String email) {
        this.firstName = Objects.requireNonNull(firstName);
        this.lastName = Objects.requireNonNull(lastName);
        this.birthDate = Objects.requireNonNull(birthDate);
        this.email = normalizeEmail(email);
    }

    public void changePasswordHash(String passwordHash) {
        this.passwordHash = Objects.requireNonNull(passwordHash);
    }

    public void changeRole(Role role) {
        this.role = Objects.requireNonNull(role);
    }

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /** Emails are stored in lower case so lookups and the unique constraint ignore case. */
    public static String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // Two entities are equal when they have the same id. A new entity (id still null) is only
    // equal to itself. hashCode is constant so it doesn't change when the id gets assigned.
    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Customer other && id != null && id.equals(other.getId()));
    }

    @Override
    public int hashCode() {
        return Customer.class.hashCode();
    }
}

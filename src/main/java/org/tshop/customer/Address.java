package org.tshop.customer;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

/**
 * A delivery address. It references its customer by id instead of a {@code @ManyToOne}:
 * an address is never loaded without already knowing whose it is.
 */
@Entity
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;

    private String country;

    private String postalCode;

    private String street;

    private String houseNumber;

    private String apartment;

    protected Address() {
    }

    public Address(Long customerId, String country, String postalCode, String street, String houseNumber, String apartment) {
        this.customerId = Objects.requireNonNull(customerId);
        update(country, postalCode, street, houseNumber, apartment);
    }

    public void update(String country, String postalCode, String street, String houseNumber, String apartment) {
        this.country = Objects.requireNonNull(country);
        this.postalCode = Objects.requireNonNull(postalCode);
        this.street = Objects.requireNonNull(street);
        this.houseNumber = houseNumber;
        this.apartment = apartment;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCountry() {
        return country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getStreet() {
        return street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getApartment() {
        return apartment;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Address other && id != null && id.equals(other.getId()));
    }

    @Override
    public int hashCode() {
        return Address.class.hashCode();
    }
}

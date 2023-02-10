package org.tshop.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "client_adress")
public class ClientAdress {
    @Id
    @Column(name = "adress_id", columnDefinition = "INT UNSIGNED not null")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliend_id", nullable = false)
    private Client cliend;

    @Column(name = "country", nullable = false, length = 45)
    private String country;

    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;

    @Column(name = "street", nullable = false, length = 100)
    private String street;

    @Column(name = "home", length = 50)
    private String home;

    @Column(name = "apartment", length = 50)
    private String apartment;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Client getCliend() {
        return cliend;
    }

    public void setCliend(Client cliend) {
        this.cliend = cliend;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

}
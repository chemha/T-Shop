package org.tshop.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "clientaddress")
public class ClientAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INT UNSIGNED not null")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

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

    @OneToMany(mappedBy = "address")
    private Set<Order> orders = new LinkedHashSet<>();

}
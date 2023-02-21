package org.tshop.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "client")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INT UNSIGNED not null")
    private Long id;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "surname", length = 50)
    private String surname;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Column(name = "password", nullable = false, length = 128)
    private String password;

    @OneToMany(mappedBy = "client")
    private Set<ClientAddress> clientAddresses = new LinkedHashSet<>();

    @OneToMany(mappedBy = "client")
    private Set<Order> orders = new LinkedHashSet<>();


}
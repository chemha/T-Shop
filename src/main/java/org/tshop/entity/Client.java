package org.tshop.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "client")
@Getter
@Setter
public class Client {
    @Id
    @Column(name = "client_id", columnDefinition = "INT UNSIGNED not null")
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
    @OneToMany (mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ClientAddress> addresses = new LinkedHashSet<>();
}
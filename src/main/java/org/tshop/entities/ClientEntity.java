package org.tshop.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "Client")
public class ClientEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INT UNSIGNED not null")
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Size(max = 50)
    @NotNull
    @Column(name = "surname", nullable = false, length = 50)
    private String surname;

    @NotNull
    @Column(name = "birthdate", nullable = false)
    private LocalDate birthdate;

    @Size(max = 254)
    @NotNull
    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Size(max = 128)
    @NotNull
    @Column(name = "password", nullable = false, length = 128)
    private String password;

    @Column(name = "admin")
    private boolean admin;

    @OneToMany(mappedBy = "client")
    private Set<ClientAddressEntity> clientAddressEntities = new LinkedHashSet<>();

    @OneToMany(mappedBy = "client")
    private Set<OrderEntity> orderEntities = new LinkedHashSet<>();

}
package org.tshop.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "clientaddress")
public class ClientAddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INT UNSIGNED not null")
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @Size(max = 45)
    @NotNull
    @Column(name = "country", nullable = false, length = 45)
    private String country;

    @Size(max = 10)
    @NotNull
    @Column(name = "postal_code", nullable = false, length = 10)
    private String postalCode;

    @Size(max = 100)
    @NotNull
    @Column(name = "street", nullable = false, length = 100)
    private String street;

    @Size(max = 50)
    @Column(name = "home", length = 50)
    private String home;

    @Size(max = 50)
    @Column(name = "apartment", length = 50)
    private String apartment;

    @OneToMany(mappedBy = "address")
    private Set<OrderEntity> orders = new LinkedHashSet<>();

}
package org.tshop.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "client_adress")
@Getter
@Setter
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
}
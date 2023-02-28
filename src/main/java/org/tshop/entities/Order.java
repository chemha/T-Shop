package org.tshop.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "`order`")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INT UNSIGNED not null")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    private ClientAddress address;

    @Column(name = "payment_method", nullable = false, length = 10)
    private String paymentMethod;

    @Column(name = "delivery_method", nullable = false, length = 10)
    private String deliveryMethod;

    @Column(name = "payment_status", nullable = false, length = 10)
    private String paymentStatus;

    @Column(name = "order_status", nullable = false, length = 10)
    private String orderStatus;

    @OneToMany(mappedBy = "order")
    private Set<Good> goods = new LinkedHashSet<>();

}
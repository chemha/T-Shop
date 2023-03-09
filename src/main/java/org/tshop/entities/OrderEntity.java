package org.tshop.entities;

import io.swagger.models.auth.In;
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
@Table(name = "`order`")
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INT UNSIGNED not null")
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "address_id", nullable = false)
    private ClientAddressEntity address;

    @Size(max = 10)
    @NotNull
    @Column(name = "payment_method", nullable = false, length = 10)
    private String paymentMethod;

    @Size(max = 10)
    @NotNull
    @Column(name = "delivery_method", nullable = false, length = 10)
    private String deliveryMethod;

    @Size(max = 10)
    @NotNull
    @Column(name = "payment_status", nullable = false, length = 10)
    private String paymentStatus;

    @Size(max = 10)
    @NotNull
    @Column(name = "order_status", nullable = false, length = 10)
    private String orderStatus;

    @OneToMany(mappedBy = "order")
    private Set<GoodEntity> goods = new LinkedHashSet<>();

}
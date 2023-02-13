package org.tshop.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Entity
@Table(name = "`order`")
@Getter
@Setter
public class Order {
    @Id
    @Column(name = "order_id", columnDefinition = "INT UNSIGNED not null")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliend_id", nullable = false)
    private Client cliend;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "adress_id", nullable = false)
    private ClientAddress adress;

    @Column(name = "payment_method", nullable = false, length = 10)
    private String paymentMethod;

    @Column(name = "delivery_method", nullable = false, length = 10)
    private String deliveryMethod;

    @Column(name = "payment_status", nullable = false, length = 10)
    private String paymentStatus;

    @Column(name = "order_status", nullable = false, length = 10)
    private String orderStatus;
}
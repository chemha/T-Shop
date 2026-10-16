package org.tshop.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * The delivery address copied into the order when it's placed. Later changes to the customer's
 * address book don't affect where an existing order goes.
 */
@Embeddable
public record ShippingAddress(
        @Column(name = "shipping_country") String country,
        @Column(name = "shipping_postal_code") String postalCode,
        @Column(name = "shipping_street") String street,
        @Column(name = "shipping_house_number") String houseNumber,
        @Column(name = "shipping_apartment") String apartment) {
}

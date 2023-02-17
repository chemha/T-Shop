package org.tshop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.tshop.entity.Client;
import org.tshop.entity.ClientAddress;
import org.tshop.repository.AdressRepository;
import org.tshop.repository.ClientRepository;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {
    private final AdressRepository adressRepository;
    @Transactional
    public ClientAddress createAddress(final Client client, final String country, final String postalCode, final String street) {
        final ClientAddress address = new ClientAddress();
        address.setCliend(client);
        address.setCountry(country);
        address.setPostalCode(postalCode);
        address.setStreet(street);

        adressRepository.save(address);

        return address;
    }
}

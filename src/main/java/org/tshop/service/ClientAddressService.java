package org.tshop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tshop.entity.Client;
import org.tshop.entity.ClientAddress;
import org.tshop.repository.AddressRepository;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientAddressService {
    private final AddressRepository addressRepository;
    @Transactional
    public void createAddress(final Client client, final String country, final String postalCode, final String street , final String home, final String apartment) {
        final ClientAddress address = new ClientAddress();
        address.setClient(client);
        address.setCountry(country);
        address.setPostalCode(postalCode);
        address.setStreet(street);
        address.setHome(home);
        address.setApartment(apartment);

        addressRepository.save(address);
    }
}

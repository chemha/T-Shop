package org.tshop.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tshop.entities.ClientEntity;
import org.tshop.entities.ClientAddressEntity;
import org.tshop.repositories.AddressRepository;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientAddressService {
    private final AddressRepository addressRepository;
    @Transactional
    public void createAddress(final ClientEntity client, final String country, final String postalCode, final String street , final String home, final String apartment) {
        final ClientAddressEntity address = new ClientAddressEntity();
        address.setClient(client);
        address.setCountry(country);
        address.setPostalCode(postalCode);
        address.setStreet(street);
        address.setHome(home);
        address.setApartment(apartment);

        addressRepository.save(address);
    }
}

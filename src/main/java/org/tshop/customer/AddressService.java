package org.tshop.customer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tshop.shared.error.NotFoundException;

import java.util.List;

/** Every method takes the owner's id: there is no way to reach an address without it. */
@Service
public class AddressService {

    private final AddressRepository addresses;

    AddressService(AddressRepository addresses) {
        this.addresses = addresses;
    }

    public record AddressData(String country, String postalCode, String street, String houseNumber, String apartment) {
    }

    @Transactional(readOnly = true)
    public List<Address> list(Long customerId) {
        return addresses.findByCustomerIdOrderById(customerId);
    }

    @Transactional(readOnly = true)
    public Address get(Long customerId, Long addressId) {
        return addresses.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new NotFoundException("Address", addressId));
    }

    @Transactional
    public Address create(Long customerId, AddressData data) {
        return addresses.save(new Address(customerId, data.country(), data.postalCode(), data.street(),
                data.houseNumber(), data.apartment()));
    }

    @Transactional
    public Address update(Long customerId, Long addressId, AddressData data) {
        Address address = get(customerId, addressId);
        address.update(data.country(), data.postalCode(), data.street(), data.houseNumber(), data.apartment());
        return address;
    }

    /** Orders keep their own copy of the address, so deleting it doesn't affect them. */
    @Transactional
    public void delete(Long customerId, Long addressId) {
        addresses.delete(get(customerId, addressId));
    }
}

package org.tshop.customer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.tshop.api.AddressesApi;
import org.tshop.api.model.AddressDetailsDto;
import org.tshop.api.model.AddressDto;
import org.tshop.auth.CurrentCustomer;

import java.util.List;

@RestController
class AddressController implements AddressesApi {

    private final AddressService addresses;
    private final CurrentCustomer currentCustomer;
    private final AddressMapper mapper;

    AddressController(AddressService addresses, CurrentCustomer currentCustomer, AddressMapper mapper) {
        this.addresses = addresses;
        this.currentCustomer = currentCustomer;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<List<AddressDto>> listAddresses() {
        return ResponseEntity.ok(addresses.list(currentCustomer.id()).stream().map(mapper::toDto).toList());
    }

    @Override
    public ResponseEntity<AddressDto> createAddress(AddressDetailsDto request) {
        Address address = addresses.create(currentCustomer.id(), mapper.toData(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(address));
    }

    @Override
    public ResponseEntity<AddressDto> updateAddress(Long addressId, AddressDetailsDto request) {
        return ResponseEntity.ok(mapper.toDto(addresses.update(currentCustomer.id(), addressId, mapper.toData(request))));
    }

    @Override
    public ResponseEntity<Void> deleteAddress(Long addressId) {
        addresses.delete(currentCustomer.id(), addressId);
        return ResponseEntity.noContent().build();
    }
}

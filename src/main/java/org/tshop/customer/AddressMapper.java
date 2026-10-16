package org.tshop.customer;

import org.mapstruct.Mapper;
import org.tshop.api.model.AddressDetailsDto;
import org.tshop.api.model.AddressDto;
import org.tshop.customer.AddressService.AddressData;

@Mapper
public interface AddressMapper {

    AddressDto toDto(Address address);

    AddressData toData(AddressDetailsDto dto);
}

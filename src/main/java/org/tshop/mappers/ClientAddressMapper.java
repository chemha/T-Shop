package org.tshop.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.tshop.dto.ClientAddressDto;
import org.tshop.dto.ClientAddressPropertiesDto;
import org.tshop.entities.ClientAddressEntity;

@Mapper(componentModel = "spring")

public interface ClientAddressMapper {
    ClientAddressDto createDto(ClientAddressEntity entity);

    ClientAddressEntity createEntity(ClientAddressPropertiesDto dto);

    void updateEntity(@MappingTarget ClientAddressEntity entity, ClientAddressPropertiesDto dto);
}

package org.tshop.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.tshop.dto.ClientDto;
import org.tshop.dto.ClientPropertiesDto;
import org.tshop.entities.ClientEntity;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    ClientDto createDto(ClientEntity entity);

    ClientEntity createEntity(ClientPropertiesDto dto);

    void updateEntity(@MappingTarget ClientEntity entity, ClientPropertiesDto dto);
}

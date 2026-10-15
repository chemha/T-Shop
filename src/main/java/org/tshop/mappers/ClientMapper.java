package org.tshop.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.BeforeMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.tshop.dto.ClientDto;
import org.tshop.dto.ClientPropertiesDto;
import org.tshop.entities.ClientEntity;

import java.time.LocalDate;

@Mapper(componentModel = "spring")

public interface ClientMapper {
    @Mapping(target = "password", ignore = true)
    ClientDto createDto(ClientEntity entity);

    ClientEntity createEntity(ClientPropertiesDto dto);

    void updateEntity(@MappingTarget ClientEntity entity, ClientPropertiesDto dto);
}

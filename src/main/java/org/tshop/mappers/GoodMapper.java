package org.tshop.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.tshop.dto.GoodDto;
import org.tshop.dto.GoodPropertiesDto;
import org.tshop.entities.GoodEntity;

@Mapper(componentModel = "spring")

public interface GoodMapper {
    GoodDto createDto(GoodEntity entity);

    GoodEntity createEntity(GoodPropertiesDto dto);

    void updateEntity(@MappingTarget GoodEntity entity, GoodPropertiesDto dto);
}

package org.tshop.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.tshop.dto.ProductDto;
import org.tshop.dto.ProductPropertiesDto;
import org.tshop.entities.ProductEntity;

@Mapper(componentModel = "spring")

public interface ProductMapper {
    ProductDto createDto(ProductEntity entity);

    ProductEntity createEntity(ProductPropertiesDto dto);

    void updateEntity(@MappingTarget ProductEntity entity, ProductPropertiesDto dto);

}

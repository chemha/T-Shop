package org.tshop.mappers;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.tshop.dto.CategoryDto;
import org.tshop.dto.CategoryPropertiesDto;
import org.tshop.entities.CategoryEntity;
import org.tshop.repositories.CategoryRepository;

@Mapper(componentModel = "spring")

public interface CategoryMapper {

    CategoryDto createDto(CategoryEntity entity);

    CategoryEntity createEntity(CategoryPropertiesDto dto);

    void updateEntity(@MappingTarget CategoryEntity entity, CategoryPropertiesDto dto);

}

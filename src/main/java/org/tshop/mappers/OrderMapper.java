package org.tshop.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.tshop.dto.OrderDto;
import org.tshop.dto.OrderPropertiesDto;
import org.tshop.entities.OrderEntity;

@Mapper(componentModel = "spring")

public interface OrderMapper {
    OrderDto createDto(OrderEntity entity);

    OrderEntity createEntity(OrderPropertiesDto dto);

    void updateEntity(@MappingTarget OrderEntity entity, OrderPropertiesDto dto);
}

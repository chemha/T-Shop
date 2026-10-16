package org.tshop.order;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tshop.api.model.OrderDto;
import org.tshop.api.model.OrderItemDto;
import org.tshop.shared.mapping.DateTimeMapper;

@Mapper(uses = DateTimeMapper.class)
public interface OrderMapper {

    OrderDto toDto(Order order);

    @Mapping(target = "subtotal", expression = "java(item.subtotal())")
    OrderItemDto toDto(OrderItem item);
}

package org.tshop.customer;

import org.mapstruct.Mapper;
import org.tshop.api.model.CustomerDto;
import org.tshop.shared.mapping.DateTimeMapper;

/** MapStruct writes the implementation at compile time (see target/generated-sources/annotations). */
@Mapper(uses = DateTimeMapper.class)
public interface CustomerMapper {

    CustomerDto toDto(Customer customer);
}

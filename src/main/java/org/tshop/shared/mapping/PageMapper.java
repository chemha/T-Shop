package org.tshop.shared.mapping;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.tshop.api.model.PageMetadataDto;

/** The "page" block of every paginated response: number, size, totalElements, totalPages. */
@Mapper
public interface PageMapper {

    PageMetadataDto toMetadata(Page<?> page);
}

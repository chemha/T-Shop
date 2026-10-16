package org.tshop.catalog;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tshop.api.model.CategoryDto;
import org.tshop.api.model.ProductDto;

@Mapper
public interface CatalogMapper {

    CategoryDto toDto(Category category);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    ProductDto toDto(Product product);
}

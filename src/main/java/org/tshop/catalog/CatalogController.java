package org.tshop.catalog;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.tshop.api.CategoriesApi;
import org.tshop.api.ProductsApi;
import org.tshop.api.model.CategoryDto;
import org.tshop.api.model.CategoryRequestDto;
import org.tshop.api.model.ProductDto;
import org.tshop.api.model.ProductPageDto;
import org.tshop.api.model.ProductRequestDto;
import org.tshop.catalog.ProductService.ProductData;
import org.tshop.catalog.ProductService.Search;
import org.tshop.shared.mapping.PageMapper;

import java.math.BigDecimal;
import java.util.List;

/** Who may call what is decided in SecurityConfig: reads are public, changes need ADMIN. */
@RestController
class CatalogController implements CategoriesApi, ProductsApi {

    private final CategoryService categories;
    private final ProductService products;
    private final CatalogMapper mapper;
    private final PageMapper pageMapper;

    CatalogController(CategoryService categories, ProductService products, CatalogMapper mapper, PageMapper pageMapper) {
        this.categories = categories;
        this.products = products;
        this.mapper = mapper;
        this.pageMapper = pageMapper;
    }

    // ---------------------------------------------------------------- Categories

    @Override
    public ResponseEntity<List<CategoryDto>> listCategories() {
        return ResponseEntity.ok(categories.list().stream().map(mapper::toDto).toList());
    }

    @Override
    public ResponseEntity<CategoryDto> createCategory(CategoryRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(categories.create(request.getName())));
    }

    @Override
    public ResponseEntity<CategoryDto> updateCategory(Long categoryId, CategoryRequestDto request) {
        return ResponseEntity.ok(mapper.toDto(categories.rename(categoryId, request.getName())));
    }

    @Override
    public ResponseEntity<Void> deleteCategory(Long categoryId) {
        categories.delete(categoryId);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------------- Products

    @Override
    public ResponseEntity<ProductPageDto> searchProducts(@Nullable String q, @Nullable Long categoryId,
                                                         @Nullable BigDecimal minPrice, @Nullable BigDecimal maxPrice,
                                                         Integer page, Integer size, @Nullable String sort) {
        Page<Product> result = products.search(new Search(q, categoryId, minPrice, maxPrice), page, size, ProductSort.fromValue(sort));
        return ResponseEntity.ok(new ProductPageDto(result.map(mapper::toDto).getContent(), pageMapper.toMetadata(result)));
    }

    @Override
    public ResponseEntity<ProductDto> getProduct(Long productId) {
        return ResponseEntity.ok(mapper.toDto(products.get(productId)));
    }

    @Override
    public ResponseEntity<ProductDto> createProduct(ProductRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(products.create(toData(request))));
    }

    @Override
    public ResponseEntity<ProductDto> updateProduct(Long productId, ProductRequestDto request) {
        return ResponseEntity.ok(mapper.toDto(products.update(productId, toData(request))));
    }

    @Override
    public ResponseEntity<Void> deleteProduct(Long productId) {
        products.delete(productId);
        return ResponseEntity.noContent().build();
    }

    private static ProductData toData(ProductRequestDto request) {
        return new ProductData(request.getCategoryId(), request.getName(), request.getPrice(), request.getBrand(),
                request.getColor(), request.getWeightKg(), request.getVolumeL(), request.getStock());
    }
}

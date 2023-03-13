package org.tshop.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tshop.api.ProductsApi;
import org.tshop.dto.ProductDto;
import org.tshop.dto.ProductPropertiesDto;
import org.tshop.dto.ProductSearchResultDto;
import org.tshop.services.ProductService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/client-addresses")
public class ProductController implements ProductsApi {
    private final ProductService service;

    @Override
    public ResponseEntity<Void> createProduct(ProductPropertiesDto properties) {
        service.create(properties);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Void> deleteProduct(Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ProductDto> getProduct(Integer id) {
        ProductDto dto = service.get(id);
        return ResponseEntity.ok(dto);
    }

    @Override
    public ResponseEntity<ProductSearchResultDto> getProductSearchResult(Integer pageNumber, Integer pageSize) {
        ProductSearchResultDto dto = service.getSearchResult(pageNumber, pageSize);
        return ResponseEntity.ok().body(dto);
    }

    @Override
    public ResponseEntity<ProductDto> updateProduct(Integer id, ProductPropertiesDto properties) {
        final ProductDto dto = service.update(id, properties);
        return ResponseEntity.ok().body(dto);
    }
}

package org.tshop.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.tshop.api.ProductsApi;
import org.tshop.dto.ClientDto;
import org.tshop.dto.ProductDto;
import org.tshop.dto.ProductPropertiesDto;
import org.tshop.dto.ProductSearchResultDto;
import org.tshop.services.CategoryService;
import org.tshop.services.ProductService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController implements ProductsApi {
    private final ProductService productService;
    private final CategoryService categoryService;
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("product", productService.loadEntity(id));
        model.addAttribute("categories", categoryService.findAll());
        return "products/edit";
    }

    @GetMapping("/create")
    public String edit(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "products/create";
    }

    @Override
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductPropertiesDto properties) {
        ProductDto dto = productService.create(properties);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ProductDto> getProduct(Integer id) {
        ProductDto dto = productService.get(id);
        return ResponseEntity.ok(dto);
    }

    @Override
    @GetMapping
    public ResponseEntity<ProductSearchResultDto> getProductSearchResult(Integer pageNumber, Integer pageSize) {
        ProductSearchResultDto dto = productService.getSearchResult(pageNumber, pageSize);
        return ResponseEntity.ok().body(dto);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Integer id, ProductPropertiesDto properties) {
        final ProductDto dto = productService.update(id, properties);
        return ResponseEntity.ok().body(dto);
    }

}

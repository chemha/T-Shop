package org.tshop.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.tshop.api.CategoriesApi;
import org.tshop.dto.CategoryDto;
import org.tshop.dto.CategoryPropertiesDto;
import org.tshop.dto.CategorySearchResultDto;
import org.tshop.services.CategoryService;
import org.tshop.services.CategoryService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController implements CategoriesApi {
    private final CategoryService service;
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("category", service.loadEntity(id));
        return "categories/edit";
    }

    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("categories", service.findAll());
        return "categories/index";
    }

    @GetMapping("/create")
    public String edit(Model model) {
        return "categories/create";
    }

    @Override
    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryPropertiesDto properties) {
        CategoryDto dto = service.create(properties);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CategoryDto> getCategory(Integer id) {
        CategoryDto dto = service.get(id);
        return ResponseEntity.ok(dto);
    }

    @Override
    @GetMapping
    public ResponseEntity<CategorySearchResultDto> getCategorySearchResult(Integer pageNumber, Integer pageSize) {
        CategorySearchResultDto dto = service.getSearchResult(pageNumber, pageSize);
        return ResponseEntity.ok().body(dto);
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Integer id, CategoryPropertiesDto properties) {
        final CategoryDto dto = service.update(id, properties);
        return ResponseEntity.ok().body(dto);
    }

}

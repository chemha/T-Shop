package org.tshop.catalog;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tshop.shared.error.ConflictException;
import org.tshop.shared.error.NotFoundException;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categories;
    private final ProductRepository products;

    CategoryService(CategoryRepository categories, ProductRepository products) {
        this.categories = categories;
        this.products = products;
    }

    @Transactional(readOnly = true)
    public List<Category> list() {
        return categories.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Category get(Long id) {
        return categories.findById(id).orElseThrow(() -> new NotFoundException("Category", id));
    }

    @Transactional
    public Category create(String name) {
        if (categories.existsByNameIgnoreCase(name.strip())) {
            throw new ConflictException("A category named '%s' already exists".formatted(name.strip()));
        }
        return categories.save(new Category(name));
    }

    @Transactional
    public Category rename(Long id, String name) {
        Category category = get(id);
        if (categories.existsByNameIgnoreCaseAndIdNot(name.strip(), id)) {
            throw new ConflictException("A category named '%s' already exists".formatted(name.strip()));
        }
        category.rename(name);
        return category;
    }

    @Transactional
    public void delete(Long id) {
        Category category = get(id);
        if (products.existsByCategoryId(id)) {
            throw new ConflictException("The category '%s' still has products".formatted(category.getName()));
        }
        categories.delete(category);
    }
}

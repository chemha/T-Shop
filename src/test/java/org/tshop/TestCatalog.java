package org.tshop;

import org.springframework.boot.test.context.TestComponent;
import org.tshop.catalog.Category;
import org.tshop.catalog.CategoryRepository;
import org.tshop.catalog.Product;
import org.tshop.catalog.ProductRepository;

import java.math.BigDecimal;
import java.util.UUID;

/** Creates categories and products straight in the database, with unique names. */
@TestComponent
public class TestCatalog {

    private final CategoryRepository categories;
    private final ProductRepository products;

    TestCatalog(CategoryRepository categories, ProductRepository products) {
        this.categories = categories;
        this.products = products;
    }

    public Category category() {
        return categories.save(new Category("Category " + UUID.randomUUID()));
    }

    public Product product(String price, int stock) {
        return product(category(), price, stock);
    }

    public Product product(Category category, String price, int stock) {
        return products.save(new Product(category, "Product " + UUID.randomUUID(), new BigDecimal(price), stock));
    }

    public int stockOf(Long productId) {
        return products.findById(productId).orElseThrow().getStock();
    }
}

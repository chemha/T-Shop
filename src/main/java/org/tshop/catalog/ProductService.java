package org.tshop.catalog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tshop.shared.error.InvalidFieldException;
import org.tshop.shared.error.NotFoundException;

import java.math.BigDecimal;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository products;
    private final CategoryService categories;

    ProductService(ProductRepository products, CategoryService categories) {
        this.products = products;
        this.categories = categories;
    }

    public record Search(String text, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
    }

    public record ProductData(Long categoryId, String name, BigDecimal price, String brand, String color,
                              BigDecimal weightKg, BigDecimal volumeL, int stock) {
    }

    @Transactional(readOnly = true)
    public Page<Product> search(Search search, int page, int size, ProductSort sort) {
        if (search.minPrice() != null && search.maxPrice() != null && search.minPrice().compareTo(search.maxPrice()) > 0) {
            throw new InvalidFieldException("minPrice", "must not be greater than maxPrice");
        }
        return products.findAll(
                ProductSpecifications.matching(search.text(), search.categoryId(), search.minPrice(), search.maxPrice()),
                PageRequest.of(page, size, sort.toSort()));
    }

    @Transactional(readOnly = true)
    public Product get(Long id) {
        return products.findWithCategoryById(id).orElseThrow(() -> new NotFoundException("Product", id));
    }

    @Transactional
    public Product create(ProductData data) {
        Product product = new Product(categories.get(data.categoryId()), data.name(), data.price(), data.stock());
        product.describe(data.brand(), data.color(), data.weightKg(), data.volumeL());
        product = products.save(product);
        log.info("Product {} created", product.getId());
        return product;
    }

    @Transactional
    public Product update(Long id, ProductData data) {
        Product product = get(id);
        product.update(categories.get(data.categoryId()), data.name(), data.price(), data.brand(), data.color(),
                data.weightKg(), data.volumeL(), data.stock());
        return product;
    }

    /** Cart lines go with the product (ON DELETE CASCADE); order lines keep its name and price. */
    @Transactional
    public void delete(Long id) {
        products.delete(get(id));
        log.info("Product {} deleted", id);
    }
}

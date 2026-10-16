package org.tshop.catalog;

import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Builds the WHERE clause of a catalogue search from optional filters.
 * Each filter is only added when it has a value.
 */
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> matching(String text, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        Specification<Product> spec = Specification.unrestricted();
        if (text != null && !text.isBlank()) {
            spec = spec.and(nameOrBrandContains(text));
        }
        if (categoryId != null) {
            spec = spec.and(inCategory(categoryId));
        }
        if (minPrice != null) {
            spec = spec.and((product, query, cb) -> cb.greaterThanOrEqualTo(product.get("price"), minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and((product, query, cb) -> cb.lessThanOrEqualTo(product.get("price"), maxPrice));
        }
        return spec;
    }

    static Specification<Product> nameOrBrandContains(String text) {
        // LIKE wildcards typed by the user are escaped so "50%" searches for the literal text
        String pattern = "%" + text.strip().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        return (product, query, cb) -> cb.or(
                cb.like(cb.lower(product.get("name")), pattern, '\\'),
                cb.like(cb.lower(product.get("brand")), pattern, '\\'));
    }

    static Specification<Product> inCategory(Long categoryId) {
        return (product, query, cb) -> cb.equal(product.get("category").get("id"), categoryId);
    }
}

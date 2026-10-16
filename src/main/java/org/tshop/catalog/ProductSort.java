package org.tshop.catalog;

import org.springframework.data.domain.Sort;
import org.tshop.shared.error.InvalidFieldException;

import java.util.Arrays;

/** The sort orders accepted by the catalogue search, as named in the API (sort=price_asc). */
public enum ProductSort {

    NAME("name", Sort.by("name")),
    PRICE_ASC("price_asc", Sort.by(Sort.Direction.ASC, "price")),
    PRICE_DESC("price_desc", Sort.by(Sort.Direction.DESC, "price")),
    NEWEST("newest", Sort.by(Sort.Direction.DESC, "createdAt"));

    private final String value;
    private final Sort sort;

    ProductSort(String value, Sort sort) {
        this.value = value;
        // The id breaks ties (same name, same price...). Without a unique last criterion, the
        // database may return equal rows in a different order on each query, and a product could
        // show up on two pages or on none
        this.sort = sort.and(Sort.by("id"));
    }

    public Sort toSort() {
        return sort;
    }

    public static ProductSort fromValue(String value) {
        if (value == null) {
            return NAME;
        }
        return Arrays.stream(values())
                .filter(option -> option.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new InvalidFieldException("sort", "must be one of name, price_asc, price_desc, newest"));
    }
}

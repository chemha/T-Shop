package org.tshop.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.tshop.ApiClient;
import org.tshop.ApiTest;
import org.tshop.TestAccounts;
import org.tshop.TestAccounts.Account;
import org.tshop.TestCatalog;

import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
class CatalogApiTests {

    @Autowired
    ApiClient api;

    @Autowired
    TestAccounts accounts;

    @Autowired
    TestCatalog catalog;

    // ---------------------------------------------------------------- Public reads

    @Test
    void searchIsPaginatedAndSorted() {
        Category category = catalog.category();
        catalog.product(category, "30.00", 1);
        catalog.product(category, "10.00", 1);
        catalog.product(category, "20.00", 1);
        String uri = "/api/v1/products?categoryId={id}&sort=price_desc&size=2&page={page}";

        MvcTestResult firstPage = api.get(null, uri, category.getId(), 0);

        assertThat(firstPage).hasStatusOk().bodyJson().extractingPath("$.content[*].price").asArray().containsExactly(30.0, 20.0);
        assertThat(firstPage).bodyJson().extractingPath("$.page.totalElements").isEqualTo(3);
        assertThat(firstPage).bodyJson().extractingPath("$.page.totalPages").isEqualTo(2);
        assertThat(api.get(null, uri, category.getId(), 1))
                .bodyJson().extractingPath("$.content[*].price").asArray().containsExactly(10.0);
    }

    @Test
    void productsIncludeTheirCategory() {
        Product product = catalog.product("9.99", 5);

        assertThat(api.get(null, "/api/v1/products/{id}", product.getId()))
                .hasStatusOk()
                .bodyJson().extractingPath("$.categoryName").isEqualTo(product.getCategory().getName());
    }

    @Test
    void invalidSearchParametersAreRejected() {
        assertThat(api.get(null, "/api/v1/products?sort=random")).hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.errors[0].field").isEqualTo("sort");
        assertThat(api.get(null, "/api/v1/products?minPrice=50&maxPrice=10")).hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.errors[0].field").isEqualTo("minPrice");
        // Limits declared in openapi.yaml (maximum: 100) are enforced by the generated interface
        assertThat(api.get(null, "/api/v1/products?size=1000")).hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.errors[0].field").isEqualTo("size");
    }

    @Test
    void unknownProductIsNotFound() {
        assertThat(api.get(null, "/api/v1/products/999999"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson().extractingPath("$.detail").isEqualTo("Product 999999 not found");
    }

    // ---------------------------------------------------------------- Admin changes

    @Test
    void adminManagesProducts() {
        Account admin = accounts.admin();
        Long categoryId = catalog.category().getId();

        MvcTestResult created = api.post(admin, productJson(categoryId, "19.90", 4), "/api/v1/products");
        assertThat(created).hasStatus(HttpStatus.CREATED).bodyJson().extractingPath("$.stock").isEqualTo(4);
        long id = ApiClient.id(created);

        assertThat(api.put(admin, productJson(categoryId, "17.50", 9), "/api/v1/products/{id}", id))
                .hasStatusOk().bodyJson().extractingPath("$.price").isEqualTo(17.5);
        assertThat(api.delete(admin, "/api/v1/products/{id}", id)).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(api.get(null, "/api/v1/products/{id}", id)).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void productDataIsValidated() {
        Account admin = accounts.admin();

        assertThat(api.post(admin, productJson(catalog.category().getId(), "-1", -5), "/api/v1/products"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.errors[*].field").asArray().containsExactlyInAnyOrder("price", "stock");
        assertThat(api.post(admin, productJson(999999L, "1", 1), "/api/v1/products"))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void categoryNamesAreUnique() {
        Category existing = catalog.category();

        assertThat(api.post(accounts.admin(), """
                {"name": "%s"}""".formatted(existing.getName().toUpperCase()), "/api/v1/categories"))
                .hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void categoriesWithProductsCannotBeDeleted() {
        Account admin = accounts.admin();
        Category category = catalog.category();
        catalog.product(category, "1.00", 1);
        Category empty = catalog.category();

        assertThat(api.delete(admin, "/api/v1/categories/{id}", category.getId())).hasStatus(HttpStatus.CONFLICT);
        assertThat(api.delete(admin, "/api/v1/categories/{id}", empty.getId())).hasStatus(HttpStatus.NO_CONTENT);
    }

    private static String productJson(Long categoryId, String price, int stock) {
        return """
                {"categoryId": %d, "name": "Desk lamp", "price": %s, "brand": "Lumo", "stock": %d}"""
                .formatted(categoryId, price, stock);
    }
}

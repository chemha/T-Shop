package org.tshop.catalog;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.tshop.PersistenceTest;
import org.tshop.SqlStatementCounter;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.tshop.catalog.ProductSpecifications.matching;

@PersistenceTest
class ProductRepositoryTests {

    static final PageRequest FIRST_PAGE = PageRequest.of(0, 20, Sort.by("name"));

    @Autowired
    ProductRepository products;

    @Autowired
    TestEntityManager em;

    @Autowired
    EntityManagerFactory emf;

    Category phones;
    Category cables;

    @BeforeEach
    void createCatalogue() {
        phones = em.persist(new Category("Test phones"));
        cables = em.persist(new Category("Test cables"));
        product(phones, "Zeta Phone X", "Zeta", "799.00");
        product(phones, "Zeta Phone Mini", "Zeta", "499.00");
        product(phones, "Omega 5G", "Omega", "299.00");
        product(cables, "USB-C cable 100% copper", "Omega", "9.99");
        product(cables, "USB-C cable 1m", "Cablex", "4.99");
        em.flush();
        em.clear();
    }

    @Test
    void filtersByCategory() {
        Page<Product> page = products.findAll(matching(null, cables.getId(), null, null), FIRST_PAGE);

        assertThat(page.getContent()).extracting(Product::getName)
                .containsExactly("USB-C cable 100% copper", "USB-C cable 1m");
    }

    @Test
    void searchesNameAndBrandIgnoringCase() {
        Page<Product> page = products.findAll(matching("omega", null, null, null), FIRST_PAGE);

        assertThat(page.getContent()).extracting(Product::getName)
                .containsExactly("Omega 5G", "USB-C cable 100% copper");
    }

    @Test
    void wildcardsTypedByTheUserAreLiteral() {
        // Unescaped, "%" would be the pattern "%%%" and match every product
        Page<Product> page = products.findAll(matching("%", null, null, null), FIRST_PAGE);

        assertThat(page.getContent()).extracting(Product::getName)
                .containsExactly("USB-C cable 100% copper");
    }

    @Test
    void combinesFilters() {
        Page<Product> page = products.findAll(
                matching("zeta", phones.getId(), new BigDecimal("400"), new BigDecimal("500")), FIRST_PAGE);

        assertThat(page.getContent()).extracting(Product::getName).containsExactly("Zeta Phone Mini");
    }

    @Test
    void loadsThePageAndItsCategoriesInOneQuery() {
        SqlStatementCounter sql = new SqlStatementCounter(emf);
        sql.reset();

        Page<Product> page = products.findAll(matching(null, phones.getId(), null, null), FIRST_PAGE);
        page.forEach(product -> product.getCategory().getName()); // would trigger lazy loading

        // Without the entity graph: 1 query for the products + 1 per product for its category
        assertThat(page.getContent()).hasSize(3);
        assertThat(sql.count()).isEqualTo(1);
    }

    @Test
    void knowsWhetherACategoryHasProducts() {
        Category empty = em.persistAndFlush(new Category("Test empty"));

        assertThat(products.existsByCategoryId(phones.getId())).isTrue();
        assertThat(products.existsByCategoryId(empty.getId())).isFalse();
    }

    private void product(Category category, String name, String brand, String price) {
        Product product = new Product(category, name, new BigDecimal(price), 10);
        product.describe(brand, null, null, null);
        em.persist(product);
    }
}

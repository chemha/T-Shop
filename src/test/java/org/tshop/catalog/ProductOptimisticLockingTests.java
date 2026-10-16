package org.tshop.catalog;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.tshop.PersistenceTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Simulates two customers buying the last unit at the same time. Both read the product while it
 * still has 1 unit; whoever commits second must fail instead of overwriting the first purchase.
 */
@PersistenceTest
@Transactional(propagation = Propagation.NOT_SUPPORTED) // each step below commits for real
class ProductOptimisticLockingTests {

    @Autowired
    ProductRepository products;

    @Autowired
    CategoryRepository categories;

    @Autowired
    TransactionTemplate tx;

    Long productId;
    Long categoryId;

    @Test
    void secondConcurrentUpdateFails() {
        tx.executeWithoutResult(status -> {
            Category category = categories.save(new Category("Test locking"));
            categoryId = category.getId();
            productId = products.save(new Product(category, "Last unit", new BigDecimal("10.00"), 1)).getId();
        });

        // Both "customers" load the product: same row, same version
        Product seenByAna = tx.execute(status -> products.findById(productId).orElseThrow());
        Product seenByBob = tx.execute(status -> products.findById(productId).orElseThrow());

        // Ana buys it: UPDATE ... SET stock = 0, version = 1 WHERE id = ? AND version = 0
        seenByAna.reserveStock(1);
        tx.executeWithoutResult(status -> products.save(seenByAna));

        // Bob still has version 0, so his UPDATE matches no row
        seenByBob.reserveStock(1);
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> products.save(seenByBob)))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        assertThat(products.findById(productId)).hasValueSatisfying(p -> {
            assertThat(p.getStock()).isZero();
            assertThat(p.getVersion()).isEqualTo(1);
        });
    }

    @AfterEach
    void cleanUp() {
        // Nothing is rolled back automatically in this test, so remove what it created
        tx.executeWithoutResult(status -> {
            if (productId != null) products.deleteById(productId);
            if (categoryId != null) categories.deleteById(categoryId);
        });
    }
}

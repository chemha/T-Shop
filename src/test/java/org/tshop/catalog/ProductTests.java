package org.tshop.catalog;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTests {

    Product product = new Product(new Category("Laptops"), "Dell XPS 13", new BigDecimal("1099.99"), 3);

    @Test
    void reservingStockTakesUnitsOut() {
        product.reserveStock(2);

        assertThat(product.getStock()).isEqualTo(1);
    }

    @Test
    void cannotReserveMoreThanTheStock() {
        assertThatThrownBy(() -> product.reserveStock(4))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessage("Only 3 unit(s) of 'Dell XPS 13' left, 4 requested");
        assertThat(product.getStock()).isEqualTo(3);
    }

    @Test
    void releasingStockPutsUnitsBack() {
        product.releaseStock(2);

        assertThat(product.getStock()).isEqualTo(5);
    }

    @Test
    void quantitiesMustBePositive() {
        assertThatIllegalArgumentException().isThrownBy(() -> product.reserveStock(0));
        assertThatIllegalArgumentException().isThrownBy(() -> product.releaseStock(-1));
    }

    @Test
    void priceAndStockCannotBeNegative() {
        assertThatIllegalArgumentException().isThrownBy(() -> product.changePrice(new BigDecimal("-0.01")));
        assertThatIllegalArgumentException().isThrownBy(() -> product.changeStock(-1));
    }
}

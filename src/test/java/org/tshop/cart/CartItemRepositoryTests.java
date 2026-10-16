package org.tshop.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.tshop.PersistenceTest;
import org.tshop.catalog.Category;
import org.tshop.catalog.Product;
import org.tshop.customer.Customer;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@PersistenceTest
class CartItemRepositoryTests {

    @Autowired
    CartItemRepository cart;

    @Autowired
    TestEntityManager em;

    Long ana;
    Long bob;
    Long mouse;
    Long keyboard;

    @BeforeEach
    void setUp() {
        ana = em.persist(new Customer("Ana", "García", LocalDate.of(1990, 1, 1), "ana@example.com", "x")).getId();
        bob = em.persist(new Customer("Bob", "Smith", LocalDate.of(1990, 1, 1), "bob@example.com", "x")).getId();
        Category category = em.persist(new Category("Test accessories"));
        mouse = em.persist(new Product(category, "Mouse", new BigDecimal("19.99"), 5)).getId();
        keyboard = em.persist(new Product(category, "Keyboard", new BigDecimal("49.99"), 5)).getId();
    }

    @Test
    void eachCustomerHasTheirOwnCart() {
        cart.save(new CartItem(ana, mouse, 2));
        cart.save(new CartItem(ana, keyboard, 1));
        cart.save(new CartItem(bob, mouse, 1));
        em.flush();

        assertThat(cart.findByIdCustomerIdOrderByAddedAtAsc(ana))
                .extracting(CartItem::getProductId, CartItem::getQuantity)
                .containsExactlyInAnyOrder(tuple(mouse, 2), tuple(keyboard, 1));
    }

    @Test
    void theSameProductIsOneLineWhoseQuantityChanges() {
        cart.save(new CartItem(ana, mouse, 1));
        em.flush();

        CartItem line = cart.findById(new CartItemId(ana, mouse)).orElseThrow();
        line.changeQuantity(3);
        em.flush();
        em.clear();

        assertThat(cart.findByIdCustomerIdOrderByAddedAtAsc(ana))
                .singleElement()
                .extracting(CartItem::getQuantity).isEqualTo(3);
    }

    @Test
    void clearingACartLeavesTheOthersAlone() {
        cart.save(new CartItem(ana, mouse, 2));
        cart.save(new CartItem(ana, keyboard, 1));
        cart.save(new CartItem(bob, mouse, 1));
        em.flush();

        int deleted = cart.deleteAllByCustomerId(ana);

        assertThat(deleted).isEqualTo(2);
        assertThat(cart.findByIdCustomerIdOrderByAddedAtAsc(ana)).isEmpty();
        assertThat(cart.findByIdCustomerIdOrderByAddedAtAsc(bob)).hasSize(1);
    }
}

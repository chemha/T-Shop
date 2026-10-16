package org.tshop.order;

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
import org.tshop.customer.Customer;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@PersistenceTest
class OrderRepositoryTests {

    static final ShippingAddress ADDRESS = new ShippingAddress("Spain", "28001", "Gran Via", "1", "2B");
    static final PageRequest NEWEST_FIRST = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt", "id"));

    @Autowired
    OrderRepository orders;

    @Autowired
    TestEntityManager em;

    @Autowired
    EntityManagerFactory emf;

    Long ana;
    Long bob;

    @BeforeEach
    void setUp() {
        ana = em.persist(new Customer("Ana", "García", LocalDate.of(1990, 1, 1), "ana@example.com", "x")).getId();
        bob = em.persist(new Customer("Bob", "Smith", LocalDate.of(1990, 1, 1), "bob@example.com", "x")).getId();
    }

    @Test
    void savesTheOrderWithItsLinesAndSnapshot() {
        Order order = new Order(ana, PaymentMethod.CARD, DeliveryMethod.PREMIUM, ADDRESS);
        order.addItem(1L, "Mouse", new BigDecimal("19.99"), 2);
        order.addItem(2L, "Keyboard", new BigDecimal("49.99"), 1);
        Long id = orders.save(order).getId();
        em.flush();
        em.clear(); // forget everything, so the next read really comes from the database

        Order saved = orders.findById(id).orElseThrow();

        assertThat(saved.getTotal()).isEqualByComparingTo("89.97");
        assertThat(saved.getShippingAddress()).isEqualTo(ADDRESS);
        assertThat(saved.getItems()).extracting(OrderItem::getProductName).containsExactly("Mouse", "Keyboard");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void customersOnlySeeTheirOwnOrders() {
        Long anasOrder = orders.save(orderWithItems(ana, 1)).getId();
        orders.save(orderWithItems(bob, 1));
        em.flush();

        assertThat(orders.findByIdAndCustomerId(anasOrder, ana)).isPresent();
        assertThat(orders.findByIdAndCustomerId(anasOrder, bob)).isEmpty();
        assertThat(orders.findByCustomerId(ana, NEWEST_FIRST).getContent()).hasSize(1);
        assertThat(orders.existsByCustomerId(bob)).isTrue();
    }

    @Test
    void loadsAPageOfOrdersWithTheirLinesInTwoQueries() {
        for (int i = 0; i < 5; i++) {
            orders.save(orderWithItems(ana, 3));
        }
        em.flush();
        em.clear();
        SqlStatementCounter sql = new SqlStatementCounter(emf);
        sql.reset();

        Page<Order> page = orders.findByCustomerId(ana, NEWEST_FIRST);
        page.forEach(order -> order.getItems().size()); // touches every lazy collection

        // 1 query for the orders + 1 query (WHERE order_id IN (...)) for all the lines.
        // Without batch fetching it would be 1 + 5.
        assertThat(page.getContent()).hasSize(5);
        assertThat(sql.count()).isEqualTo(2);
    }

    private static Order orderWithItems(Long customerId, int lines) {
        Order order = new Order(customerId, PaymentMethod.CASH, DeliveryMethod.STANDARD, ADDRESS);
        for (int i = 1; i <= lines; i++) {
            order.addItem((long) i, "Product " + i, new BigDecimal("10.00"), 1);
        }
        return order;
    }
}

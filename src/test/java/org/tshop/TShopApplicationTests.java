package org.tshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TShopApplicationTests {

    @Autowired
    JdbcClient jdbc;

    @Test
    void migrationsCreateTheSchemaAndLoadTheDemoData() {
        // The database is shared with the other tests, which add their own rows, so this checks
        // for specific demo rows instead of counting
        String adminRole = jdbc.sql("SELECT role FROM customer WHERE email = 'admin@admin.admin'")
                .query(String.class).single();
        String laptopCategory = jdbc.sql("SELECT c.name FROM product p JOIN category c ON c.id = p.category_id WHERE p.name = 'Dell XPS 13'")
                .query(String.class).single();

        assertThat(adminRole).isEqualTo("ADMIN");
        assertThat(laptopCategory).isEqualTo("Laptops");
    }
}

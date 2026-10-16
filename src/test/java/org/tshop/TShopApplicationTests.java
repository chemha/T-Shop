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
        long products = jdbc.sql("SELECT count(*) FROM product").query(Long.class).single();
        String adminRole = jdbc.sql("SELECT role FROM customer WHERE email = 'admin@admin.admin'")
                .query(String.class).single();

        assertThat(products).isEqualTo(13);
        assertThat(adminRole).isEqualTo("ADMIN");
    }
}

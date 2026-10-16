package org.tshop;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A JPA slice test: only entities, repositories, Flyway and the datasource are started (no web
 * layer), against the shared PostgreSQL container. Each test runs in a transaction that is rolled
 * back at the end. Hibernate statistics are on so tests can count the SQL statements executed.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Import(TestcontainersConfiguration.class)
public @interface PersistenceTest {
}

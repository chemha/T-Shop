package org.tshop;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Full application test through the HTTP layer (MockMvc), security included, against the shared
 * PostgreSQL container.
 * <p>
 * Deliberately not {@code @Transactional}: a test transaction would stay open during the request
 * and hide bugs such as lazy loading outside a service. Tests use unique emails instead
 * (see {@link TestAccounts}) so their data doesn't collide.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, TestAccounts.class})
public @interface ApiTest {
}

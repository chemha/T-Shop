package org.tshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@ApiTest
class ApiDocumentationTests {

    @Autowired
    MockMvcTester mvc;

    @Test
    void contractIsPublished() {
        assertThat(mvc.get().uri("/openapi.yaml"))
                .hasStatusOk()
                .bodyText().contains("title: T-Shop API");
    }

    @Test
    void swaggerUiShowsTheContract() {
        assertThat(mvc.get().uri("/v3/api-docs/swagger-config"))
                .hasStatusOk()
                .bodyJson().extractingPath("$.url").isEqualTo("/openapi.yaml");
    }
}

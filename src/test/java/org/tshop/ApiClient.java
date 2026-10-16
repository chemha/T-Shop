package org.tshop;

import com.jayway.jsonpath.JsonPath;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MockMvcTester.MockMvcRequestBuilder;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.tshop.TestAccounts.Account;

import java.io.UnsupportedEncodingException;

/** Shortcuts for calling the API as a given account (or anonymously, with null). */
@TestComponent
public class ApiClient {

    private final MockMvcTester mvc;

    ApiClient(MockMvcTester mvc) {
        this.mvc = mvc;
    }

    public MvcTestResult get(@Nullable Account account, String uri, Object... uriVariables) {
        return send(HttpMethod.GET, account, null, uri, uriVariables);
    }

    public MvcTestResult post(@Nullable Account account, @Nullable String json, String uri, Object... uriVariables) {
        return send(HttpMethod.POST, account, json, uri, uriVariables);
    }

    public MvcTestResult put(@Nullable Account account, @Nullable String json, String uri, Object... uriVariables) {
        return send(HttpMethod.PUT, account, json, uri, uriVariables);
    }

    public MvcTestResult delete(@Nullable Account account, String uri, Object... uriVariables) {
        return send(HttpMethod.DELETE, account, null, uri, uriVariables);
    }

    /** Reads a value from the JSON response, e.g. {@code read(result, "$.id")}. */
    public static <T> T read(MvcTestResult result, String jsonPath) {
        try {
            return JsonPath.read(result.getResponse().getContentAsString(), jsonPath);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static long id(MvcTestResult result) {
        return ApiClient.<Number>read(result, "$.id").longValue();
    }

    private MvcTestResult send(HttpMethod method, @Nullable Account account, @Nullable String json,
                               String uri, Object... uriVariables) {
        MockMvcRequestBuilder request = mvc.method(method).uri(uri, uriVariables);
        if (account != null) {
            request.header(HttpHeaders.AUTHORIZATION, account.bearer());
        }
        if (json != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(json);
        }
        return request.exchange();
    }
}

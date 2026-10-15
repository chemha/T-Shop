import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.tshop.entities.ClientEntity;
import org.tshop.repositories.ClientRepository;
import org.tshop.services.security.LoginSuccessHandler;

import javax.servlet.http.HttpSession;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoginSuccessHandlerTest {
    private final ClientRepository repository = mock(ClientRepository.class);
    private final LoginSuccessHandler handler = new LoginSuccessHandler(repository);

    @Test
    void onAuthenticationSuccess_shouldStoreClientInSessionAndRedirectHome() throws Exception {
        ClientEntity client = new ClientEntity();
        client.setId(1);
        client.setName("admin");
        client.setEmail("admin@admin.admin");
        client.setAdmin(true);
        when(repository.findByEmail("admin@admin.admin")).thenReturn(Optional.of(client));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/clients/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(request, response, new UsernamePasswordAuthenticationToken("admin@admin.admin", null));

        HttpSession session = request.getSession();
        assertEquals(1, session.getAttribute("userId"));
        assertEquals(true, session.getAttribute("admin"));
        assertEquals("admin", session.getAttribute("userName"));
        assertEquals("/", response.getRedirectedUrl());
    }
}

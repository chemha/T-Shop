import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.ui.Model;
import org.tshop.controllers.ClientController;
import org.tshop.repositories.ClientRepository;
import org.tshop.services.ClientService;

import javax.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ClientControllerTest {
    private final ClientController controller = new ClientController(mock(ClientService.class), mock(ClientRepository.class));
    private final Model model = mock(Model.class);
    private final HttpSession session = mock(HttpSession.class);

    @Test
    void login_shouldReturnLoginPage() {
        String result = controller.login();
        assertEquals("clients/login", result);
    }
}
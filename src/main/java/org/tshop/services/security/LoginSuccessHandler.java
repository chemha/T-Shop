package org.tshop.services.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.tshop.entities.ClientEntity;
import org.tshop.repositories.ClientRepository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * After Spring Security authenticates a client, stores the session attributes used by the
 * templates and controllers ("userId", "admin" and "userName").
 */
@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final ClientRepository clientRepository;
    private final Logger logger = LoggerFactory.getLogger(LoginSuccessHandler.class);

    public LoginSuccessHandler(ClientRepository clientRepository) {
        super("/");
        this.clientRepository = clientRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        ClientEntity clientEntity = clientRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("No client found by specified email"));

        HttpSession session = request.getSession();
        session.setAttribute("userId", clientEntity.getId());
        session.setAttribute("admin", clientEntity.getAdmin());
        session.setAttribute("userName", clientEntity.getName());

        logger.info("Client with email {} has logged in", clientEntity.getEmail());

        super.onAuthenticationSuccess(request, response, authentication);
    }
}

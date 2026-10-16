package org.tshop.auth;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * The customer making the request, taken from the validated token. Endpoints under /me never
 * receive a customer id from the client, so nobody can act on someone else's data by changing
 * an id in the URL (the IDOR problem of v1).
 */
@Component
public class CurrentCustomer {

    public Long id() {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken token) {
            return Long.valueOf(token.getToken().getSubject());
        }
        throw new AuthenticationCredentialsNotFoundException("Not authenticated");
    }
}

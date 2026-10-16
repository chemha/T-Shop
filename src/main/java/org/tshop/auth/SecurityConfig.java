package org.tshop.auth;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity
class SecurityConfig {

    private static final String API = "/api/v1";

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        ProblemDetailSecurityHandler errors = new ProblemDetailSecurityHandler(exceptionResolver);
        return http
                // No cookies or sessions: the browser never sends credentials on its own, so
                // cross-site request forgery isn't possible and CSRF tokens aren't needed
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Documentation
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/openapi.yaml").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Public API
                        .requestMatchers(HttpMethod.POST, API + "/auth/register", API + "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, API + "/products", API + "/products/*", API + "/categories").permitAll()
                        // Admin only
                        .requestMatchers(API + "/admin/**").hasRole("ADMIN")
                        .requestMatchers(API + "/products/**", API + "/categories/**").hasRole("ADMIN")
                        // Everything else needs a valid token. Rules are checked in order and anything
                        // not listed is denied by default, so a new endpoint is never public by mistake
                        .requestMatchers(API + "/**").authenticated()
                        .anyRequest().denyAll())
                // Validates the "Authorization: Bearer <jwt>" header on every request
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(errors)
                        .accessDeniedHandler(errors))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(errors)
                        .accessDeniedHandler(errors))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .build();
    }

    /** Turns the "roles" claim, e.g. ["ADMIN"], into the authority ROLE_ADMIN used by hasRole("ADMIN"). */
    private static JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName(TokenService.ROLES_CLAIM);
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    /**
     * New passwords are stored as "{bcrypt}$2a$10$...": the prefix records the algorithm, so it can
     * be changed in the future without breaking existing passwords. Hashes without a prefix (the
     * ones imported from v1) are read as BCrypt.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        DelegatingPasswordEncoder encoder = (DelegatingPasswordEncoder) PasswordEncoderFactories.createDelegatingPasswordEncoder();
        encoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
        return encoder;
    }
}

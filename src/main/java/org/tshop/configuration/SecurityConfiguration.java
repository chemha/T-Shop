package org.tshop.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.tshop.services.security.LoginSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private LoginSuccessHandler loginSuccessHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // The token is also stored in the XSRF-TOKEN cookie so js/csrf.js can send it with AJAX requests
                .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
                .authorizeRequests()
                // Logged-in clients
                .antMatchers("/cart", "/removeCart", "/modifyCart").authenticated()
                .antMatchers("/clients/profile", "/clients/edit", "/clients/password").authenticated()
                .antMatchers("/orders/index", "/orders/create", "/client-addresses/**").authenticated()
                // Admins
                .antMatchers("/clients/index", "/categories/**", "/orders/**", "/goods/**").hasRole("ADMIN")
                .regexMatchers("/clients/\\d+").hasRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/clients").hasRole("ADMIN")
                .antMatchers("/products/create", "/products/*/edit").hasRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/products/**").permitAll()
                .antMatchers("/products/**").hasRole("ADMIN")
                // Everything else (home, cart actions, login, registration) is public
                .anyRequest().permitAll()
                .and()
                .formLogin()
                .loginPage("/clients/login")
                .usernameParameter("email")
                .successHandler(loginSuccessHandler)
                .failureUrl("/clients/login?error")
                .permitAll()
                .and()
                .logout()
                .logoutUrl("/clients/logout")
                .logoutSuccessUrl("/")
                .permitAll();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**", "/js/**", "/vendor/**");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

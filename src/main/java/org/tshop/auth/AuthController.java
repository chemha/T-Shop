package org.tshop.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.tshop.api.AuthApi;
import org.tshop.api.model.CustomerDto;
import org.tshop.api.model.LoginRequestDto;
import org.tshop.api.model.RegisterRequestDto;
import org.tshop.api.model.TokenResponseDto;
import org.tshop.customer.AccountService;
import org.tshop.customer.AccountService.ProfileData;
import org.tshop.customer.CustomerMapper;

@RestController
class AuthController implements AuthApi {

    private final AuthService auth;
    private final AccountService accounts;
    private final CustomerMapper mapper;

    AuthController(AuthService auth, AccountService accounts, CustomerMapper mapper) {
        this.auth = auth;
        this.accounts = accounts;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<CustomerDto> register(RegisterRequestDto request) {
        ProfileData profile = new ProfileData(request.getFirstName(), request.getLastName(), request.getBirthDate(), request.getEmail());
        CustomerDto customer = mapper.toDto(accounts.register(profile, request.getPassword()));
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }

    @Override
    public ResponseEntity<TokenResponseDto> login(LoginRequestDto request) {
        TokenService.Token token = auth.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(new TokenResponseDto(token.value(), "Bearer", token.expiresInSeconds()));
    }
}

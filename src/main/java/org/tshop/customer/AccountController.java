package org.tshop.customer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.tshop.api.AccountApi;
import org.tshop.api.model.ChangePasswordRequestDto;
import org.tshop.api.model.CustomerDto;
import org.tshop.api.model.UpdateProfileRequestDto;
import org.tshop.auth.CurrentCustomer;
import org.tshop.customer.AccountService.ProfileData;

/**
 * Implements the interface generated from openapi.yaml: paths, HTTP methods, parameters and
 * validation come from there. The controller only translates between DTOs and the service.
 */
@RestController
class AccountController implements AccountApi {

    private final AccountService accounts;
    private final CurrentCustomer currentCustomer;
    private final CustomerMapper mapper;

    AccountController(AccountService accounts, CurrentCustomer currentCustomer, CustomerMapper mapper) {
        this.accounts = accounts;
        this.currentCustomer = currentCustomer;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<CustomerDto> getProfile() {
        return ResponseEntity.ok(mapper.toDto(accounts.get(currentCustomer.id())));
    }

    @Override
    public ResponseEntity<CustomerDto> updateProfile(UpdateProfileRequestDto request) {
        ProfileData profile = new ProfileData(request.getFirstName(), request.getLastName(), request.getBirthDate(), request.getEmail());
        return ResponseEntity.ok(mapper.toDto(accounts.updateProfile(currentCustomer.id(), profile)));
    }

    @Override
    public ResponseEntity<Void> changePassword(ChangePasswordRequestDto request) {
        accounts.changePassword(currentCustomer.id(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}

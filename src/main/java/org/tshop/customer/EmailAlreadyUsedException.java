package org.tshop.customer;

import org.tshop.shared.error.ConflictException;

public class EmailAlreadyUsedException extends ConflictException {

    public EmailAlreadyUsedException(String email) {
        super("The email %s is already registered".formatted(email));
    }
}

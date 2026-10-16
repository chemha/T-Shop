package org.tshop.shared.error;

/** The request is valid but conflicts with the current state of the data. Mapped to 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}

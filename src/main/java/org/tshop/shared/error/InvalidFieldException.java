package org.tshop.shared.error;

/**
 * A field is well-formed but rejected by a business rule (e.g. a wrong current password).
 * Mapped to 400 with the same shape as a Bean Validation error.
 */
public class InvalidFieldException extends RuntimeException {

    private final String field;

    public InvalidFieldException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}

package org.tshop.shared.error;

/** The requested resource doesn't exist, or doesn't belong to the current user. Mapped to 404. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String resource, Object id) {
        super("%s %s not found".formatted(resource, id));
    }
}

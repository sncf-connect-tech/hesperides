package org.hesperides.core.domain.exceptions;

public abstract class OutOfDateException extends RuntimeException {

    public OutOfDateException(String message) {
        super(message);
    }
}

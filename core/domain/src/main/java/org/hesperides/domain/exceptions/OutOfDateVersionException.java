package org.hesperides.domain.exceptions;

public class OutOfDateVersionException extends RuntimeException {
    public OutOfDateVersionException(final long expected, final long actual) {
        super("Expected " + expected + " but found " + actual);
    }
}

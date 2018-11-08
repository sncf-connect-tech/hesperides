package org.hesperides.core.domain.exceptions;

public class OutOfDateVersionException extends RuntimeException {
    public OutOfDateVersionException(final Long expected, final Long actual) {
        super("Expected " + expected + " but found " + actual);
    }
}

package org.hesperides.core.domain.exceptions;

public class OutOfDateVersionException extends RuntimeException {
    public OutOfDateVersionException(final Long expected, final Long actual) {
        super("Invalid versionId: expected " + expected + " but found " + actual);
    }
}

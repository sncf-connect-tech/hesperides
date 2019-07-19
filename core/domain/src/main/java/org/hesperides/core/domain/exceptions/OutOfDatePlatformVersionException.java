package org.hesperides.core.domain.exceptions;

public class OutOfDatePlatformVersionException extends OutOfDateException {
    public OutOfDatePlatformVersionException(final Long expected, final Long actual) {
        super("Invalid platform VersionId for : expected " + expected + " but found " + actual);
    }
}

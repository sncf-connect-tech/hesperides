package org.hesperides.core.domain.exceptions;

import org.hesperides.core.domain.modules.entities.Module;

public class OutOfDatePlatformVersionException extends RuntimeException {
    public OutOfDatePlatformVersionException(final Long expected, final Long actual) {
        super("Invalid platform VersionId for : expected " + expected + " but found " + actual);
    }
}

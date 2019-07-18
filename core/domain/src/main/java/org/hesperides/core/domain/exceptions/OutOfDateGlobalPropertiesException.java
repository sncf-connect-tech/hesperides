package org.hesperides.core.domain.exceptions;

import org.hesperides.core.domain.modules.entities.Module;

public class OutOfDateGlobalPropertiesException extends RuntimeException {
    public OutOfDateGlobalPropertiesException(final Long expected, final Long actual) {
        super("Invalid global propertiesVersionId for : expected " + expected + " but found " + actual);
    }
}

package org.hesperides.core.domain.exceptions;

public class OutOfDateGlobalPropertiesException extends OutOfDateException {
    public OutOfDateGlobalPropertiesException(final Long expected, final Long actual) {
        super("Invalid global propertiesVersionId for : expected " + expected + " but found " + actual);
    }
}

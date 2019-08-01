package org.hesperides.core.domain.exceptions;

import org.hesperides.core.domain.modules.entities.Module;

public class OutOfDatePropertiesException extends OutOfDateException {
    public OutOfDatePropertiesException(final String path, final Long expected, final Long actual) {
        super("Invalid propertiesVersionId for " + Module.Key.fromPropertiesPath(path).toString() + ": expected " + expected + " but found " + actual);
    }
}

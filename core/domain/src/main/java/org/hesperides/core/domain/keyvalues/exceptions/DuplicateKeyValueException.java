package org.hesperides.core.domain.keyvalues.exceptions;

import org.hesperides.core.domain.exceptions.DuplicateException;

public class DuplicateKeyValueException extends DuplicateException {
    public DuplicateKeyValueException(String key) {
        super("Could not create a new key-value with key " + key + " as it already exists");
    }
}

package org.hesperides.application.exceptions;

import org.hesperides.domain.modules.entities.Module;

public class DuplicateModuleException extends RuntimeException {
    public DuplicateModuleException(Module.Key newModuleKey) {
        super("could not create a new module with key: " + newModuleKey + " as it already exists");
    }
}

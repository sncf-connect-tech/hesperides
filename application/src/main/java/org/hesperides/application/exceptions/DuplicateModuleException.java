package org.hesperides.application.exceptions;

import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.exceptions.DuplicateException;

public class DuplicateModuleException extends DuplicateException {
    public DuplicateModuleException(Module.Key newModuleKey) {
        super("could not create a new module with key: " + newModuleKey + " as it already exists");
    }
}

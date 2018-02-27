package org.hesperides.application.exceptions;

import org.hesperides.domain.modules.entities.Module;

public class ModuleNotFoundException extends RuntimeException {
    public ModuleNotFoundException(Module.Key newModuleKey) {
        super("could not update a new module with key: " + newModuleKey + " as it does not exist");
    }
}

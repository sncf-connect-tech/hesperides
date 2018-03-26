package org.hesperides.application.exceptions;

import org.hesperides.domain.modules.entities.Module;
import org.hesperides.domain.modules.exceptions.NotFoundException;

public class ModuleNotFoundException extends NotFoundException {
    public ModuleNotFoundException(Module.Key newModuleKey) {
        super("could not update a new module with key: " + newModuleKey + " as it does not exist");
    }
}

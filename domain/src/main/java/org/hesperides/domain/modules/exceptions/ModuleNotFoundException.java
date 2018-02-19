package org.hesperides.domain.modules.exceptions;

import org.hesperides.domain.modules.entities.Module;

public class ModuleNotFoundException extends NotFoundException {

    public ModuleNotFoundException(Module.Key key) {
        super("Could not find module info for " + key);
    }
}

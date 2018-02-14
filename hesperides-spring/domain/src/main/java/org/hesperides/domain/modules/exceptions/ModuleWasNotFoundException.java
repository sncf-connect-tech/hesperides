package org.hesperides.domain.modules.exceptions;

import org.hesperides.domain.modules.entities.Module;

public class ModuleWasNotFoundException extends NotFoundException {

    public ModuleWasNotFoundException(Module.Key key) {
        super("Could not find module info for " + key);
    }
}

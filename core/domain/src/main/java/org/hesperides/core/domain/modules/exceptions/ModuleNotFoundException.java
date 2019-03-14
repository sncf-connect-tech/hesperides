package org.hesperides.core.domain.modules.exceptions;

import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.modules.entities.Module;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

public class ModuleNotFoundException extends NotFoundException {
    public ModuleNotFoundException(TemplateContainer.Key key) {
        super("Could not find module info for " + key.getNamespaceWithoutPrefix());
    }

    public ModuleNotFoundException(Module.Key key, String modulePath) {
        super("Could not find module " + key.getNamespaceWithoutPrefix() + " at path " + modulePath);
    }
}

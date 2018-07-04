package org.hesperides.domain.modules.exceptions;

import org.hesperides.domain.exceptions.NotFoundException;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;

public class ModuleNotFoundException extends NotFoundException {
    public ModuleNotFoundException(TemplateContainer.Key key) {
        super("Could not find module info for " + key);
    }
}

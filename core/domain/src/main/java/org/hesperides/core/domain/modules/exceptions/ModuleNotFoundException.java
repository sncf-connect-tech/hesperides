package org.hesperides.core.domain.modules.exceptions;

import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

public class ModuleNotFoundException extends NotFoundException {
    public ModuleNotFoundException(TemplateContainer.Key key) {
        super("Could not find module info for " + key);
    }
}

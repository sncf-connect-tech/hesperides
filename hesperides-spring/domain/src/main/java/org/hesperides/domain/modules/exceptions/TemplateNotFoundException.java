package org.hesperides.domain.modules.exceptions;

import org.hesperides.domain.modules.entities.Module;

public class TemplateNotFoundException extends NotFoundException {
    public TemplateNotFoundException(Module.Key moduleKey, String templateName) {
        super("Could not find template in " + moduleKey + "/" + templateName);
    }
}

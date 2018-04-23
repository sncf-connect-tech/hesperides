package org.hesperides.domain.modules.exceptions;

import org.hesperides.domain.exceptions.NotFoundException;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

public class TemplateNotFoundException extends NotFoundException {
    public TemplateNotFoundException(TemplateContainer.Key moduleKey, String templateName) {
        super("Could not find template in " + moduleKey + "/" + templateName);
    }
}

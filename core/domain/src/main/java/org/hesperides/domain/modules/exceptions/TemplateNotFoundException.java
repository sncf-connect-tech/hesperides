package org.hesperides.domain.modules.exceptions;

import org.hesperides.domain.exceptions.NotFoundException;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;

public class TemplateNotFoundException extends NotFoundException {
    public TemplateNotFoundException(TemplateContainer.Key templateContainerKey, String templateName) {
        super("Could not find template in " + templateContainerKey + "/" + templateName);
    }
}

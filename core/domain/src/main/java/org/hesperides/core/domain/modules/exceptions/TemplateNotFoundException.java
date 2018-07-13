package org.hesperides.core.domain.modules.exceptions;

import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

public class TemplateNotFoundException extends NotFoundException {
    public TemplateNotFoundException(TemplateContainer.Key templateContainerKey, String templateName) {
        super("Could not find template in " + templateContainerKey + "/" + templateName);
    }
}

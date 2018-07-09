package org.hesperides.domain.modules.exceptions;

import org.hesperides.domain.exceptions.DuplicateException;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;

public class DuplicateTemplateCreationException extends DuplicateException {
    public DuplicateTemplateCreationException(TemplateContainer.Key templateContainerKey, String templateName) {
        super("Template " + templateContainerKey + "/" + templateName + " already exists");
    }
}

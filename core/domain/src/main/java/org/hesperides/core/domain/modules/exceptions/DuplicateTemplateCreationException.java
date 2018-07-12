package org.hesperides.core.domain.modules.exceptions;

import org.hesperides.core.domain.exceptions.DuplicateException;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

public class DuplicateTemplateCreationException extends DuplicateException {
    public DuplicateTemplateCreationException(TemplateContainer.Key templateContainerKey, String templateName) {
        super("Template " + templateContainerKey + "/" + templateName + " already exists");
    }
}

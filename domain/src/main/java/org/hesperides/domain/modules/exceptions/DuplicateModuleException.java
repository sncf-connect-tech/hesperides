package org.hesperides.domain.modules.exceptions;

import org.hesperides.domain.exceptions.DuplicateException;
import org.hesperides.domain.templatecontainers.entities.TemplateContainer;

public class DuplicateModuleException extends DuplicateException {
    public DuplicateModuleException(TemplateContainer.Key newModuleKey) {
        super("could not create a new module with key: " + newModuleKey + " as it already exists");
    }
}

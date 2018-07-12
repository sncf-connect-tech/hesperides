package org.hesperides.core.domain.modules.exceptions;

import org.hesperides.core.domain.exceptions.DuplicateException;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

public class DuplicateModuleException extends DuplicateException {
    public DuplicateModuleException(TemplateContainer.Key newModuleKey) {
        super("could not create a new module with key: " + newModuleKey + " as it already exists");
    }
}

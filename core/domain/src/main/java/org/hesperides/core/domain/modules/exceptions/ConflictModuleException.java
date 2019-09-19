package org.hesperides.core.domain.modules.exceptions;

import org.hesperides.core.domain.exceptions.DuplicateException;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

public class ConflictModuleException extends DuplicateException {
    public ConflictModuleException(TemplateContainer.Key newModuleKey) {
        super("could not delete a module with key: "+ newModuleKey.getNamespaceWithoutPrefix()+ " as it's used by an existing platform" );
    }
}

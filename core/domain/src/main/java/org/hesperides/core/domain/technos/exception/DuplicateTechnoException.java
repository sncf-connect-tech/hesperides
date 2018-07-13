package org.hesperides.core.domain.technos.exception;

import org.hesperides.core.domain.exceptions.DuplicateException;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;

public class DuplicateTechnoException extends DuplicateException {
    public DuplicateTechnoException(TemplateContainer.Key technoKey) {
        super("could not create a techno with key: " + technoKey + " as it already exists");
    }
}

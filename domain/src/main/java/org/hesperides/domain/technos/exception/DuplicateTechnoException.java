package org.hesperides.domain.technos.exception;

import org.hesperides.domain.exceptions.DuplicateException;
import org.hesperides.domain.templatecontainer.entities.TemplateContainer;

public class DuplicateTechnoException extends DuplicateException {
    public DuplicateTechnoException(TemplateContainer.Key technoKey) {
        super("could not create a techno with key: " + technoKey + " as it already exists");
    }
}

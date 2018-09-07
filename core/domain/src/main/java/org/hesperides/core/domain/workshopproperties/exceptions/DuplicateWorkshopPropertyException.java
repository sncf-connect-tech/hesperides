package org.hesperides.core.domain.workshopproperties.exceptions;

import org.hesperides.core.domain.exceptions.DuplicateException;

public class DuplicateWorkshopPropertyException extends DuplicateException {
    public DuplicateWorkshopPropertyException(String key) {
        super("could not create a new workshop property with key: " + key + " as it already exists");
    }
}

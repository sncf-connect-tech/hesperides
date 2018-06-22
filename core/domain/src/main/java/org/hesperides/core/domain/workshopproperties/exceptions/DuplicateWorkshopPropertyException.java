package org.hesperides.domain.workshopproperties.exceptions;

import org.hesperides.domain.exceptions.DuplicateException;

public class DuplicateWorkshopPropertyException extends DuplicateException {
    public DuplicateWorkshopPropertyException(String key) {
        super("could not create a new workshop property with key: " + key + " as it already exists");
    }
}

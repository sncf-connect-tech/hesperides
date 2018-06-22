package org.hesperides.domain.workshopproperties.exceptions;

import org.hesperides.domain.exceptions.NotFoundException;

public class WorkshopPropertyNotFoundException extends NotFoundException {
    public WorkshopPropertyNotFoundException(String key) {
        super("Could not find workshop property info for " + key);
    }
}

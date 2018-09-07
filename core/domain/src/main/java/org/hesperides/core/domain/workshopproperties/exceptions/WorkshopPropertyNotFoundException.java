package org.hesperides.core.domain.workshopproperties.exceptions;

import org.hesperides.core.domain.exceptions.NotFoundException;

public class WorkshopPropertyNotFoundException extends NotFoundException {
    public WorkshopPropertyNotFoundException(String key) {
        super("Could not find workshop property info for " + key);
    }
}

package org.hesperides.core.domain.platforms.exceptions;

import org.hesperides.core.domain.exceptions.NotFoundException;

public class ApplicationNotFoundException extends NotFoundException {
    public ApplicationNotFoundException(String applicationName) {
        super("Could not find application info for " + applicationName);
    }
}

package org.hesperides.domain.platforms.exceptions;

import org.hesperides.domain.exceptions.NotFoundException;

public class ApplicationNotFoundException extends NotFoundException {
    public ApplicationNotFoundException(String appName) {
        super("Could not find application info for " + appName);
    }
}

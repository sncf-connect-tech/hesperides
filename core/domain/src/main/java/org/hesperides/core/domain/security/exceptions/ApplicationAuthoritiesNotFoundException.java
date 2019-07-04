package org.hesperides.core.domain.security.exceptions;

import org.hesperides.core.domain.exceptions.NotFoundException;

public class ApplicationAuthoritiesNotFoundException extends NotFoundException {
    public ApplicationAuthoritiesNotFoundException(String applicationName) {
        super("Authorities not found for application " + applicationName);
    }
}

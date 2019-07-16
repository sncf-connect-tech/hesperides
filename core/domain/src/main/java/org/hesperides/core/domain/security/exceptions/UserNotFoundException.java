package org.hesperides.core.domain.security.exceptions;

import org.hesperides.core.domain.exceptions.NotFoundException;
import org.hesperides.core.domain.platforms.entities.Platform;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String username) {
        super("Could not find user with name " + username);
    }
}

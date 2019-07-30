package org.hesperides.core.domain.security.exceptions;

import org.hesperides.core.domain.exceptions.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String username) {
        super("Could not find user with name " + username);
    }
}

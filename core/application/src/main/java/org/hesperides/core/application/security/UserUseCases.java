package org.hesperides.core.application.security;

import org.hesperides.core.domain.security.entities.User;
import org.hesperides.core.domain.security.exceptions.UserNotFoundException;
import org.hesperides.core.domain.security.queries.UserQueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserUseCases {

    private final UserQueries userQueries;

    @Autowired
    public UserUseCases(UserQueries userQueries) {
        this.userQueries = userQueries;
    }

    public User getUser(String username) {
        return userQueries.getOptionalUser(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }
}

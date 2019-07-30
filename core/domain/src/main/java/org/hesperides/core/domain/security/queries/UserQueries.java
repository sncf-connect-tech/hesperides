package org.hesperides.core.domain.security.queries;

import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.commons.axon.AxonQueries;
import org.hesperides.core.domain.security.GetUserQuery;
import org.hesperides.core.domain.security.entities.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserQueries extends AxonQueries {
    protected UserQueries(QueryGateway queryGateway) {
        super(queryGateway);
    }

    public Optional<User> getOptionalUser(String username) {
        return querySyncOptional(new GetUserQuery(username), User.class);
    }
}

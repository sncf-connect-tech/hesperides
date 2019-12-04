package org.hesperides.core.domain.keyvalues;

import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.keyvalues.queries.views.KeyValueView;

import java.util.Optional;

public interface KeyValueProjectionRepository {

    /*** EVENT HANDLERS ***/

    @EventHandler
    void onKeyValueCreatedEvent(KeyValueCreatedEvent event);

    @EventHandler
    void onKeyValueUpdatedEvent(KeyValueUpdatedEvent event);

    @EventHandler
    void onKeyValueDeletedEvent(KeyValueDeletedEvent event);

    /*** QUERY HANDLERS ***/

    @QueryHandler
    Boolean onKeyValueExistsQuery(KeyValueExistsQuery query);

    @QueryHandler
    Optional<KeyValueView> onGetKeyValueQuery(GetKeyValueQuery query);
}

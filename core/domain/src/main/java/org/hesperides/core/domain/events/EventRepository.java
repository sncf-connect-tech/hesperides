package org.hesperides.core.domain.events;

import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.events.queries.EventView;

import java.util.List;

public interface EventRepository {

    @QueryHandler
    List<EventView> onGetEventsByAggregateIdentifierQuery(GetEventsByAggregateIdentifierQuery query);

    void cleanAggregateEvents(String aggregateIdentifier);
}

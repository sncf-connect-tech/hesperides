package org.hesperides.core.domain.events;

import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.events.queries.EventView;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository {

    @QueryHandler
    List<EventView> onGetLastToFirstEventsQuery(GetLastToFirstEventsQuery query);

    @QueryHandler
    List<EventView> onGetLastToFirstPlatformModulePropertiesUpdatedEvents(GetLastToFirstPlatformModulePropertiesUpdatedEvents query);

    void cleanAggregateEvents(String aggregateIdentifier);
}

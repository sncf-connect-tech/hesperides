package org.hesperides.core.domain.events.queries;

import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.commons.axon.AxonQueries;
import org.hesperides.core.domain.events.GetLastToFirstEventsQuery;
import org.hesperides.core.domain.events.GetLastToFirstPlatformModulePropertiesUpdatedEvents;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class EventQueries extends AxonQueries {

    protected EventQueries(QueryGateway queryGateway) {
        super(queryGateway);
    }

    public List<EventView> getLastToFirstEvents(String aggregateId, Integer page, Integer size) {
        return getLastToFirstEventsByTypes(aggregateId, new Class[0], page, size);
    }

    public List<EventView> getLastToFirstEventsByType(String aggregateId, Class eventType, Integer page, Integer size) {
        return getLastToFirstEventsByTypes(aggregateId, new Class[]{eventType}, page, size);
    }

    public List<EventView> getLastToFirstEventsByTypes(String aggregateId, Class[] eventTypes, Integer page, Integer size) {
        return querySyncList(new GetLastToFirstEventsQuery(aggregateId, eventTypes, page, size), EventView.class);
    }

    public List<EventView> getLastToFirstPlatformModulePropertiesUpdatedEvents(String aggregateId, String propertiesPath, Integer page, Integer size) {
        return querySyncList(new GetLastToFirstPlatformModulePropertiesUpdatedEvents(aggregateId, propertiesPath, page, size), EventView.class);
    }
}

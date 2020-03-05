package org.hesperides.core.domain.events.queries;

import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.commons.axon.AxonQueries;
import org.hesperides.core.domain.events.GetEventsQuery;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class EventQueries extends AxonQueries {

    protected EventQueries(QueryGateway queryGateway) {
        super(queryGateway);
    }

    public List<EventView> getEvents(String aggregateId, Integer page, Integer size) {
        return getEventsByTypes(aggregateId, new Class[0], page, size);
    }

    public List<EventView> getEventsByTypes(String aggregateId, Class[] eventTypes, Integer page, Integer size) {
        return querySyncList(new GetEventsQuery(aggregateId, eventTypes, page, size), EventView.class);
    }
}

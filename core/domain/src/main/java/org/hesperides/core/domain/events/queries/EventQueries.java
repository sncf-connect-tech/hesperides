package org.hesperides.core.domain.events.queries;

import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.commons.axon.AxonQueries;
import org.hesperides.core.domain.events.GetEventsByAggregateIdentifierQuery;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class EventQueries extends AxonQueries {

    protected EventQueries(QueryGateway queryGateway) {
        super(queryGateway);
    }

    public List<EventView> getEvents(String aggregateId, Integer page, Integer size) {
        return getEventsByTypes(aggregateId, new Class[0], page, size);
    }

    public List<EventView> getEventsByTypes(String aggregateId, Class[] eventTypes, Integer page, Integer size) {
        List<EventView> events = querySyncList(new GetEventsByAggregateIdentifierQuery(aggregateId, eventTypes, page, size), EventView.class);
        return events.stream().sorted(Comparator.comparing(EventView::getTimestamp).reversed()).collect(Collectors.toList());
    }
}

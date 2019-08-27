package org.hesperides.core.domain.events.queries;

import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.commons.axon.AxonQueries;
import org.hesperides.core.domain.events.GenericEventsByStreamQuery;
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
        List<EventView> events = querySyncList(new GenericEventsByStreamQuery(aggregateId), EventView.class);
        return ordinateAndPaginateAccordingSize(events, page, size);
    }

    private List<EventView> ordinateAndPaginateAccordingSize(final List<EventView> events, Integer page, Integer size) {
        return events.stream().sorted(Comparator.comparing(EventView::getTimestamp).reversed())
                .skip((page - 1) * size)
                .limit(size)
                .collect(Collectors.toList());
    }
}

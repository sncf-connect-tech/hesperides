package org.hesperides.core.domain.events.queries;

import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.commons.axon.AxonQueries;
import org.hesperides.core.domain.events.GenericEventsByStreamQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public List<EventView> getPageEvents(final String aggregateId, final Pageable pageable) {
        List<EventView> events = querySyncList(new GenericEventsByStreamQuery(aggregateId), EventView.class);
        return createEventPage(events, pageable);
    }

    private List<EventView> createEventPage(final List<EventView> events, final Pageable pageable) {
        // TODO utiliser le sort du pageable
        return events.stream().sorted(Comparator.comparing(EventView::getTimestamp).reversed())
                .skip(pageable.getPageNumber() * pageable.getPageSize())
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());
    }
}

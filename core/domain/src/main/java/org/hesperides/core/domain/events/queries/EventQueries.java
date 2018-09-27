package org.hesperides.core.domain.events.queries;

import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.commons.axon.AxonQueries;
import org.hesperides.core.domain.events.GenericEventsByStreamQuery;
import org.hesperides.core.domain.events.PlatformEventsByStreamQuery;
import org.hesperides.core.domain.platforms.entities.Platform;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class EventQueries extends AxonQueries {


    protected EventQueries(QueryGateway queryGateway) {
        super(queryGateway);
    }

    public List<EventView> getEvents(TemplateContainer.Key key, Integer page, Integer size) {
        return ordinateAndPaginateAccordingSize(querySyncList(new GenericEventsByStreamQuery(key), EventView.class), page, size);
    }

    public List<EventView> getEvents(Platform.Key key, Integer page, Integer size) {
        return ordinateAndPaginateAccordingSize(querySyncList(new PlatformEventsByStreamQuery(key), EventView.class), page, size);
    }

    private List<EventView> ordinateAndPaginateAccordingSize(final List<EventView> events, Integer page, Integer size) {
        return events.stream().sorted(Comparator.comparing(EventView::getTimestamp).reversed())
                .skip((page - 1) * size)
                .limit(size)
                .collect(Collectors.toList());
    }
}

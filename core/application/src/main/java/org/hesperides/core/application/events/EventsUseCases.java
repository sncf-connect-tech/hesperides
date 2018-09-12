package org.hesperides.core.application.events;

import org.hesperides.core.domain.events.queries.EventQueries;
import org.hesperides.core.domain.events.queries.EventView;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventsUseCases {

    private final EventQueries queries;

    @Autowired
    public EventsUseCases(EventQueries queries) {
        this.queries = queries;
    }

    public List<EventView> getEvents(TemplateContainer.Key key) {
        return queries.getEvents(key);
    }
}

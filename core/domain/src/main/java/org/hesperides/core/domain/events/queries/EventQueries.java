package org.hesperides.core.domain.events.queries;

import org.axonframework.queryhandling.QueryGateway;
import org.hesperides.commons.axon.AxonQueries;
import org.hesperides.core.domain.events.EventsByStreamQuery;
import org.hesperides.core.domain.templatecontainers.entities.TemplateContainer;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class EventQueries extends AxonQueries {


    protected EventQueries(QueryGateway queryGateway) {
        super(queryGateway);
    }

    public List<EventView> getEvents(TemplateContainer.Key key) {
        return querySyncList(new EventsByStreamQuery(key), EventView.class);
    }
}

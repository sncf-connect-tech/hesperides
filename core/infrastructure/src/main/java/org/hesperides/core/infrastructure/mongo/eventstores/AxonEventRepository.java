package org.hesperides.core.infrastructure.mongo.eventstores;

import io.micrometer.core.annotation.Timed;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.events.EventRepository;
import org.hesperides.core.domain.events.GenericEventsByStreamQuery;
import org.hesperides.core.domain.events.queries.EventView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static org.hesperides.commons.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.SpringProfiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class AxonEventRepository implements EventRepository {

    private EventStorageEngine eventStore;

    @Autowired
    public AxonEventRepository(EventStorageEngine eventStore) {
        this.eventStore = eventStore;
    }

    @QueryHandler
    @Override
    @Timed
    public List<EventView> onGetEventsStream(final GenericEventsByStreamQuery query) {
        return getEventViews(query.getAggregateId());
    }

    private List<EventView> getEventViews(final String aggregateIdentifier) {
        return eventStore.readEvents(aggregateIdentifier)
                .asStream()
                .map(EventView::new)
                .collect(Collectors.toList());
    }

    public boolean hasOneEvent(final String aggregateIdentifier) {
        return getEventViews(aggregateIdentifier).size() == 1;
    }
}

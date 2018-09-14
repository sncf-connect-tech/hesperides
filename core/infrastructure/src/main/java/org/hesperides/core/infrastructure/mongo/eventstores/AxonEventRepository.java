package org.hesperides.core.infrastructure.mongo.eventstores;

import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.events.EventRepository;
import org.hesperides.core.domain.events.GenericEventsByStreamQuery;
import org.hesperides.core.domain.events.PlatformEventsByStreamQuery;
import org.hesperides.core.domain.events.queries.EventView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static org.hesperides.commons.spring.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.spring.SpringProfiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class AxonEventRepository implements EventRepository {


    EventStore eventStore;

    @Autowired
    public AxonEventRepository(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    @QueryHandler
    @Override
    public List<EventView> onGetEventsStream(final GenericEventsByStreamQuery query) {
        return getEventViews(query.getEventStream().toString());
    }

    @QueryHandler
    @Override
    public List<EventView> onGetEventsStream(final PlatformEventsByStreamQuery query) {
        return getEventViews(query.getEventStream().toString());
    }

    private List<EventView> getEventViews(final String aggragateIdentifier) {
        return eventStore.readEvents(aggragateIdentifier).asStream()
                .map(EventView::new).collect(Collectors.toList());
    }
}

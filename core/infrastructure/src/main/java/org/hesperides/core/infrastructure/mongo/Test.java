package org.hesperides.core.infrastructure.mongo;

import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.events.EventRepository;
import org.hesperides.core.domain.events.EventsByStreamQuery;
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
public class Test implements EventRepository {


    EventStorageEngine eventStore;

    @Autowired
    public Test(EventStorageEngine eventStore) {
        this.eventStore = eventStore;
    }

    @QueryHandler
    @Override
    public List<EventView> onGetEventsStream(EventsByStreamQuery query) {
        return eventStore.readEvents(query.getEventStream().toString()).asStream()
                .map(EventView::new).collect(Collectors.toList());
    }
}

package org.hesperides.core.infrastructure.mongo.events;

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
public class MongoAxonEventRepository implements EventRepository {

    private final EventStorageEngine eventStorageEngine;
    private final MongoEventRepository mongoEventRepository;

    @Autowired
    public MongoAxonEventRepository(EventStorageEngine eventStorageEngine, MongoEventRepository mongoEventRepository) {
        this.eventStorageEngine = eventStorageEngine;
        this.mongoEventRepository = mongoEventRepository;
    }

    @QueryHandler
    @Override
    @Timed
    public List<EventView> onGenericEventsByStreamQuery(final GenericEventsByStreamQuery query) {
        return eventStorageEngine.readEvents(query.getAggregateIdentifier())
                .asStream()
                .map(EventView::new)
                .collect(Collectors.toList());
    }

    @Override
    @Timed
    public void cleanAggregateEvents(String aggregateIdentifier) {
        mongoEventRepository.deleteAllByAggregateIdentifier(aggregateIdentifier);
    }
}

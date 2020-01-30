package org.hesperides.core.infrastructure.mongo.events;

import io.micrometer.core.annotation.Timed;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.events.EventRepository;
import org.hesperides.core.domain.events.GetEventsByAggregateIdentifierQuery;
import org.hesperides.core.domain.events.queries.EventView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
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
    public List<EventView> onGenericEventsByStreamQuery(final GetEventsByAggregateIdentifierQuery query) {
        return eventStorageEngine.readEvents(query.getAggregateIdentifier())
                .asStream()
                .skip((query.getPage() - 1) * query.getSize())
                .limit(query.getSize())
                .map(EventView::new)
                .filter(eventView -> query.getEventTypes().length == 0
                        || Arrays.stream(query.getEventTypes()).anyMatch(userEventClass -> eventView.getData().getClass().equals(userEventClass)))
                .collect(Collectors.toList());
    }

    @Override
    @Timed
    public void cleanAggregateEvents(String aggregateIdentifier) {
        mongoEventRepository.deleteAllByAggregateIdentifier(aggregateIdentifier);
    }
}

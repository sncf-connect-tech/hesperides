package org.hesperides.core.infrastructure.mongo.events;

import com.thoughtworks.xstream.XStream;
import io.micrometer.core.annotation.Timed;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.axonframework.eventsourcing.GenericDomainEventMessage;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.events.EventRepository;
import org.hesperides.core.domain.events.GetEventsByAggregateIdentifierQuery;
import org.hesperides.core.domain.events.queries.EventView;
import org.hesperides.core.domain.platforms.PlatformCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
    private final MongoTemplate mongoTemplate;

    @Autowired
    public MongoAxonEventRepository(EventStorageEngine eventStorageEngine, MongoEventRepository mongoEventRepository, MongoTemplate mongoTemplate) {
        this.eventStorageEngine = eventStorageEngine;
        this.mongoEventRepository = mongoEventRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @QueryHandler
    @Override
    @Timed
    public List<EventView> onGetEventsByAggregateIdentifierQuery(GetEventsByAggregateIdentifierQuery query) {

        List<EventDocument> allByAggregateIdentifier = mongoEventRepository.findAllByAggregateIdentifier(query.getAggregateIdentifier());

        XStream xStream = new XStream();
        PlatformCreatedEvent e = (PlatformCreatedEvent) xStream.fromXML(allByAggregateIdentifier.get(0).getSerializedPayload());

        return eventStorageEngine.readEvents(query.getAggregateIdentifier())
                .asStream()
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

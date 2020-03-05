package org.hesperides.core.infrastructure.mongo.events;

import io.micrometer.core.annotation.Timed;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.events.EventRepository;
import org.hesperides.core.domain.events.GetEventsQuery;
import org.hesperides.core.domain.events.queries.EventView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hesperides.commons.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.SpringProfiles.MONGO;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoAxonEventRepository implements EventRepository {

    private final MongoEventRepository mongoEventRepository;

    @Autowired
    public MongoAxonEventRepository(MongoEventRepository mongoEventRepository) {
        this.mongoEventRepository = mongoEventRepository;
    }

    @QueryHandler
    @Override
    @Timed
    public List<EventView> onGetEventsQuery(GetEventsQuery query) {
        List<String> payloadTypes = Stream.of(query.getEventTypes()).map(Class::getName).collect(Collectors.toList());
        Pageable pageable = query.getPage() > 0 && query.getSize() > 0
                ? PageRequest.of(query.getPage() - 1, query.getSize())
                : Pageable.unpaged();

        List<EventDocument> events = CollectionUtils.isEmpty(payloadTypes)
                ? mongoEventRepository.findAllByAggregateIdentifierOrderByTimestampDesc(query.getAggregateIdentifier(), pageable)
                : mongoEventRepository.findAllByAggregateIdentifierAndPayloadTypeInOrderByTimestampDesc(query.getAggregateIdentifier(), payloadTypes, pageable);

        return events.stream()
                .map(EventDocument::toEventView)
                .collect(Collectors.toList());
    }

    @Override
    @Timed
    public void cleanAggregateEvents(String aggregateIdentifier) {
        mongoEventRepository.deleteAllByAggregateIdentifier(aggregateIdentifier);
    }
}

package org.hesperides.core.infrastructure.mongo.events;

import io.micrometer.core.annotation.Timed;
import org.axonframework.queryhandling.QueryHandler;
import org.hesperides.core.domain.events.EventRepository;
import org.hesperides.core.domain.events.GetLastToFirstEventsQuery;
import org.hesperides.core.domain.events.GetLastToFirstPlatformModulePropertiesUpdatedEvents;
import org.hesperides.core.domain.events.queries.EventView;
import org.hesperides.core.domain.platforms.PlatformModulePropertiesUpdatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hesperides.commons.SpringProfiles.FAKE_MONGO;
import static org.hesperides.commons.SpringProfiles.MONGO;
import static org.springframework.util.CollectionUtils.isEmpty;

@Profile({MONGO, FAKE_MONGO})
@Repository
public class MongoAxonEventRepository implements EventRepository {

    private final MongoEventRepository mongoEventRepository;

    @Autowired
    public MongoAxonEventRepository(MongoEventRepository mongoEventRepository) {
        this.mongoEventRepository = mongoEventRepository;
    }

    @Override
    @Timed
    @QueryHandler
    public List<EventView> onGetLastToFirstEventsQuery(GetLastToFirstEventsQuery query) {
        List<String> payloadTypes = Stream.of(query.getEventTypes())
                .map(Class::getName)
                .collect(toList());

        Pageable pageable = buildPageable(query.getPage(), query.getSize());

        List<EventDocument> events = isEmpty(payloadTypes)
                ? mongoEventRepository.findAllByAggregateIdentifierOrderByTimestampDesc(query.getAggregateIdentifier(), pageable)
                : mongoEventRepository.findAllByAggregateIdentifierAndPayloadTypeInOrderByTimestampDesc(query.getAggregateIdentifier(), payloadTypes, pageable);

        return events.stream()
                .map(EventDocument::toEventView)
                .collect(toList());
    }

    @Override
    public List<EventView> onGetLastToFirstPlatformModulePropertiesUpdatedEvents(GetLastToFirstPlatformModulePropertiesUpdatedEvents query) {
        String propertiesPathPayload = "<propertiesPath>" + query.getPropertiesPath() + "</propertiesPath>";
        Pageable pageable = buildPageable(query.getPage(), query.getSize());

        return mongoEventRepository.findAllByAggregateIdentifierAndPayloadTypeAndSerializedPayloadLikeOrderByTimestampDesc(
                query.getAggregateIdentifier(),
                PlatformModulePropertiesUpdatedEvent.class.getName(),
                propertiesPathPayload,
                pageable)
                .stream()
                .map(EventDocument::toEventView)
                .collect(toList());
    }

    @Override
    @Timed
    public void cleanAggregateEvents(String aggregateIdentifier) {
        mongoEventRepository.deleteAllByAggregateIdentifier(aggregateIdentifier);
    }

    private static Pageable buildPageable(Integer page, Integer size) {
        return page != null && size != null && page > 0 && size > 0
                ? PageRequest.of(page - 1, size)
                : Pageable.unpaged();
    }
}

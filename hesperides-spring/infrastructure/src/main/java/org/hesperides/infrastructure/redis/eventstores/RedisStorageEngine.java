package org.hesperides.infrastructure.redis.eventstores;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.axonframework.eventsourcing.eventstore.DomainEventStream;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.TrackingToken;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Component
public class RedisStorageEngine implements EventStorageEngine {

    private final StringRedisTemplate template;
    private final Codec codec;

    public RedisStorageEngine(StringRedisTemplate template, Codec codec) {
        this.template = template;
        this.codec = codec;
    }

    @Override
    public void appendEvents(List<? extends EventMessage<?>> events) {

        log.debug("append {} events to redis", events.size());

        template.execute(new SessionCallback<List<String>>() {
            @Override
            public List<String> execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                events.stream()
                        .filter(event -> event instanceof DomainEventMessage)
                        .forEach(event -> operations.opsForList()
                                .rightPush(((DomainEventMessage) event).getAggregateIdentifier(),
                                        codec.code((DomainEventMessage) event)));
                return operations.exec();
            }
        });
    }

    @Override
    public void storeSnapshot(DomainEventMessage<?> snapshot) {

        log.debug("storing snapshot (todo)");

    }

    @Override
    public Stream<? extends TrackedEventMessage<?>> readEvents(TrackingToken trackingToken, boolean mayBlock) {

        log.debug("Read events. (tracked)");

        return null;
    }

    @Override
    public DomainEventStream readEvents(String aggregateIdentifier, long firstSequenceNumber) {

        log.debug("Read events from {}", firstSequenceNumber);
        List<String> range = template.opsForList().range(aggregateIdentifier, firstSequenceNumber, -1);
        log.debug("Read: {}", range);
        return DomainEventStream.of(codec.decode(aggregateIdentifier, firstSequenceNumber, range));
    }

    @Override
    public Optional<DomainEventMessage<?>> readSnapshot(String aggregateIdentifier) {
        log.debug("reading snapshot (todo)");
        return Optional.empty();
    }
}

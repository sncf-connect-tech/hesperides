package org.hesperides.infrastructure.redis.eventstores;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.common.Assert;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.axonframework.eventsourcing.eventstore.AbstractEventStorageEngine;
import org.axonframework.eventsourcing.eventstore.DomainEventData;
import org.axonframework.eventsourcing.eventstore.TrackedEventData;
import org.axonframework.eventsourcing.eventstore.TrackingToken;
import org.axonframework.serialization.Serializer;
import org.hesperides.domain.security.UserEvent;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Stockage des events dans Redis.
 *
 * On utilise la structure du legacy:
 * Key: un aggregat
 * Values: les events pour cet aggregat sous forme de list.
 *
 * Le problème de ce système c'est qu'il nous manque un index, pour les tracking processors.
 *
 * Un tracking processor doit avoir un marque page pour savoir où est-ce qu'il s'est arrêter de lire les
 * events.
 *
 * Il faut donc avoir un index linéaire de tous les events. (il y a environ 20000 clés et 500000 events)
 *
 * On peut tenter en utilisant un HSET avec comme clé le timestamp de chaque event. Mais c'est trop lent à construire
 * Dans ce cas, le 'marque page' (token) est le timestamp
 *
 * On peut également utiliser une liste qui va stocker la liste des tous les events sequentiellement.
 * on ne va pas dupliquer les events: l'idée est de stocker dans cette liste le couple {aggregate_identifier:event_indice} qui identifie un event.
 *
 * Dans ce cas, le 'marque page' est l'indice dans la liste. on pourra utiliser un {@link org.axonframework.eventsourcing.eventstore.GlobalSequenceTrackingToken}
 *
 * Le problème qu'on va avoir avec cette solution, c'est qu'on n'est pas tolérant à la reprise: si on veut réindexer un event déjà indexé, la liste n'est pas
 * suffisante, il faut ajouter un HSET qui va permettre de gérer l'unicité des events.
 *
 * Dernier problème, lors de l'ajout de nouveau events par le legacy, on n'est pas notifié et notre index devient rapidement obsolète.
 * Solution: implementer un lister sur les clés et les listes d'events pour maintenir l'index à jour.
 */
@Slf4j
@Component
public class LegacyRedisStorageEngine extends AbstractEventStorageEngine {

    public static final String AGGREGATES_INDEX = "a_aggregates_index";

    private final StringRedisTemplate template;
    private final Codec codec;
    private final EventsIndexer eventsIndexer;
    private final EventsListener eventsListener;

    public LegacyRedisStorageEngine(StringRedisTemplate template, Codec codec) {
        super(null, null, null, null);
        this.template = template;
        this.codec = codec;

        this.eventsIndexer = new EventsIndexer(template);
        this.eventsListener = new EventsListener();
    }

    @PostConstruct
    public void init() {
        log.info("Starting legacy redis storage.");

        // check si on a l'index des events
        eventsIndexer.rebuildIfNecessary();

        // se met à l'écoute des nouveaux events.
        this.eventsListener.start();
    }

    @PreDestroy
    public void done() {
        this.eventsListener.stop();
    }

    @Override
    protected void appendEvents(List<? extends EventMessage<?>> events, Serializer serializer) {
        log.debug("append {} events to redis", events.size());

        template.execute(new SessionCallback<List<String>>() {
            @Override
            public List<String> execute(RedisOperations operations) throws DataAccessException {
                operations.multi();

                // ajouter les events sur les clés adhoc
                events.stream()
                        .filter(event -> event instanceof DomainEventMessage)
                        .forEach(event -> {
                            String aggregateIdentifier = ((DomainEventMessage) event).getAggregateIdentifier();
                            operations.opsForList().rightPush(aggregateIdentifier, codec.code((DomainEventMessage) event));
                            // met à jour l'index de tous les events
                            operations.opsForZSet().add(AGGREGATES_INDEX, aggregateIdentifier, 1);
                        });
                return operations.exec();
            }
        });
    }

    @Override
    protected void storeSnapshot(DomainEventMessage<?> snapshot, Serializer serializer) {
        // TODO: pour l'instant on ne gère pas les snapshots
        throw new IllegalArgumentException("pas géré pour l'instant");
    }

    @Override
    protected Optional<? extends DomainEventData<?>> readSnapshotData(String aggregateIdentifier) {
        // TODO: pour l'instant on ne gère pas les snapshots
        throw new IllegalArgumentException("pas géré pour l'instant");
    }

    @Override
    protected Stream<DomainEventData<? extends UserEvent>> readEventData(String identifier, long firstSequenceNumber) {
        List<String> range = template.opsForList().range(identifier, 0, -1);

        return codec.decodeAsTrackedDomainEventData(identifier, firstSequenceNumber, range)
                .map(trackedEventData -> (DomainEventData<? extends UserEvent>)trackedEventData);
    }

    @Override
    protected Stream<? extends TrackedEventData<?>> readEventData(TrackingToken trackingToken, boolean mayBlock) {

        int batchSize = 20;
        EventStreamSpliterator<? extends TrackedEventData<?>> spliterator = new EventStreamSpliterator<>(
                lastItem -> fetchTrackedEvents(lastItem == null ? trackingToken : lastItem.trackingToken(), batchSize),
                batchSize, true);
        return StreamSupport.stream(spliterator, false);
    }

    private List<? extends TrackedEventData<?>> fetchTrackedEvents(TrackingToken trackingToken, int batchSize) {

        Set<String> keys = template.opsForZSet().rangeByLex(AGGREGATES_INDEX, computeRangeFromTrackingToken(trackingToken), RedisZSetCommands.Limit.limit().count(batchSize));
        // ok maintenant pipeline les get:
        List<Object> events = template.executePipelined(new SessionCallback<List<String>>() {
            @Override
            public List<String> execute(RedisOperations operations) throws DataAccessException {
                keys.forEach(key -> operations.opsForList().range(key, 0, -1));
                return null;
            }
        });

        List<TrackedEventData<?>> result = new ArrayList<>();

        Iterator<String> kIt = keys.iterator();
        Iterator<Object> eIt = events.iterator();
        while (kIt.hasNext()) {
            String currentKey = kIt.next();
            List<String> currentEvents = (List<String>) eIt.next();

            try {
                Stream<TrackedEventData<?>> domainEventDataStream =
                        codec.decodeAsTrackedDomainEventData(currentKey, 0, currentEvents);
                List<TrackedEventData<?>> collect = domainEventDataStream.collect(Collectors.toList());
                result.addAll(collect);
            } catch (Exception e) {
                // en cas d'erreur, on laisse tomber la clé
                log.error("could not read an event of aggregate {}, error was:{}, skip this aggregate.",
                        currentKey, e.getMessage());
            }
        }

        return result;
    }

    private RedisZSetCommands.Range computeRangeFromTrackingToken(TrackingToken trackingToken) {

        if (trackingToken == null)  {
            // renvoi tout:
            return RedisZSetCommands.Range.unbounded();
        } else {
            Assert.isTrue(trackingToken instanceof RedisTrackingToken, () -> "bad token type");
            RedisTrackingToken token = (RedisTrackingToken) trackingToken;
            return RedisZSetCommands.Range.range().gt(token.identifier);
        }
    }


    private static class EventStreamSpliterator<T> extends Spliterators.AbstractSpliterator<T> {

        private final Function<T, List<? extends T>> fetchFunction;
        private final int batchSize;
        private final boolean fetchUntilEmpty;
        private Iterator<? extends T> iterator;
        private T lastItem;
        private int sizeOfLastBatch;

        private EventStreamSpliterator(Function<T, List<? extends T>> fetchFunction, int batchSize,
                                       boolean fetchUntilEmpty) {
            super(Long.MAX_VALUE, NONNULL | ORDERED | DISTINCT | CONCURRENT);
            this.fetchFunction = fetchFunction;
            this.batchSize = batchSize;
            this.fetchUntilEmpty = fetchUntilEmpty;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            Objects.requireNonNull(action);
            if (iterator == null || !iterator.hasNext()) {
                if (iterator != null && batchSize > sizeOfLastBatch && !fetchUntilEmpty) {
                    return false;
                }
                List<? extends T> items = fetchFunction.apply(lastItem);
                iterator = items.iterator();
                if ((sizeOfLastBatch = items.size()) == 0) {
                    return false;
                }
            }
            action.accept(lastItem = iterator.next());
            return true;
        }
    }

    public static class RedisTrackingToken implements TrackingToken {
        private final String identifier;

        public RedisTrackingToken(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public TrackingToken lowerBound(TrackingToken other) {
            Assert.isTrue(other instanceof RedisTrackingToken, () -> "bad token type");

            if (this.covers(other)) {
                return this;
            } else {
                return other;
            }
        }

        @Override
        public TrackingToken upperBound(TrackingToken other) {
            Assert.isTrue(other instanceof RedisTrackingToken, () -> "bad token type");
            if (this.covers(other)) {
                return other;
            } else {
                return this;
            }
        }

        @Override
        public boolean covers(TrackingToken other) {
            // ce token couvre l'autre si l'autre est > au sens lexicographique
            Assert.isTrue(other instanceof RedisTrackingToken, () -> "bad token type");
            return identifier.compareTo(((RedisTrackingToken) other).identifier) > 0;
        }
    }


    /**
     * écoute les nouveaux events. utilise le système de pub/sub de Redis
     *
     * TODO !!!
     */
    private class EventsListener {
        public void stop() {

        }

        public void start() {

        }
    }
}
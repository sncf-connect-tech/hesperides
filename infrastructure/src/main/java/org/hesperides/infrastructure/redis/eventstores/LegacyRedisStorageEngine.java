package org.hesperides.infrastructure.redis.eventstores;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.axonframework.eventsourcing.eventstore.*;
import org.axonframework.serialization.*;
import org.hesperides.infrastructure.redis.RedisConfiguration;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Stockage des events dans Redis.
 * <p>
 * On utilise la structure du legacy:
 * Key: un aggregat
 * Values: les events pour cet aggregat sous forme de list.
 * <p>
 * Le problème de ce système c'est qu'il nous manque un index, pour les tracking processors.
 * <p>
 * Un tracking processor doit avoir un marque page pour savoir où est-ce qu'il s'est arrêter de lire les
 * events.
 * <p>
 * Il faut donc avoir un index linéaire de tous les events. (il y a environ 20000 clés et 500000 events)
 * <p>
 * On peut tenter en utilisant un HSET avec comme clé le timestamp de chaque event. Mais c'est trop lent à construire
 * Dans ce cas, le 'marque page' (token) est le timestamp
 * <p>
 * On peut également utiliser une liste qui va stocker la liste des tous les events sequentiellement.
 * on ne va pas dupliquer les events: l'idée est de stocker dans cette liste le couple {aggregate_identifier:event_indice} qui identifie un event.
 * <p>
 * Dans ce cas, le 'marque page' est l'indice dans la liste. on pourra utiliser un {@link org.axonframework.eventsourcing.eventstore.GlobalSequenceTrackingToken}
 * <p>
 * Le problème qu'on va avoir avec cette solution, c'est qu'on n'est pas tolérant à la reprise: si on veut réindexer un event déjà indexé, la liste n'est pas
 * suffisante, il faut ajouter un HSET qui va permettre de gérer l'unicité des events.
 * <p>
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

    private final CounterService counterService;

    public LegacyRedisStorageEngine(StringRedisTemplate template, Codec codec, CounterService counterService) {
        super(new PleaseDoNothingSerializer(), null, null, new PleaseDoNothingSerializer());
        this.template = template;
        this.codec = codec;

        this.eventsIndexer = new EventsIndexer(template);
        this.counterService = counterService;
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

        // boucle sur chaque event:
        events.stream()
                .filter(event -> event instanceof DomainEventMessage)
                .map(event -> (DomainEventMessage) event)
                .forEach(event -> eventsIndexer.indexEvent(event.getAggregateIdentifier(), codec.code(event)));
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
    protected Stream<DomainEventData<?>> readEventData(String identifier, long firstSequenceNumber) {
        List<String> range = template.opsForList().range(identifier, 0, -1);

        List<DomainEventMessage<?>> eventMessages = codec.decode(identifier, firstSequenceNumber, range);

        List<DomainEventData<?>> result = new ArrayList<>();
        int i = 0;
        for (DomainEventMessage<?> domainEventMessage : eventMessages) {
            GenericDomainEventEntry<Object> entry = new GenericDomainEventEntry<>(domainEventMessage.getType(),
                    domainEventMessage.getAggregateIdentifier(),
                    domainEventMessage.getSequenceNumber(),
                    domainEventMessage.getAggregateIdentifier() + ":" + i++,
                    domainEventMessage.getTimestamp(),
                    domainEventMessage.getPayloadType().getName(),
                    "",
                    domainEventMessage.getPayload(),
                    domainEventMessage.getMetaData()
            );
            result.add(entry);
        }
        return result.stream();
    }

    @Override
    protected Stream<? extends TrackedEventData<?>> readEventData(TrackingToken trackingToken, boolean mayBlock) {

        log.debug("Need to read data from trackingToken {}", trackingToken);

        // tente une implémentation naive
//        GlobalSequenceTrackingToken start = trackingToken == null ? new GlobalSequenceTrackingToken(0) : ((GlobalSequenceTrackingToken) trackingToken).next();
//        int batchSize = 10;
//        List<? extends TrackedEventData<?>> trackedEventData = fetchTrackedEvents(start,
//                batchSize
//                //(int) (eventsIndexer.getEventsCount() - start.getGlobalIndex())
//        );
//        return trackedEventData.stream();

        // charge les events a fetcher:

        int batchSize = 50;

        EventStreamSpliterator<? extends TrackedEventData<?>> spliterator = new EventStreamSpliterator<>(
                lastItem -> fetchTrackedEvents(getStartToken(lastItem, trackingToken), batchSize));
        return StreamSupport.stream(spliterator, false);
    }

    private GlobalSequenceTrackingToken getStartToken(TrackedEventData<?> lastItem, TrackingToken trackingToken) {
        if (lastItem != null) {
            return ((GlobalSequenceTrackingToken) lastItem.trackingToken()).next();
        } else {
            if (trackingToken != null) {
                return ((GlobalSequenceTrackingToken) trackingToken).next();
            } else {
                return new GlobalSequenceTrackingToken(0);
            }
        }
    }

    private static class EventStreamSpliterator<T> extends Spliterators.AbstractSpliterator<T> {

        private final Function<T, List<? extends T>> fetchFunction;
        private Iterator<? extends T> iterator;
        private T lastItem;

        private EventStreamSpliterator(Function<T, List<? extends T>> fetchFunction) {
            super(Long.MAX_VALUE, NONNULL | ORDERED | DISTINCT | CONCURRENT);
            this.fetchFunction = fetchFunction;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            Objects.requireNonNull(action);
            if (iterator == null || !iterator.hasNext()) {
                List<? extends T> items = fetchFunction.apply(lastItem);
                iterator = items.iterator();
                if (items.isEmpty()) {
                    log.debug("End of stream !");
                    return false;
                }
            }
            T t = lastItem = iterator.next();
            action.accept(t);
            return true;
        }
    }

    private List<? extends TrackedEventData<?>> fetchTrackedEvents(TrackingToken trackingToken, int batchSize) {

        GlobalSequenceTrackingToken token = (GlobalSequenceTrackingToken) trackingToken;

        // récupère le point de départ:
        long start = token.getGlobalIndex();

        List<EventsIndexer.EventDescriptor> events = eventsIndexer.findEventsData(start, start + batchSize - 1);

        List<TrackedEventData<?>> result = new ArrayList<>();

        GlobalSequenceTrackingToken current = token;

        for (EventsIndexer.EventDescriptor event : events) {
            counterService.increment(getClass().getSimpleName() + ".fetchEvents.process");
            try {
                TrackedEventData<?> eventData =
                        codec.decodeEventAsTrackedDomainEventData(
                                event.getAggregateId(),
                                0, event.getEventData(), current
                        );

                current = current.next();

                result.add(eventData);
                counterService.increment(getClass().getSimpleName() + ".fetchEvents.success");

            } catch (Exception e) {
                // compte les erreurs.
                counterService.increment(getClass().getSimpleName() + ".fetchEvents.errors." + e.getClass().getSimpleName());
//                // en cas d'erreur, on laisse tomber la clé
//                log.error("could not read an event of aggregate {}, error was:{}, skip this event.",
//                        event.getAggregateId(), e.getMessage());
            }
        }

        // vérifie qu'on a bien au moins un element, sinon on ne peut plus avancer.
        // sauf, evidement si on est à la fin du stream.
        if (result.isEmpty() && start < eventsIndexer.getEventsCount()) {
            // ok on doit rejouer, en avançant le pointeur de la taille du batch
            return fetchTrackedEvents(new GlobalSequenceTrackingToken(start + batchSize), batchSize);
        }
        return result;
    }

    private static class PleaseDoNothingSerializer implements Serializer {

        @Override
        public <T> SerializedObject<T> serialize(Object object, Class<T> expectedRepresentation) {
            return new SimpleSerializedObject(object, object.getClass(), typeForClass(object.getClass()));
        }

        @Override
        public <T> boolean canSerializeTo(Class<T> expectedRepresentation) {
            return true; // je peux tout faire !
        }

        @Override
        public <S, T> T deserialize(SerializedObject<S> serializedObject) {
            return (T) serializedObject.getData(); // identity deserialize
        }

        @Override
        public Class classForType(SerializedType type) throws UnknownSerializedTypeException {
            try {
                return  Thread.currentThread().getContextClassLoader().loadClass(type.getName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public SerializedType typeForClass(Class type) {
            return new SimpleSerializedType(type.getName(), "");
        }

        @Override
        public Converter getConverter() {
            return null;
        }
    }

    /**
     * écoute les nouveaux events. utilise le système de pub/sub de Redis
     * <p>
     * TODO !!!
     */
    private class EventsListener {
        public void stop() {

        }

        public void start() {

        }
    }
}
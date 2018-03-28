package org.hesperides.infrastructure.redis.eventstores;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.common.Assert;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventsourcing.DomainEventMessage;
import org.axonframework.eventsourcing.eventstore.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Long.parseLong;

@Slf4j
@Component
public class RedisStorageEngine implements EventStorageEngine {

    public static final String EVENTS_INDEX = "a_events_index";
    public static final String REBUILDING_INDEX_PROCESS = "a_already_indexed";
    private static final JsonParser jsonParser = new JsonParser();
    private static final DefaultRedisScript<String> INDEX_EVENTS_SCRIPT = new DefaultRedisScript<>();

    static {
        INDEX_EVENTS_SCRIPT.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/index_events.lua")));
        INDEX_EVENTS_SCRIPT.setResultType(String.class);
    }

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

                // ajouter les events sur les clés adhoc
                events.stream()
                        .filter(event -> event instanceof DomainEventMessage)
                        .forEach(event -> {
                            String aggregateIdentifier = ((DomainEventMessage) event).getAggregateIdentifier();
                            long index = operations.opsForList().rightPush(aggregateIdentifier, codec.code((DomainEventMessage) event));
                            // met à jour l'index de tous les events
                            operations.opsForList().rightPush(EVENTS_INDEX, new EventIndex(event.getTimestamp(), (DomainEventMessage<?>) event, index));
                        });
                return operations.exec();
            }
        });
    }

    @Override
    public void storeSnapshot(DomainEventMessage<?> snapshot) {
        log.debug("storing snapshot (todo)");
        throw new IllegalArgumentException("pas implémenté pour l'instant");
    }


    private class EventReaderStats {

        private final AtomicInteger readCount = new AtomicInteger();
        private final AtomicInteger readOkCount = new AtomicInteger();
        private final AtomicInteger readKoCount = new AtomicInteger();
        private final Map<String, AtomicInteger> exceptionsKind = new HashMap<>();
        private final StopWatch watch;
        private final ScheduledExecutorService monitoring;

        public EventReaderStats() {
            watch = new StopWatch();
            monitoring = Executors.newSingleThreadScheduledExecutor();
        }

        public void statsReadExceptions(Exception e) {
            readKoCount.incrementAndGet();
            exceptionsKind.getOrDefault(e.getClass().getName(), new AtomicInteger()).incrementAndGet();
        }

        public void eventReadSuccessfully() {
            readOkCount.incrementAndGet();
        }

        void report() {
            watch.stop();
            monitoring.shutdownNow();
            log.info("Read report:\n Read in {} ms\n -> Success: {}\n -> Failed: {}\nErrors:\n{}", watch.getTotalTimeMillis(), readOkCount.get(), readKoCount.get(), new Gson().toJson(exceptionsKind));
        }

        public void startReadingEventsIndex() {
            watch.start("Reading all events index");
        }

        public void stopReadingEventsIndex() {
            watch.stop();
        }

        public void startReadingAllEvents(int allEventsCount) {
            watch.start("Reading all events");
            monitoring.scheduleAtFixedRate(() -> log.info("Reading: {} %", (readCount.get() * 100) / allEventsCount), 1,1,TimeUnit.SECONDS);
            log.info("Reading all events: {}", allEventsCount);
        }

        public void readingEvent() {
            readCount.incrementAndGet();
        }
    }

    @Override
    public Stream<? extends TrackedEventMessage<?>> readEvents(TrackingToken trackingToken, boolean mayBlock) {

        log.debug("Read events. (tracked): {},{}", trackingToken, mayBlock);

        if (trackingToken == null) {

            log.debug("full scan of event store was required !");

            rebuildEventsIndex();

            log.debug("Event index is consistent, Streaming all events.");

            // faut lire tous les évents en séquence !!!!!
            // on utilise le timestamp comme global sequence tracking token !
            EventReaderStats stats = new EventReaderStats();

            stats.startReadingEventsIndex();
            List<TrackedEventMessage<?>> trackedEventMessages = new LinkedList<>();
            Set<String> allEvents = template.opsForZSet().range(EVENTS_INDEX, 0, -1);
            stats.stopReadingEventsIndex();

            // permet de cacher la liste des events, et d'éviter un aller/retour sur le serveur.
            LoadingCache<String, List<String>> eventsByKeyCache = CacheBuilder.newBuilder().build(new CacheLoader<String, List<String>>() {
                @Override
                public List<String> load(String key) throws Exception {
                    return template.opsForList().range(key, 0, -1);
                }
            });
            stats.startReadingAllEvents(allEvents.size());

            for (String event : allEvents) {
                //
                stats.readingEvent();
                EventIndex eventIndex = fromIndexValue(event);
                // recherche l'event.
                try {
                    TrackedEventMessage<?> trackedEventMessage = eventIndex.lookupEvent(eventsByKeyCache);
                    trackedEventMessages.add(trackedEventMessage);
                    stats.eventReadSuccessfully();
                } catch (Exception e) {
                    // fait des stats sur les erreurs de lecture
                    stats.statsReadExceptions(e);
                }
            }

            stats.report();

            return trackedEventMessages.stream();

        } else {

            // todo: coder quand on a un tracking token...
        }

        return null;
    }

    private void rebuildEventsIndex() {

        // todo: pause les clients avant de lancer ce travail (commande redis PAUSE)

        StopWatch watch = new StopWatch();

        watch.start("retrieve all keys ");
        List<String> keys = template.keys("*").stream().filter(this::isAEventSourcedKey).collect(Collectors.toList());
        watch.stop();

        // stocke les clés qu'on a déjà traité dans un hash, en effet, le traitement peut planter,
        // on veut le reprendre sans retraiter toutes les clés.
        watch.start("Checking existence of previous run");
        Long size = template.opsForHash().size(REBUILDING_INDEX_PROCESS);
        log.info("Already processed key count: {}", size);

        watch.stop();

        if (size == keys.size()) {
            log.info("No need to rebuild index, it seems ok as already processed key count is equal to total key count.");
            return;
        }

        // desactivation du système de persistence, parce qu'on va ajouter beaucoup de clé !!
        watch.start("Saving redis current 'save' configuration");
        List<String> saveList = template.getConnectionFactory().getConnection().getConfig("save");
        String currentSave = "";
        if (!saveList.isEmpty()) {
            currentSave = saveList.get(1);
        }
        log.info("Saving 'save' configuration : {}", currentSave);
        watch.stop();

        // desactive rdb
        watch.start("Disable redis 'save' processing");
        template.getConnectionFactory().getConnection().setConfig("save", "");
        watch.stop();

        watch.start("index keys");
        Indexer indexer = new MultiThreadedIndexer();
        indexer.process(keys);
        watch.stop();

//        watch.start("clear useless keys");
//        // supprime la clé pour le process si tout c'est bien passé.
//        if (indexer.hasNoKo()) {
//            template.delete(REBUILDING_INDEX_PROCESS);
//        }
//        watch.stop();

        // restore rdb
        template.getConnectionFactory().getConnection().setConfig("save", currentSave);

        log.info("{}", watch.prettyPrint());
        log.info("done. total time: {} ms", watch.getTotalTimeMillis());

        // todo relance les clients.
    }

    private boolean isAEventSourcedKey(Object key) {
        return key.toString().startsWith("module-") || key.toString().startsWith("platform-") || key.toString().startsWith("template_package-");
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

    /**
     * un truc qui index
     */
    private abstract class Indexer {

        AtomicInteger keyCount = new AtomicInteger();
        AtomicInteger okCount = new AtomicInteger();
        AtomicInteger koCount = new AtomicInteger();
        AtomicInteger noopCount = new AtomicInteger();

        void indexEvents(AtomicInteger keyCount, AtomicInteger okCount, AtomicInteger koCount, AtomicInteger noopCount, List<String> batch) {
            for (String key : batch) {
                keyCount.incrementAndGet();
                String result = template.execute(INDEX_EVENTS_SCRIPT, Collections.singletonList(key));
                switch (result) {
                    case "ok":
                        okCount.incrementAndGet();
                        break;
                    case "noop":
                        noopCount.incrementAndGet();
                        break;
                    default:
                        koCount.incrementAndGet();
                }
            }
        }

        abstract void process(List<String> keys);

        public boolean hasNoKo() {
            return koCount.get() == 0;
        }
    }

    private EventIndex fromIndexValue(String indexValue) {
        String[] split = indexValue.split("[:]");
        return new EventIndex(parseLong(split[0]), split[1], parseLong(split[2]));
    }

    private class EventIndex implements Comparable<EventIndex> {
        private final long timestamp;
        private final String aggregateIdentifier;
        private final long eventIndex;

        private EventIndex(long timestamp, String aggregateIdentifier, long eventIndex) {
            this.timestamp = timestamp;
            this.aggregateIdentifier = aggregateIdentifier;
            this.eventIndex = eventIndex;
        }

        EventIndex(Instant timestamp, DomainEventMessage<?> event, long index) {
            // todo check le pb des epochs je suis pas sur de moi la dessus.
            this(timestamp.toEpochMilli(), event.getAggregateIdentifier(), index);
        }

        String toIndexValue() {
            return timestamp + ":" + aggregateIdentifier + ":" + eventIndex;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            EventIndex that = (EventIndex) o;

            return timestamp == that.timestamp;
        }

        @Override
        public int hashCode() {
            return (int) (timestamp ^ (timestamp >>> 32));
        }

        @Override
        public int compareTo(@NotNull EventIndex o) {
            return Long.compare(this.timestamp, o.timestamp);
        }

        /**
         * recherche l'event dans l'event store.
         * @return un optional, il est possible qu'on arrive pas a trouver l'évent, ou bien
         * qu'il y ait un autre problème (genre impossible de décoder un event).
         * @param eventsByKeyCache un cache pour accelérer un peu le traitement.
         */
        public TrackedEventMessage lookupEvent(LoadingCache<String, List<String>> eventsByKeyCache) {
            String data = eventsByKeyCache.getUnchecked(aggregateIdentifier).get((int) eventIndex);
            DomainEventMessage<?> eventMessage = codec.decode(aggregateIdentifier, eventIndex, data);
            return EventUtils.asTrackedEventMessage(eventMessage, new GlobalSequenceTrackingToken(timestamp));
        }
    }

    class MultiThreadedIndexer extends Indexer {

        @Override
        public void process(List<String> keys) {

            int processorsCount = 10;
            List<List<String>> batches = Lists.partition(keys, keys.size() / processorsCount);

            ExecutorService threadPool = Executors.newFixedThreadPool(batches.size() + 1);

            List<Callable<Object>> callables = batches.stream().map(batch -> Executors.callable(() -> indexEvents(keyCount, okCount, koCount, noopCount, batch))).collect(Collectors.toList());

            // thread de contrôle.
            ScheduledExecutorService controlService = Executors.newSingleThreadScheduledExecutor();
            controlService.scheduleAtFixedRate(() -> log.info("current key count {}, ok={}, ko={}, noop={}",
                    keyCount.get(), okCount.get(), koCount.get(), noopCount.get()), 1, 1, TimeUnit.SECONDS);

            try {
                threadPool.invokeAll(callables);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
            controlService.shutdownNow();
        }
    }

    class SingleThreadedIndexer extends Indexer {

        @Override
        public void process(List<String> keys) {
            keys.stream()
                    .peek(s -> {
                        if (this.keyCount.get() % 100 == 0)
                            log.info("current key count {}, ok={}, ko={}, noop={}",
                                    keyCount.get(), okCount.get(), koCount.get(), noopCount.get()
                            );
                    })
                    .forEach(key -> indexEvents(keyCount, okCount, koCount, noopCount, Collections.singletonList(key)));
        }
    }
}
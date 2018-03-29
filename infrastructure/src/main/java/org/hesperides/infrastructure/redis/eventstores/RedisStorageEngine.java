package org.hesperides.infrastructure.redis.eventstores;

import com.google.common.collect.Lists;
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
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@Component
public class RedisStorageEngine extends AbstractEventStorageEngine {

    public static final String AGGREGATES_INDEX = "a_aggregates_index";

    private final StringRedisTemplate template;
    private final Codec codec;

    public RedisStorageEngine(StringRedisTemplate template, Codec codec) {
        super(null, null, null, null);
        this.template = template;
        this.codec = codec;
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

        if (trackingToken == null) {

            // réindex (si nécessaire)
            log.debug("full scan of event store was required !");

            rebuildAggregateIndex();

            log.debug("Event index is consistent, Streaming all events.");
        }

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

    private void rebuildAggregateIndex() {

        StopWatch watch = new StopWatch();

        List<String> keys = retrieveAllKeys(watch);

        // desactivation du système de persistence, parce qu'on va ajouter beaucoup de clé !!
        String currentSave = getCurrentSaveConfiguration(watch);

        // desactive rdb
        disableRdb(watch);

        watch.start("indexing aggregates.");
        MultiThreadedIndexer indexer = new MultiThreadedIndexer();
        indexer.process(keys);
        watch.stop();

        // restore rdb
        restoreSaveConfiguration(currentSave);

        log.info("{}", watch.prettyPrint());
        log.info("Processed: keys: {}, noop: {}, ok: {}", indexer.keyCount, indexer.noopCount, indexer.okCount);
        log.info("done. total time: {} ms", watch.getTotalTimeMillis());
    }

    private List<String> retrieveAllKeys(StopWatch watch) {
        watch.start("retrieve all keys ");
        List<String> keys = template.keys("*").stream().filter(this::isAEventSourcedKey).collect(Collectors.toList());
        watch.stop();
        return keys;
    }

    private void restoreSaveConfiguration(String currentSave) {
        template.getConnectionFactory().getConnection().setConfig("save", currentSave);
    }

    private void disableRdb(StopWatch watch) {
        watch.start("Disable redis 'save' processing");
        template.getConnectionFactory().getConnection().setConfig("save", "");
        watch.stop();
    }

    private String getCurrentSaveConfiguration(StopWatch watch) {
        watch.start("Saving redis current 'save' configuration");
        List<String> saveList = template.getConnectionFactory().getConnection().getConfig("save");
        String currentSave = "";
        if (!saveList.isEmpty()) {
            currentSave = saveList.get(1);
        }
        log.info("Saving 'save' configuration : {}", currentSave);
        watch.stop();
        return currentSave;
    }

    private boolean isAEventSourcedKey(Object key) {
        return key.toString().startsWith("module-") || key.toString().startsWith("platform-") || key.toString().startsWith("template_package-");
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

    class MultiThreadedIndexer {

        AtomicInteger keyCount = new AtomicInteger();
        AtomicInteger okCount = new AtomicInteger();
        AtomicInteger koCount = new AtomicInteger();
        AtomicInteger noopCount = new AtomicInteger();

        public void process(List<String> keys) {

            int processorsCount = 10;
            List<List<String>> batches = Lists.partition(keys, keys.size() / processorsCount);

            ExecutorService threadPool = Executors.newFixedThreadPool(batches.size() + 1);

            List<Callable<Object>> callables = batches.stream().map(batch -> Executors.callable(() -> doWithBatchOfKeys(batch))).collect(Collectors.toList());

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

        void doWithBatchOfKeys(List<String> batch) {
            List<Object> results = template.executePipelined(new SessionCallback<Void>() {
                @Override
                public Void execute(RedisOperations operations) throws DataAccessException {
                    for (String key : batch) {
                        keyCount.incrementAndGet();
                        template.opsForZSet().add(AGGREGATES_INDEX, key, 1);
                    }
                    return null;
                }
            });
            results.forEach(result -> {
                if (result instanceof Boolean) {
                    if ((Boolean) result) {
                        okCount.incrementAndGet();
                    } else {
                        noopCount.incrementAndGet();
                    }
                }
            });
        }
    }
}
/*
 *
 *  * This file is part of the Hesperides distribution.
 *  * (https://github.com/voyages-sncf-technologies/hesperides)
 *  * Copyright (c) 2016 VSCT.
 *  *
 *  * Hesperides is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as
 *  * published by the Free Software Foundation, version 3.
 *  *
 *  * Hesperides is distributed in the hope that it will be useful, but
 *  * WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  * General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package com.vsct.dt.hesperides.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsct.dt.hesperides.util.ManageableJedisConnectionInterface;
import io.dropwizard.jackson.Jackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by william_montaz on 03/11/2014.
 */
public final class RedisEventStore<A extends JedisCommands&MultiKeyCommands&AdvancedJedisCommands&ScriptingCommands&BasicCommands&ClusterCommands&Closeable> implements EventStore {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisEventStore.class);

    /**
     * Builder of event from JSON.
     */
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    /**
     * Pool.
     */
    private final Pool<A> dataPool;

    /**
     * Snapshot pool.
     */
    private final Pool<A> snapshotPool;

    /**
     * Number of retry.
     */
    private final int nRetry;

    /**
     * Wait time before retry.
     */
    private final int waitBeforeRetryMs;

    /**
     * Number of events to be loaded at once when replaying
     * It is also the events list pagination page size.
     */
    private int BATCH_SIZE = 100;

    public RedisEventStore(final ManageableJedisConnectionInterface<A> dataPool,
                           final ManageableJedisConnectionInterface<A> snapshotPool) {
        this.dataPool = dataPool.getPool();
        this.snapshotPool = snapshotPool.getPool();
        this.nRetry = dataPool.getnRetry();
        this.waitBeforeRetryMs = dataPool.getWaitBeforeRetryMs();
    }

    private <T> T execute(Pool<A> redisPool, final RedisCommand<T, A> command) {
        int attempt = 1;

        for (; ; ) {

            try (A jedis = redisPool.getResource()) {
                return command.execute(jedis);
            } catch (final JedisException e) {
                if (attempt <= nRetry) {
                    LOGGER.warn("JEDIS CONNECTION - ATTEMPT {} ON {}", attempt, nRetry);
                    try {
                        Thread.sleep(waitBeforeRetryMs);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                    attempt++;
                    continue;
                }
                else {
                    LOGGER.error("JEDIS CONNECTION FAILED AFTER {} ATTEMPTS", nRetry);
                    throw e;
                }
            } catch (Throwable e) {
                command.error(e);

                return null;
            }
        }
    }

    @Override
    public <T> T store(final String streamName, final T event, final UserInfo userInfo,
                       final EventStoreCallback callback) {

        return execute(dataPool, new RedisCommand<T, A>() {
            @Override
            public T execute(final A jedis) throws Throwable {
                Event eventStoreEvent = new Event(event.getClass().getCanonicalName(),
                        MAPPER.writeValueAsString(event), System.currentTimeMillis(), userInfo.getUsername());

                jedis.rpush(streamName, MAPPER.writeValueAsString(eventStoreEvent));

                LOGGER.debug("stored event {}", event);

                callback.complete();

                return event;
            }

            @Override
            public void error(final Throwable e) {
                if (e instanceof JsonProcessingException) {
                    LOGGER.error("Could not serialize the event to string");
                }

                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public HesperidesSnapshotItem findSnapshot(final String streamName, final long offset, final EventTester<Event> ev) {
        return execute(snapshotPool, new RedisCommand<HesperidesSnapshotItem, A>() {
            @Override
            public HesperidesSnapshotItem execute(final A jedis) throws Throwable {
                final String redisKey = String.format("snapshotevents-%s", streamName);

                if (jedis.exists(streamName) && jedis.exists(redisKey)) {
                    final long lastIndex = jedis.llen(streamName) - 1;

                    long currentEventIndex;
                    long selectedCacheIndex = 0;

                    // Check event that jump to ev.increment
                    for (currentEventIndex = 0; currentEventIndex <= lastIndex; currentEventIndex += offset) {
                        final List<String> binaryEvents = jedis.lrange(streamName, currentEventIndex, currentEventIndex);

                        final Event event = MAPPER.readValue(binaryEvents.get(0), Event.class);

                        if (ev.test(event)) {
                            break;
                        }
                    }

                    // E.g. we stop to event #8000
                    // Cache index to event #8000 is #79
                    // But #8000 is rejected, we must reject cache #79 an get cache #78
                    selectedCacheIndex = ((currentEventIndex / offset) - 1) - 1;

                    final List<String> lastCache = jedis.lrange(redisKey, selectedCacheIndex, selectedCacheIndex);

                    final HesperidesSnapshotCacheEntry cache
                            = MAPPER.readValue(lastCache.get(0), HesperidesSnapshotCacheEntry.class);

                    Object object = MAPPER.readValue(cache.getData(), Class.forName(cache.getCacheType()));

                    return new HesperidesSnapshotItem(object, cache.getNbEvents(), currentEventIndex);
                }

                return null;
            }

            @Override
            public void error(final Throwable e) {
                if (e instanceof ClassNotFoundException || e instanceof IOException) {
                    LOGGER.error("Could not deserialize the snapshot '{}' cache.", streamName);
                    // Don't bock cause Hesperides could be work
                    LOGGER.error("Stacktrace : ", e);
                } else {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public HesperidesSnapshotItem findLastSnapshot(final String streamName) {
        return execute(snapshotPool, new RedisCommand<HesperidesSnapshotItem, A>() {
            @Override
            public HesperidesSnapshotItem execute(final A jedis) throws Throwable {
                final String redisKey = String.format("snapshotevents-%s", streamName);

                if (jedis.exists(redisKey)) {
                    final long lastIndex = jedis.llen(redisKey) - 1;
                    final List<String> lastCache = jedis.lrange(redisKey, lastIndex, lastIndex);

                    final HesperidesSnapshotCacheEntry cache
                            = MAPPER.readValue(lastCache.get(0), HesperidesSnapshotCacheEntry.class);

                    Object object = MAPPER.readValue(cache.getData(), Class.forName(cache.getCacheType()));

                    return new HesperidesSnapshotItem(object, cache.getNbEvents(), jedis.llen(streamName));
                }

                return null;
            }

            @Override
            public void error(final Throwable e) {
                if (e instanceof ClassNotFoundException || e instanceof IOException) {
                    LOGGER.error("Could not deserialize the snapshot '{}' cache.", streamName);
                    // Don't bock cause Hesperides could be work
                    LOGGER.error("Stacktrace : ", e);
                } else {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public <T> void storeSnapshot(final String streamName, final T object, final long offset) {
        execute(snapshotPool, new RedisCommand<Void, A>() {
            @Override
            public Void execute(final A jedis) throws Throwable {
                final String redisKey = String.format("snapshotevents-%s", streamName);

                final long currentNbEvent = jedis.llen(streamName);
                HesperidesSnapshotCacheEntry hsce;

                if (offset == 0 || currentNbEvent % offset == 0) {
                    LOGGER.debug("Store new snapshot for key {}.", redisKey);
                    hsce = new HesperidesSnapshotCacheEntry(
                            object.getClass().getCanonicalName(),
                            MAPPER.writeValueAsString(object),
                            currentNbEvent);

                    jedis.rpush(redisKey, MAPPER.writeValueAsString(hsce));
                }

                return null;
            }

            @Override
            public void error(final Throwable e) {
                LOGGER.error("Could not serialize the snapshot '{}' cache to string.", streamName);
                // Don't bock cause Hesperides could be work
                e.printStackTrace();
            }
        });
    }

    @Override
    public <T> void createSnapshot(final String streamName, final T object, final long nbEvent) {
        execute(snapshotPool, new RedisCommand<Void, A>() {
            @Override
            public Void execute(final A jedis) throws Throwable {
                final String redisKey = String.format("snapshotevents-%s", streamName);

                LOGGER.debug("Store new snapshot for key {}.", redisKey);
                HesperidesSnapshotCacheEntry hsce = new HesperidesSnapshotCacheEntry(
                            object.getClass().getCanonicalName(),
                            MAPPER.writeValueAsString(object),
                        nbEvent);

                jedis.rpush(redisKey, MAPPER.writeValueAsString(hsce));

                return null;
            }

            @Override
            public void error(final Throwable e) {
                LOGGER.error("Could not serialize the snapshot '{}' cache to string.", streamName);
                // Don't bock cause Hesperides could be work
                e.printStackTrace();
            }
        });
    }

    @Override
    public void withEvents(final String streamName, final long stopTimestamp,
                           final Consumer<Object> eventConsumer) throws StoreReadingException {
        long len;

        try (A jedis = dataPool.getResource()) {

            len = jedis.llen(streamName);

            withEvents(streamName, 0, len, stopTimestamp, eventConsumer);
        } catch (StoreReadingException | IOException e) {
            e.printStackTrace();
            throw new StoreReadingException(e);
        }
    }

    @Override
    public void withEvents(final String streamName, final long start, final long stop, final long stopTimestamp,
                           final Consumer<Object> eventConsumer) throws StoreReadingException {
        try (A jedis = dataPool.getResource()) {
            LOGGER.debug("{} events to restore for stream {}", stop - start, streamName);

            final long startTime = System.nanoTime();

            int indexEvent;
            long indexBatch;
            int counter = 0;
            long startIO;
            long stopIO;

            long ioAccumulator = 0, serializationAccumulator = 0, processingAccumulator = 0;

            for (indexBatch = start; indexBatch < stop; indexBatch = indexBatch + BATCH_SIZE) {

                startIO = System.nanoTime();

                List<String> events = jedis.lrange(streamName, indexBatch,
                        indexBatch + BATCH_SIZE - 1);

                if (LOGGER.isDebugEnabled()) {
                    stopIO = System.nanoTime();

                    ioAccumulator += stopIO - startIO;
                }

                for (indexEvent = 0; indexEvent < events.size(); indexEvent++) {

                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Processing event {}", indexBatch + indexEvent);
                    }

                    long startSerialization = System.nanoTime();

                    Event event = MAPPER.readValue(events.get(indexEvent), Event.class);

                    if (event.getTimestamp() > stopTimestamp) {
                        //No need to go beyong this point in time
                        indexBatch = stop;
                        break;
                    }

                    Object hesperidesEvent = MAPPER.readValue(event.getData(), Class.forName(event.getEventType()));
                    long stopSerialization = System.nanoTime();

                    serializationAccumulator += stopSerialization - startSerialization;

                    long startProcessing = System.nanoTime();
                    eventConsumer.accept(hesperidesEvent);
                    long stopProcessing = System.nanoTime();

                    processingAccumulator += stopProcessing - startProcessing;

                    counter++;
                }
            }

            if (LOGGER.isDebugEnabled()) {
                final long stopTime = System.nanoTime();

                long durationMs = (stopTime - startTime) / 1000000;

                double frequency = ((double) counter / durationMs) * 1000;

                LOGGER.debug("Stream {} complete ({} events processed - duration {} ms - {} msg/sec - {} ms IO -"
                        + "{} ms Serialization - {} ms processing)", streamName, counter, durationMs, frequency,
                        ioAccumulator / 1000000, serializationAccumulator / 1000000, processingAccumulator / 1000000);
            }
        } catch (StoreReadingException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
            throw new StoreReadingException(e);
        }
    }

    @Override
    public void withEvents(final String streamName, final long start, final long stop,
                           final Consumer<Object> eventConsumer) throws StoreReadingException {
        try (A jedis = dataPool.getResource()) {

            LOGGER.debug("{} events to restore for stream {}", stop - start, streamName);

            final List<String> events = jedis.lrange(streamName, start, stop);

            for (int indexEvent = 0; indexEvent < events.size(); indexEvent++) {

                LOGGER.trace("Processing event {}", indexEvent);

                Event event = MAPPER.readValue(events.get(indexEvent), Event.class);

                Object hesperidesEvent = MAPPER.readValue(event.getData(), Class.forName(event.getEventType()));

                eventConsumer.accept(hesperidesEvent);
            }

            LOGGER.debug("Stream {} complete ({} events processed)", streamName, stop - start);

        } catch (StoreReadingException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
            throw new StoreReadingException(e);
        }
    }

    @Override
    public Set<String> getStreamsLike(final String term) {
        try (A jedis = dataPool.getResource()) {
            return jedis.keys(term);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public List<Event> getEventsList(final String streamName, final int page, final int size)
            throws StoreReadingException {

        try (A jedis = dataPool.getResource()){

            LOGGER.debug("Start Retrieving {} events for {}", size, streamName);

            // size of entry for  streamName
            Long len = jedis.llen(streamName);

            //
            // This is a little calculation to get redis events from oldest to newest
            // This was required because of troubles on sorting events with pagination.
            //

            // Calculating from where we should start retrieving
                // Note : The default page number is 1 and the default pagination size is 25
            long from =  len - ( (page > 0 ? page : 1 ) * (size > 0 ? size : 25));
            from = (from > 0) ? from : 0;

            // Calculating til where we should retrieve. The total retrieved items should be equal to the size !
            long to = -1 * (((page -1) * size) + 1);

            // Querying redis
            List<String> binaryEvents = jedis.lrange(streamName, from, to);

            // Converting items from redis to Event objects
            List<Event> events = new ArrayList<>();

            for (int index = 0; index < binaryEvents.size(); index ++){
                events.add(MAPPER.readValue(binaryEvents.get(index), Event.class));
            }

            LOGGER.debug("End retrieving events from {} to {}. Size of events retrieved {} events for {}.",
                    from, to, events.size(), streamName);

            return events;

        } catch (StoreReadingException | IOException e) {
            LOGGER.debug("Exception {} occurred when getting the list of events for {}. Stacktrace : {}",
                    e.getClass().getCanonicalName(), streamName, e.getStackTrace());
            throw new StoreReadingException(e);
        }
    }

    @Override
    public void clearCache(final String streamName) {
        execute(snapshotPool, new RedisCommand<Void, A>() {
            @Override
            public Void execute(final A jedis) throws Throwable {
                final String redisKey = String.format("snapshotevents-%s", streamName);

                LOGGER.debug("Clear snapshot snapshot {}.", redisKey);

                jedis.del(redisKey);

                return null;
            }

            @Override
            public void error(final Throwable e) {
                LOGGER.error("Could not serialize the snapshot '{}' cache to string.", streamName);
                // Don't bock cause Hesperides could be work
                e.printStackTrace();
            }
        });
    }

    public boolean isConnected() {
        try (A jedis = dataPool.getResource()) {
            jedis.ping();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}

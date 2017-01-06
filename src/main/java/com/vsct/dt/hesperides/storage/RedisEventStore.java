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
import io.dropwizard.jackson.Jackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by william_montaz on 03/11/2014.
 */
public final class RedisEventStore implements EventStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisEventStore.class);

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    private final Pool<Jedis> connectionPool;
    private final int nRetry;
    private final int waitBeforeRetryMs;
    /**
     * Number of events to be loaded at once when replaying
     * It is also the events list pagination page size.
     */
    private int BATCH_SIZE = 100;

    public RedisEventStore(final Pool<Jedis> connectionPool, int nRetry, int waitBeforeRetryMs) {
        this.connectionPool = connectionPool;
        this.nRetry = nRetry;
        this.waitBeforeRetryMs = waitBeforeRetryMs;
    }

    @Override
    public <T> T store(final String streamName, final T event, final UserInfo userInfo) {
        int attempt = 1;

        for (; ; ) {

            try (Jedis jedis = connectionPool.getResource()) {

                Event eventStoreEvent = new Event(event.getClass().getCanonicalName(), MAPPER.writeValueAsString(event), System.currentTimeMillis(), userInfo.getUsername());

                jedis.rpush(streamName, MAPPER.writeValueAsString(eventStoreEvent));

                LOGGER.debug("stored event {}", event);

                return event;

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
            } catch (JsonProcessingException e) {
                LOGGER.error("Could not serialize the event to string");
                //Make runtime exception because there is nothing we can do
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void withEvents(final String streamName, final long stopTimestamp, final Consumer<Object> eventConsumer) throws StoreReadingException {
        try (Jedis jedis = connectionPool.getResource()) {

            Long len = jedis.llen(streamName);

            LOGGER.debug("{} events to restore for stream {}", len, streamName);

            long start = System.nanoTime();

            int i = 0, j = 0, counter = 0;
            long ioAccumulator = 0, serializationAccumulator = 0, processingAccumulator = 0;
            for (j = 0; j < len; j = j + BATCH_SIZE) {

                long startIO = System.nanoTime();
                List<byte[]> events = jedis.lrange(streamName.getBytes(StandardCharsets.UTF_8), j, j + BATCH_SIZE - 1);
                long stopIO = System.nanoTime();

                ioAccumulator += stopIO - startIO;

                for (i = 0; i < events.size(); i++) {

                    LOGGER.trace("Processing event {}", j + i);

                    long startSerialization = System.nanoTime();
                    //String stringEvent = jedis.lindex(streamName, i);
                    Event event = MAPPER.readValue(events.get(i), Event.class);

                    if (event.getTimestamp() > stopTimestamp) {
                        //No need to go beyong this point in time
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
            long stop = System.nanoTime();

            long durationMs = (stop - start) / 1000000;

            double frequency = ((double) counter / durationMs) * 1000;

            LOGGER.debug("Stream {} complete ({} events processed - duration {} ms - {} msg/sec - {} ms IO - {} ms Serialization - {} ms processing)", streamName, counter, durationMs, frequency, ioAccumulator / 1000000, serializationAccumulator / 1000000, processingAccumulator / 1000000);

        } catch (StoreReadingException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
            throw new StoreReadingException(e);
        }
    }

    @Override
    public Set<String> getStreamsLike(final String term) {
        try (Jedis jedis = connectionPool.getResource()) {
            return jedis.keys(term);
        }
    }

    @Override
    public List<Event> getEventsList (final String streamName, final int page, final int size) throws StoreReadingException{

        try (Jedis jedis = connectionPool.getResource()){

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
            List<byte[]> binaryEvents = jedis.lrange(streamName.getBytes(StandardCharsets.UTF_8), from, to);

            // Converting items from redis to Event objects
            List<Event> events = new ArrayList<>();
            for (int index = 0; index < binaryEvents.size(); index ++){
                events.add(MAPPER.readValue(binaryEvents.get(index), Event.class));
            }

            LOGGER.debug("End retrieving events from {} to {}. Size of events retrieved {} events for {}.", from, to, events.size(), streamName);

            return events;

        }catch (StoreReadingException | IOException e){
            LOGGER.debug("Exception {} occurred when getting the list of events for {}. Stacktrace : {}", e.getClass().getCanonicalName(), streamName, e.getStackTrace());
            throw new StoreReadingException(e);
        }
    }

    public boolean isConnected() {
        try (Jedis jedis = connectionPool.getResource()) {
            jedis.ping();
            return true;
        } catch (JedisException e) {
            return false;
        }
    }

}

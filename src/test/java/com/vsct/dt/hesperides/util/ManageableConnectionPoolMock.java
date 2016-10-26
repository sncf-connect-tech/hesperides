package com.vsct.dt.hesperides.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsct.dt.hesperides.storage.Event;
import io.dropwizard.jackson.Jackson;
import redis.clients.util.Pool;

import java.io.IOException;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by emeric_martineau on 02/06/2016.
 */
public class ManageableConnectionPoolMock implements ManageableJedisConnectionInterface<JedisMock> {
    private PoolMock poolMock = new PoolMock();
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();


    @Override
    public Pool<JedisMock> getPool() {
        return poolMock;
    }

    @Override
    public int getnRetry() {
        return 0;
    }

    @Override
    public int getWaitBeforeRetryMs() {
        return 0;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }

    public void reset() {
        this.poolMock.clear();
    }

    public void checkSavedLastEventOnStream(String streamName, Object event) throws IOException, ClassNotFoundException {
        final String lastEvent = this.poolMock.getResource().getLastEvent(streamName);

        assertFalse("Event stored in wrong event stream", lastEvent == null);

        Event eventStored = MAPPER.readValue(lastEvent, Event.class);

        Object hesperidesEvent = MAPPER.readValue(eventStored.getData(), Class.forName(eventStored.getEventType()));

        assertTrue("Wrong event stored", hesperidesEvent.equals(event));
    }
}

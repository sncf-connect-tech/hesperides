/*
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
 */

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
    private PoolMock poolMock;
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    public ManageableConnectionPoolMock() {
        this.poolMock = new PoolMock();
    }

    public ManageableConnectionPoolMock(final PoolMock poolMock) {
        this.poolMock = poolMock;
    }

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

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

package com.vsct.dt.hesperides.events;

import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.EventBus;
import com.vsct.dt.hesperides.applications.PropertiesSavedEvent;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.storage.RedisEventStore;
import com.vsct.dt.hesperides.storage.UserInfo;
import com.vsct.dt.hesperides.templating.platform.PropertiesData;
import com.vsct.dt.hesperides.util.ManageableConnectionPoolMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by tidiane_sidibe on 19/04/2016.
 */
public class EventsAggregateTest {
    private final EventBus eventBus = new EventBus();
    private final ManageableConnectionPoolMock poolRedis = new ManageableConnectionPoolMock();
    private final EventStore eventStore = new RedisEventStore(poolRedis, poolRedis);
    private final EventsConfiguration testEventsConfiguration = new EventsConfiguration();
    EventsAggregate events = null;


    public void loadEvents (final String streamName){
        final PropertiesData propertiesData = new PropertiesData(ImmutableSet.of(), ImmutableSet.of());

        final PropertiesSavedEvent propertiesSavedEvent = new PropertiesSavedEvent("KTN", "USN1",
                "#DEMO#WAS#demoKatana-war#1.0.0.0#WORKINGCOPY", propertiesData, "");

        final int SIZE = 300;

        for (int i = 0; i < SIZE - 1; i ++) {
            eventStore.store(streamName, propertiesSavedEvent, UserInfo.UNTRACKED, () -> {});
        }
    }

    @Before
    public void setUp() throws Exception {
        testEventsConfiguration.setPoolMaxSize(1);
        testEventsConfiguration.setPoolMaxSize(1);
        testEventsConfiguration.setQueueCapacity(3);
        events = new EventsAggregate(testEventsConfiguration, eventBus, eventStore);
        poolRedis.reset();
        loadEvents("test-stream");
    }

    @Test
    public void shouldGetFirstPageOfEvents (){
        assertThat(events.getEventsList("test-stream", 1, 25).size()).isEqualTo(25);
    }

    @Test
    public void shouldGetAPageOfEvents (){
        assertThat(events.getEventsList("test-stream", 3, 25).size()).isEqualTo(25);
    }

    @After
    public void cleanUp(){
        events = null;
    }
}

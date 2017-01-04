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

import com.google.common.eventbus.EventBus;
import com.vsct.dt.hesperides.EventStoreMock;
import org.junit.After;
import static org.fest.assertions.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by tidiane_sidibe on 19/04/2016.
 */
public class EventsAggregateTest {
    EventBus eventBus        = new EventBus();
    EventStoreMock eventStoreMock  = new EventStoreMock();
    EventsConfiguration testEventsConfiguration = new EventsConfiguration();
    EventsAggregate events = null;

    @Before
    public void setUp() throws Exception {
        testEventsConfiguration.setPoolMaxSize(1);
        testEventsConfiguration.setPoolMaxSize(1);
        testEventsConfiguration.setQueueCapacity(3);
        events = new EventsAggregate(testEventsConfiguration, eventBus, eventStoreMock);
        eventStoreMock.reset();
        eventStoreMock.loadEvents();
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

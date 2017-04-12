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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.vsct.dt.hesperides.storage.Event;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.storage.SingleThreadAggregate;
import io.dropwizard.jackson.Jackson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * The aggregator for events.
 *
 * Created by tidiane_sidibe on 01/03/2016.
 */
public class EventsAggregate extends SingleThreadAggregate implements Events {

    protected final EventStore eventStore;
    private final ObjectMapper MAPPER = Jackson.newObjectMapper();
    private final ExecutorService executor;
    private static final Logger LOGGER = LoggerFactory.getLogger(EventsAggregate.class);

    /**
     * Convenient class that wraps the thread executor of the aggregate
     */
    private ExecutorService singleThreadPool;

    /**
     * The constructor of the aggregator
     * @param eventBus
     * @param eventStore
     */
    public EventsAggregate (final EventsConfiguration eventsConfiguration, final EventBus eventBus, final EventStore eventStore){
        super(eventBus, eventStore);
        this.eventStore = eventStore;
        this.executor = new ThreadPoolExecutor(
                eventsConfiguration.getPoolMinSize(),
                eventsConfiguration.getPoolMaxSize(),
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(eventsConfiguration.getQueueCapacity()));
    }

    @Override
    public List<EventData> getEventsList(final String streamName, final int page, final int size){

        Callable<List<EventData> > task = () -> {
            List<Event> list = this.eventStore.getEventsList(streamName, page, size);

            List<EventData> events = new ArrayList<>();

            try {
                // Converting the data field from String to Object : this is useful for the front-end uses
                for (Event e : list){
                    Object data = MAPPER.readValue(e.getData(), Class.forName(e.getEventType()));
                    events.add(new EventData(e.getEventType(), data, e.getTimestamp(), e.getUser()));
                }

                // Sort in inverse order
                Collections.reverse(events);
                return events;
            }catch (IOException | ClassNotFoundException e){
                LOGGER.debug(" Error while convertingEvent to EventData. Message : {}", e.getMessage());
                return null;
            }
        };

        List<EventData> results = null;
        try {
            results = executor.submit(task).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.debug(" Execution error while getting events list. Message : {}", e.getMessage());
        }

        return results;
    }

    @Override
    public String getStreamPrefix() {
        return null;
    }

    @Override
    protected ExecutorService executorService() {
        return this.singleThreadPool;
    }
}

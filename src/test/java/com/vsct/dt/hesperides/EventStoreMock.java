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

package com.vsct.dt.hesperides;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vsct.dt.hesperides.storage.Event;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.storage.StoreReadingException;
import com.vsct.dt.hesperides.storage.UserInfo;
import io.dropwizard.jackson.Jackson;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.fail;

/**
 * Created by william_montaz on 28/10/2014.
 */
public class EventStoreMock implements EventStore {

    private Object event = null;
    private String stream = null;
    private boolean used = false;
    private boolean isChecked = false;
    private boolean ignoreEvents = false;

    private final int BATCH_SIZE = 100;
    private List<Event> llist = null;

    public void loadEvents (){
        final int SIZE = 300;
        Event [] list = new Event[SIZE];

        for (int i = 0; i < SIZE - 1; i ++){
            Event e = new Event(
                    "com.vsct.dt.hesperides.applications.PropertiesSavedEvent",
                    "{\"applicationName\":\"KTN\",\"platformName\":\"USN1\",\"path\":\"#DEMO#WAS#demoKatana-war#1.0.0.0#WORKINGCOPY\",\"properties\":{\"key_value_properties\":[{\"name\":\"expression_reguliere\",\"value\":\"qdfsdfsdfsdfsfsdfs\"},{\"name\":\"propriete_a_surcharger\",\"value\":\"sdfsdfsdf\"},{\"name\":\"username\",\"value\":\"sdfsdfsdf\"},{\"name\":\"test\",\"value\":\"sdfsdfsdfsdfsdfs\"}],\"iterable_properties\":[]}}",
                    Long.parseLong("1458896478470"),
                    "untracked"

            );
            list[i] = e;
        }
        this.llist = Lists.asList(list[0], list);
    }

    public void checkSavedTheEventOnStream(String streamName, Object event){
        isChecked = true;
        assertEquals("Event stored in wrong event stream", streamName, this.stream);
        assertEquals("Wrong event stored", event, this.event);
    }

    public void verifyUnexpectedEvents(){
        if(!isChecked && !ignoreEvents && (stream != null || event != null))
            fail("An event has been stored but was not checked and you did not explicitly asked to ignore events. This might be unwanted behavior.");
    }

    public void ignoreEvents(){
        ignoreEvents = true;
    }

    @Override
    public <T> T store(String streamName, T event, UserInfo userInfo) {
        if(!ignoreEvents){
            if(used)
                fail("EventStoreMock can be used for just to record one event otherwise it needs to be reset after each event that might have been fired");
            this.stream = streamName;
            this.event = event;
            this.used = true;
        }
        return event;
    }

    @Override
    public void withEvents(String streamName, long stopTimestamp, Consumer<Object> eventConsumer) throws StoreReadingException {
        //Do nothing
    }

    @Override
    public List<Event> getEventsList(final String streamName, final int page, final int size){
        return this.llist.subList((page - 1) * size, page * size); // not zero-based as jedis.lrange !
    }

    @Override
    public Set<String> getStreamsLike(String term) {
        return Sets.newHashSet("Test-Stream");
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    public void reset() {
        this.event = null;
        this.stream = null;
        this.used = false;
        this.isChecked = false;
        this.ignoreEvents = false;
        this.llist = null;
    }

}

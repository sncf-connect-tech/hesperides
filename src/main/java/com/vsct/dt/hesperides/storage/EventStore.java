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

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by william_montaz on 10/10/2014.
 *
 * Updated by Tidane SIDIBE on 01/03/2016.
 *      Adding the getEventList method
 */
/* Simple implementation of an event store not designed to be shared among multiple application instances */
public interface EventStore {

    <T> T store(String streamName, T event, UserInfo userinfo);

    void withEvents(String streamName, final long stopTimestamp, Consumer<Object> eventConsumer) throws StoreReadingException;

    Set<String> getStreamsLike(String term);

    /**
     * This will get the events list form the redis store.
     *
     * @param streamName : the full redis streamName of the application, the module ...
     * @param page       : the pagination current page number
     * @param size       : the pagination size.
     * @return JSON Array of {@link Event} stored for the specified key
     * @throws {@link StoreReadingException} when things go wrong
     * @author Tidiane SIDIBE
     */
    List<Event> getEventsList (final String streamName, final int page, final int size) throws StoreReadingException;

    public boolean isConnected();

}


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
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Created by william_montaz on 10/10/2014.
 *
 * Updated by Tidane SIDIBE on 01/03/2016.
 *      Adding the getEventList method
 */
/* Simple implementation of an event store not designed to be shared among multiple application instances */
public interface EventStore {
    /**
     * Store event.
     *
     * @param streamName
     * @param event
     * @param userinfo
     * @param callback
     * @param <T>
     * @return
     */
    <T> T store(String streamName, T event, UserInfo userinfo, EventStoreCallback callback);

    /**
     * Read snapshot.
     *
     * @param streamName
     * @return
     */
    HesperidesSnapshotItem findLastSnapshot(String streamName);

    /**
     * Read snapshot.
     *
     * @param streamName
     * @param offset
     * @param timestamp lambda expression to validate event to be returned
     *
     * @return
     */
    HesperidesSnapshotItem findSnapshot(String streamName, long offset, long timestamp);

    /**
     * Save snapshot.
     *
     * @param streamName
     * @param object
     * @param offset number of event max before write snapshot
     * @param <T>
     */
    <T> void storeSnapshot(String streamName, T object, long offset);

    /**
     * Create snapshot (don't check if need or not store a new snapshot).
     * Only for cache regeneration.
     *
     * @param streamName
     * @param object
     * @param nbEvent number of event
     * @param <T>
     */
    <T> void createSnapshot(String streamName, T object, long nbEvent) ;

    /**
     * Load event by date.
     *
     * @param streamName
     * @param stopTimestamp
     * @param eventConsumer
     * @throws StoreReadingException
     */
    void withEvents(String streamName, long stopTimestamp,  Consumer<Object> eventConsumer) throws StoreReadingException;

    /**
     * Load event by date.
     *
     * @param streamName
     * @param start
     * @param stop
     * @param stopTimestamp
     * @param eventConsumer
     * @throws StoreReadingException
     */
    void withEvents(String streamName, long start, long stop, long stopTimestamp, Consumer<Object> eventConsumer) throws StoreReadingException;

    /**
     * Load event by position.
     *
     * @param streamName
     * @param start
     * @param stop
     * @param eventConsumer
     * @throws StoreReadingException
     */
    void withEvents(String streamName, long start, long stop, Consumer<Object> eventConsumer) throws StoreReadingException;

    /**
     * Return list of stream.
     *
     * @param term
     *
     * @return list of stream
     */
    Set<String> getStreamsLike(String term);

    /**
     * Check if connection is avaible.
     */
    boolean isConnected();

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

    /**
     * Clear cache.
     *
     * @param streamName name of stream (not name of key of cache)
     */
    void clearCache(String streamName);
}


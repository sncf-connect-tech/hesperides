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

import com.google.common.eventbus.EventBus;
import com.vsct.dt.hesperides.exception.runtime.HesperidesException;
import com.vsct.dt.hesperides.exception.runtime.StateLockedException;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by william_montaz on 22/01/2015.
 */
public abstract class AbstractThreadAggregate implements Managed, StoragePrefixInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractThreadAggregate.class);

    /**
     * Used to store events, storing is actually made throug the try atomic method
     * storing is a single thread process, events queue and wait to be stored
     * More on comments on method tryatomic
     */
    private final EventStore store;
    /**
     * Reference to the application bus to propagate events to listners
     */
    private final EventBus eventBus;

    /**
     * Describes if the aggregate allows write operations.
     * Write operations are not allowed when there has been an unexpected error that could lead to inconsistency
     */
    private AtomicBoolean writable = new AtomicBoolean(true);

    /**
     * UserProvider used to get user information
     */
    private UserProvider userProvider;

    /**
     * Describe state of the aggregate. when it is replaying, the events are not stored
     */
    protected AtomicBoolean isReplaying = new AtomicBoolean(false);

    /**
     * Convenient bus to implement replayability
     * The aggregate will only have to specify methods used to replay events
     * and use the @Subscribe annotation
     * It will automatically be registered on the replayBus for replay
     */
    protected EventBus replayBus = new EventBus();

    /**
     * Default user provider that can be changed later
     */
    private static class DefaultUserProvider implements UserProvider {

        @Override
        public UserInfo getCurrentUserInfo() {
            return UserInfo.UNTRACKED;
        }
    }

    protected AbstractThreadAggregate(final EventBus eventBus, final EventStore eventStore) {
        this(eventBus, eventStore, new DefaultUserProvider());
    }

    protected AbstractThreadAggregate(final EventBus eventBus, final EventStore eventStore, final UserProvider userProvider) {
        this.store = eventStore;
        this.eventBus = eventBus;
        this.userProvider = userProvider;
    }

    /**
     * Used to manipulate state and perform atomic write operations, being sure no else modifies the state concurently
     * Commands are executed in order, they produce events that are stored and then dispatched
     * A better implementation could allow concurrent modification depending on streamName
     */
    protected <T> T tryAtomic(final String entityName, final HesperidesCommand<T> command) {

        UserInfo userInfo = userProvider.getCurrentUserInfo();

        //Execute command and try to store the resulting event
        Future<T> future = this.executorService().submit(() -> {
                    //If we go multi instance that's where we can synchronize state with event store
                    //We are simple instance so dont do it
                    final String key = getStreamPrefix() + "-" + entityName;

                    try {

                        if (writable.get() == false) {
                            throw new StateLockedException("State write operations have been locked for state {}. This is due to an exception that occured when modifying state. APPLICATION RESTART IS NEEDED.");
                        }

                        T event = command.apply();

                        if (isReplaying.get() == false) {
                            store.store(key, event, userInfo, command);
                            eventBus.post(event);
                        } else {
                            command.complete();
                        }

                        return event;
                    } catch (HesperidesException e) {
                        //avoid blocking state with HesperidesExceptions
                        LOGGER.info("HesperidesException has been sent by command for entity '{}'. We dont block the state", entityName);
                        throw e;
                    } catch (RuntimeException e) {
                          /* This is all that should have not happened, SO ITS BAAAAAAD */
                        //For a more advanced implementation we could restore the state back,
                        //With this implementation we will just tell to restart the app in order
                        //to get the state back
                        LOGGER.error("A problem occured when storing the event for entity '{}'. It could now be inconsistent. APPLICATION RESTART IS NEEDED !!", entityName);
                        LOGGER.error(e.getMessage());
                        writable.set(false);
                        throw e;
                    }
                }
        );

        try {
            return future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            //Try to keep the real nature of the exception thrown
            if (cause instanceof HesperidesException) {
                throw (HesperidesException) cause;
            } else if (cause instanceof RuntimeException) {
                LOGGER.error("A problem occured when trying to execute command. This needs further investigation");
                LOGGER.error(e.getMessage());
                throw (RuntimeException) cause;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isWritable() {
        return writable.get();
    }

    /**
     * Aggregate managed by DropWizard
     * Start requires replaying the events
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        // Nothing
    }

    /**
     * Aggregate managed by DropWizard
     * Nothing especial needed for stop
     *
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        // Nothing
    }

    protected EventBus getEventBus() {
        return eventBus;
    }

    protected void replay(final String stream) throws StoreReadingException {
        isReplaying.set(true);
        replayBus.register(this);

        LOGGER.debug("Replay event for key '{}' from scratch.", stream);

        store.withEvents(stream, Long.MAX_VALUE, event -> replayBus.post(event));

        isReplaying.set(false);
        //Release replayBus
        replayBus.unregister(this);
    }

    protected void replay(final String stream, final long start, final long stop) throws StoreReadingException {
        isReplaying.set(true);
        replayBus.register(this);

        LOGGER.debug("Replay event for key '{}' from {} to {}.", stream, start, stop);

        store.withEvents(stream, start, stop, event -> replayBus.post(event));

        isReplaying.set(false);
        //Release replayBus
        replayBus.unregister(this);
    }

    protected void replay(final String stream, final long start, final long stop, final long stopTimestamp) throws StoreReadingException {
        isReplaying.set(true);
        replayBus.register(this);

        LOGGER.debug("Replay event for key '{}' until to timestamp '{}'.", stream, stopTimestamp);

        store.withEvents(stream, start, stop, stopTimestamp, event -> replayBus.post(event));

        isReplaying.set(false);
        //Release replayBus
        replayBus.unregister(this);
    }

    protected void replay(final String stream, final long stopTimestamp) throws StoreReadingException {
        isReplaying.set(true);
        replayBus.register(this);

        LOGGER.debug("Replay event for key '{}' until to timestamp '{}'.", stream, stopTimestamp);

        store.withEvents(stream, stopTimestamp, event -> replayBus.post(event));

        isReplaying.set(false);
        //Release replayBus
        replayBus.unregister(this);
    }

    /**
     * Allow to regenerate cache at startup.
     */
    protected void regenerateCache(final String keySearch) {
        final Set<String> listStream = store.getStreamsLike(keySearch);

        for (String key : listStream) {
            LOGGER.info("Regenerate cache for stream {}.", key);

            clearCacheInDatabase(key);

            replay(key);
        }
    }

    protected void clearCacheInDatabase(final String key) {
        store.clearCache(key);
    }

    /**
     * Return exector service for tryAtomic function.
     * Allow to don't provide thread for Virtual***Aggregate.
     *
     * @return an executor service
     */
    protected abstract ExecutorService executorService();
}

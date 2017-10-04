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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import com.vsct.dt.hesperides.AbstractCacheTest;
import com.vsct.dt.hesperides.applications.PropertiesSavedEvent;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.exception.runtime.StateLockedException;
import com.vsct.dt.hesperides.security.model.User;
import com.vsct.dt.hesperides.util.JedisMock;
import com.vsct.dt.hesperides.util.ManageableConnectionPoolMock;
import com.vsct.dt.hesperides.util.PoolMock;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static junit.framework.TestCase.fail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import io.dropwizard.jackson.Jackson;
import tests.type.UnitTests;

/**
 * Created by wmontaz on 06/11/2014.
 */
@Category(UnitTests.class)
public class AbstractThreadAggregateTest extends AbstractCacheTest {
    /**
     * Builder of event from JSON.
     */
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    /**
     * Mock redis call.
     */
    private JedisMock jedisMock;

    private class AggregateTestImpl extends AbstractThreadAggregate {
        /**
         * Convenient class that wraps the thread executor of the aggregate.
         */
        private ExecutorService singleThreadPool;

        protected AggregateTestImpl() {
            super(new EventBus(), eventStore);

            final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                    .setDaemon(false)
                    .setNameFormat("TEST-%d")
                    .build();

            this.singleThreadPool = Executors.newFixedThreadPool(1, threadFactory);
        }

        @Override
        public String getStreamPrefix() {
            return "TEST";
        }

        @Override
        protected ExecutorService executorService() {
            return this.singleThreadPool;
        }
    }

    private class TestSavedCommand implements HesperidesCommand<PropertiesSavedEvent> {

        @Override
        public void complete() {

        }

        @Override
        public PropertiesSavedEvent apply() {
            return null;
        }
    }

    @Before
    public void setUp() throws Exception {
        reset(this.jedisMock);

        super.setUp();
    }

    /**
     * Provide pool mock with jedis mock.
     * @return
     */
    protected ManageableConnectionPoolMock getManageableConnectionPoolMock()  {
        this.jedisMock = mock(JedisMock.class);
        return new ManageableConnectionPoolMock(new PoolMock(jedisMock));
    }

    @Test
    public void should_apply_command_when_state_is_writable_using_threadlocal_user_informations() throws JsonProcessingException {

        AggregateTestImpl testAggregate = new AggregateTestImpl();

        TestSavedCommand command = mock(TestSavedCommand.class);
        PropertiesSavedEvent event = new PropertiesSavedEvent("applicationName", "platformName", "path", null, "comment");
        when(command.apply()).thenReturn(event);

        testAggregate.tryAtomic("stream", command);

        final Event eventStoreEvent = new Event(event.getClass().getCanonicalName(),
                MAPPER.writeValueAsString(event), timeProvider.currentTimestamp() - 1, UserInfo.UNTRACKED.getName());

        verify(command).apply();
        verify(jedisMock).rpush("TEST-stream", MAPPER.writeValueAsString(eventStoreEvent));
    }

    @Test
    public void should_use_not_tracked_user_when_no_user_provided() throws JsonProcessingException {
        UserInfo userInfo = new UserInfo(User.UNTRACKED.getName());

        AggregateTestImpl testAggregate = new AggregateTestImpl();

        TestSavedCommand command = mock(TestSavedCommand.class);
        PropertiesSavedEvent event = new PropertiesSavedEvent("applicationName", "platformName", "path", null, "comment");
        when(command.apply()).thenReturn(event);

        testAggregate.tryAtomic("stream", command);

        final Event eventStoreEvent = new Event(event.getClass().getCanonicalName(),
                MAPPER.writeValueAsString(event), timeProvider.currentTimestamp() - 1, userInfo.getName());

        verify(command).apply();
        verify(jedisMock).rpush("TEST-stream", MAPPER.writeValueAsString(eventStoreEvent));
    }

    @Test
    public void should_block_state_when_unknown_runtime_exception_occurs() {
        AggregateTestImpl testAggregate = new AggregateTestImpl();

        TestSavedCommand command = mock(TestSavedCommand.class);
        PropertiesSavedEvent event = new PropertiesSavedEvent("applicationName", "platformName", "path", null, "comment");
        when(command.apply()).thenReturn(event);

        when(jedisMock.rpush(any(), any())).thenThrow(new RuntimeException());

        try {
            testAggregate.tryAtomic("stream", command);
        } catch(Exception e) {
            assertThat(testAggregate.isWritable()).isFalse();
            return;
        }

        fail();
    }

    //This is a way to detect an excpetionthrown by aggregate that is qualified
    //HesperidesExceptions guaranties that the state is still useable
    @Test
    public void should_not_block_state_when_hesperides_runtime_exception_occurs() {
        AggregateTestImpl testAggregate = new AggregateTestImpl();

        TestSavedCommand command = mock(TestSavedCommand.class);
        when(command.apply()).thenThrow(new MissingResourceException("Something is missing"));

        try {
            testAggregate.tryAtomic("stream", command);
        } catch(Exception e) {
            assertThat(testAggregate.isWritable()).isTrue();
            return;
        }

        fail();
    }

    @Test(expected = StateLockedException.class)
    public void should_fail_when_state_is_not_writable() {
        AggregateTestImpl testAggregate = new AggregateTestImpl();

        TestSavedCommand command = mock(TestSavedCommand.class);
        when(command.apply()).thenThrow(new RuntimeException());

        /* Block the state */
        try {
            testAggregate.tryAtomic("stream", command);
        } catch(Exception e) {
            testAggregate.tryAtomic("stream", command);
        }

        fail();
    }

}

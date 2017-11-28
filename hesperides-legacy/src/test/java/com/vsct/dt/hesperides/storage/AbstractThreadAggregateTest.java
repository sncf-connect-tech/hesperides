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
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.exception.runtime.StateLockedException;
import com.vsct.dt.hesperides.security.model.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tests.type.UnitTests;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by wmontaz on 06/11/2014.
 */
@Category(UnitTests.class)
public class AbstractThreadAggregateTest {

    static EventStore eventStore = mock(EventStore.class);

    private static class AggregateTestImpl extends AbstractThreadAggregate {
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

    @Before
    public void setUp() {
        reset(eventStore);
    }

    @Test
    public void should_apply_command_when_state_is_writable_using_threadlocal_user_informations() {

        AggregateTestImpl testAggregate = new AggregateTestImpl();

        HesperidesCommand command = mock(HesperidesCommand.class);
        Object event = new Object();
        when(command.apply()).thenReturn(event);

        when(eventStore.store("TEST-stream", event, UserInfo.UNTRACKED, command)).thenReturn(event);

        testAggregate.tryAtomic("stream", command);

        verify(command).apply();
        verify(eventStore).store("TEST-stream", event, UserInfo.UNTRACKED, command);
    }

    @Test
    public void should_use_not_tracked_user_when_no_user_provided() {

        AggregateTestImpl testAggregate = new AggregateTestImpl();

        HesperidesCommand command = mock(HesperidesCommand.class);
        Object event = new Object();
        when(command.apply()).thenReturn(event);

        UserInfo userInfo = new UserInfo(User.UNTRACKED.getUsername());
        when(eventStore.store("TEST-stream", event, userInfo, command)).thenReturn(event);

        testAggregate.tryAtomic("stream", command);

        verify(command).apply();
        verify(eventStore).store("TEST-stream", event, UserInfo.UNTRACKED, command);
    }

    @Test
    public void should_block_state_when_unknown_runtime_exception_occurs() {
        AggregateTestImpl testAggregate = new AggregateTestImpl();

        HesperidesCommand command = mock(HesperidesCommand.class);
        when(command.apply()).thenThrow(new RuntimeException());

        try {
            testAggregate.tryAtomic("stream", command);
        } catch (Exception e) {
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

        HesperidesCommand command = mock(HesperidesCommand.class);
        when(command.apply()).thenThrow(new MissingResourceException("Something is missing"));

        try {
            testAggregate.tryAtomic("stream", command);
        } catch (Exception e) {
            assertThat(testAggregate.isWritable()).isTrue();
            return;
        }

        fail();
    }

    @Test
    public void should_lock_when_eventstore_throws_runtime() {
        AggregateTestImpl testAggregate = new AggregateTestImpl();

        HesperidesCommand command = mock(HesperidesCommand.class);
        Object event = new Object();
        when(command.apply()).thenReturn(event);

        when(eventStore.store("TEST-stream", event, UserInfo.UNTRACKED, command)).thenThrow(new RuntimeException());

        try {
            testAggregate.tryAtomic("stream", command);
        } catch (Exception e) {
            assertThat(testAggregate.isWritable()).isFalse();
            return;
        }

        fail();
    }

    @Test(expected = StateLockedException.class)
    public void should_fail_when_state_is_not_writable() {
        AggregateTestImpl testAggregate = new AggregateTestImpl();

        HesperidesCommand command = mock(HesperidesCommand.class);
        when(command.apply()).thenThrow(new RuntimeException());

        /* Block the state */
        try {
            testAggregate.tryAtomic("stream", command);
        } catch (Exception e) {
            testAggregate.tryAtomic("stream", command);
        }

        fail();
    }

}

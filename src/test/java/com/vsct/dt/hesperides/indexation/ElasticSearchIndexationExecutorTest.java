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

package com.vsct.dt.hesperides.indexation;

import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by william_montaz on 04/11/2014.
 */
public class ElasticSearchIndexationExecutorTest {

    ElasticSearchClient elasticSearchClient = mock(ElasticSearchClient.class);

    @Test
    public void testThatItRunsTask() throws ExecutionException, InterruptedException {
        ElasticSearchIndexationExecutor elasticSearchIndexationExecutor = new ElasticSearchIndexationExecutor(elasticSearchClient, 1, 100);
        ElasticSearchIndexationCommand task = mock(ElasticSearchIndexationCommand.class);

        elasticSearchIndexationExecutor.index(task).get();

        verify(task).index(elasticSearchClient);
    }

    @Test
    public void testThatItRetriesAndReturnsSuccessIfLessThanNRetry() throws ExecutionException, InterruptedException {
        ElasticSearchIndexationExecutor elasticSearchIndexationExecutor = new ElasticSearchIndexationExecutor(elasticSearchClient, 2, 100);
        ElasticSearchIndexationCommand task = mock(ElasticSearchIndexationCommand.class);
        when(task.index(elasticSearchClient)).thenThrow(new RuntimeException()).thenReturn(null);

        elasticSearchIndexationExecutor.index(task).get();


        verify(task, times(2)).index(elasticSearchClient);
    }

    @Test
    public void testThatItReturnsFailAfterTryingNTimesWithUncheckedExceptions() throws ExecutionException, InterruptedException {
        ElasticSearchIndexationExecutor elasticSearchIndexationExecutor = new ElasticSearchIndexationExecutor(elasticSearchClient, 2, 100);
        ElasticSearchIndexationCommand task = mock(ElasticSearchIndexationCommand.class);
        when(task.index(elasticSearchClient)).thenThrow(new RuntimeException())
                .thenThrow(new RuntimeException());

        elasticSearchIndexationExecutor.index(task).get();

        verify(task, times(2)).index(elasticSearchClient);
    }


}

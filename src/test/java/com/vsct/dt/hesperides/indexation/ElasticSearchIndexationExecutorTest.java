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

import com.vsct.dt.hesperides.AbstractCacheTest;
import com.vsct.dt.hesperides.applications.PlatformKey;
import com.vsct.dt.hesperides.resources.HesperidesFullIndexationResource;
import com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyKey;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageWorkingCopyKey;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;

import tests.type.UnitTests;

/**
 * Created by william_montaz on 04/11/2014.
 */
@Category(UnitTests.class)
public class ElasticSearchIndexationExecutorTest extends AbstractCacheTest {
    private ElasticSearchClient elasticSearchClient = mock(ElasticSearchClient.class);


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

    @Test
    public void full_indexation() throws IOException {
        // Create module with template
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("my_module1", "the_version");
        generateModule(moduleKey, NB_EVENT_BEFORE_STORE);
        // Create deleted module
        moduleKey = new ModuleWorkingCopyKey("my_module2", "the_version");
        generateModule(moduleKey, 0);
        this.modulesWithEvent.delete(moduleKey);

        // Create template package
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package1", "package_version");
        generateTemplatePackage(packageInfo, NB_EVENT_BEFORE_STORE);
        // Create deleted template package
        packageInfo = new TemplatePackageWorkingCopyKey("some_package2", "package_version");
        generateTemplatePackage(packageInfo, 0);
        this.templatePackagesWithEvent.delete(packageInfo);

        // Create application
        PlatformKey platformKey = PlatformKey.withName("a_pltfm1")
                .withApplicationName("an_app")
                .build();
        generateApplication(platformKey, NB_EVENT_BEFORE_STORE);
        // Create deleted application
        platformKey = PlatformKey.withName("a_pltfm2")
                .withApplicationName("an_app")
                .build();
        generateApplication(platformKey, 0);
        this.applicationsWithEvent.delete(platformKey);

        // Run indexation
        HttpClient httpClient = mock(HttpClient.class);

        Mockito.when(httpClient.execute(Mockito.any(), (HttpRequest) Mockito.any())).thenReturn(null);

        Mockito.when(elasticSearchClient.getClient()).thenReturn(httpClient);

        ElasticSearchIndexationExecutor elasticSearchIndexationExecutor
                = new ElasticSearchIndexationExecutor(elasticSearchClient, 2, 100);
        HesperidesFullIndexationResource fullIndexationResource = new HesperidesFullIndexationResource(
                elasticSearchIndexationExecutor, applicationsWithEvent, modulesWithEvent, templatePackagesWithEvent);

        fullIndexationResource.resetIndex();
    }
}

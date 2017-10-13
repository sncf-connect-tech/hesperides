/*
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
 */

package com.vsct.dt.hesperides.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vsct.dt.hesperides.applications.Applications;
import com.vsct.dt.hesperides.applications.InstanceModel;
import com.vsct.dt.hesperides.applications.PlatformKey;
import com.google.common.eventbus.EventBus;
import com.vsct.dt.hesperides.HesperidesCacheParameter;
import com.vsct.dt.hesperides.HesperidesConfiguration;
import com.vsct.dt.hesperides.applications.*;
import com.vsct.dt.hesperides.indexation.ESServiceException;
import com.vsct.dt.hesperides.indexation.search.ApplicationSearch;
import com.vsct.dt.hesperides.indexation.search.ApplicationSearchResponse;
import com.vsct.dt.hesperides.indexation.search.ModuleSearch;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.storage.RedisEventStore;
import com.vsct.dt.hesperides.storage.RetryRedisConfiguration;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import com.vsct.dt.hesperides.templating.models.KeyValuePropertyModel;
import com.vsct.dt.hesperides.templating.modules.ModuleKey;
import com.vsct.dt.hesperides.templating.modules.Modules;
import com.vsct.dt.hesperides.templating.platform.*;
import com.vsct.dt.hesperides.util.HesperidesCacheConfiguration;
import com.vsct.dt.hesperides.util.ManageableConnectionPoolMock;
import com.vsct.dt.hesperides.util.Release;
import com.vsct.dt.hesperides.util.WorkingCopy;
import com.vsct.dt.hesperides.util.converter.ApplicationConverter;
import com.vsct.dt.hesperides.util.converter.PlatformConverter;
import com.vsct.dt.hesperides.util.converter.PropertiesConverter;
import com.vsct.dt.hesperides.util.converter.TimeStampedPlatformConverter;
import com.vsct.dt.hesperides.util.converter.impl.*;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tests.type.UnitTests;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by william_montaz on 01/09/14.
 */
@Category(UnitTests.class)
public class HesperidesApplicationResourceTest extends AbstractDisableUserResourcesTest {
    private static final Applications applications = mock(Applications.class);
    private static final Modules modules = mock(Modules.class);
    private static final ApplicationSearch applicationSearch = mock(ApplicationSearch.class);
    private static final ModuleSearch moduleSearch = mock(ModuleSearch.class);

    public static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    public static final PlatformConverter PLATFORM_CONVERTER = new DefaultPlatformConverter(new DefaultInstanceConverter());
    public static final TimeStampedPlatformConverter TIME_CONVERTER = new DefaultTimeStampedPlatformConverter(PLATFORM_CONVERTER);
    public static final ApplicationConverter APPLI_CONVERTER = new DefaultApplicationConverter(PLATFORM_CONVERTER);
    private static final PropertiesConverter PROPERTIES_CONVERTER = new DefaultPropertiesConverter();
    private static final HesperidesModuleResource MODULE_RESOURCE = new HesperidesModuleResource(modules, moduleSearch);

    private final String comment = "Test comment";

    private final EventBus eventBus = new EventBus();
    private final ManageableConnectionPoolMock poolRedis = new ManageableConnectionPoolMock();
    private final EventStore eventStore = new RedisEventStore(poolRedis, poolRedis, () -> System.currentTimeMillis());
    private ApplicationsAggregate applicationsWithEvent;

    @ClassRule
    public static ResourceTestRule simpleAuthResources = createSimpleAuthResource(
            new HesperidesApplicationResource(applications, modules, applicationSearch, TIME_CONVERTER,
                    APPLI_CONVERTER, PROPERTIES_CONVERTER, MODULE_RESOURCE)
    );

    @ClassRule
    public static ResourceTestRule disabledAuthResources  = createDisabledAuthResource(
            new HesperidesApplicationResource(applications, modules, applicationSearch, TIME_CONVERTER,
                    APPLI_CONVERTER, PROPERTIES_CONVERTER, MODULE_RESOURCE)
    );


    @Override
    protected ResourceTestRule getAuthResources() {
        return simpleAuthResources;
    }

    @Override
    protected ResourceTestRule getDisabledAuthResources() {
        return disabledAuthResources;
    }

    @Before
    public void setup() throws AuthenticationException {
        reset(applications);
        reset(applicationSearch);

        final RetryRedisConfiguration retryRedisConfiguration = new RetryRedisConfiguration();
        final HesperidesCacheParameter hesperidesCacheParameter = new HesperidesCacheParameter();

        final HesperidesCacheConfiguration hesperidesCacheConfiguration = new HesperidesCacheConfiguration();
        hesperidesCacheConfiguration.setRedisConfiguration(retryRedisConfiguration);
        hesperidesCacheConfiguration.setPlatformTimeline(hesperidesCacheParameter);
        hesperidesCacheConfiguration.setTemplatePackage(hesperidesCacheParameter);
        hesperidesCacheConfiguration.setNbEventBeforePersiste(10000);

        final HesperidesConfiguration hesperidesConfiguration = new HesperidesConfiguration();
        hesperidesConfiguration.setCacheConfiguration(hesperidesCacheConfiguration);

        applicationsWithEvent = new ApplicationsAggregate(eventBus, eventStore,
                new SnapshotRegistry(poolRedis.getPool()), hesperidesConfiguration);
        poolRedis.reset();
    }

    @Test
    public void should_get_application_with_name() throws ESServiceException {
        final List<PlatformData> listPlatform = new ArrayList<>(1);

        final PlatformData ptfData = PlatformData
                .withPlatformName("my_platform")
                .withApplicationName("my_name")
                .withApplicationVersion("my_version")
                .withModules(new HashSet<>(0))
                .withVersion(1)
                .build();

        listPlatform.add(ptfData);

        ApplicationData application = new ApplicationData("my_name", Lists.newArrayList(ptfData));

        Set<ApplicationSearchResponse> elsResponse = new HashSet<>(1);

        elsResponse.add(new ApplicationSearchResponse("my_name", "my_version", "my_platform"));

        when(applicationSearch.getApplications("my_name")).thenReturn(elsResponse);

        when(applications.getPlatform(new PlatformKey("my_name", "my_platform"))).thenReturn(Optional.of(ptfData));

        Application app = withoutAuth("/applications/my_name")
                .request()
                .get()
                .readEntity(Application.class);

        assertThat(application.getName()).isEqualTo(app.getName());
        assertThat(application.getPlatforms().size()).isEqualTo(app.getPlatforms().size());
    }

    @Test
    public void should_return_404_if_not_found() throws ESServiceException {
        Set<ApplicationSearchResponse> elsResponse = new HashSet<>(1);

        assertThat(
                withoutAuth("/applications/app_name")
                .request()
                .get()
                .getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void get_application_should_return_401_if_not_authenticated() {
        assertThat(
                withAuth("/applications/name")
                    .request()
                    .get().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void should_search_applications_with_provided_name() throws ESServiceException {
        ApplicationSearchResponse applicationSearchResponse1 =  new ApplicationSearchResponse("name1", "version1", "platform1");
        ApplicationSearchResponse applicationSearchResponse2 =  new ApplicationSearchResponse("name2", "version2", "platform2");

        ApplicationListItem item1 = new ApplicationListItem("name1");
        ApplicationListItem item2 = new ApplicationListItem("name2");

        when(applicationSearch.getApplicationsLike("na")).thenReturn(Sets.newHashSet(applicationSearchResponse1, applicationSearchResponse2));

        assertThat(withoutAuth("/applications/perform_search").queryParam("name", "na")
                .request()
                .post(Entity.json(null))
                .readEntity(new GenericType<Set<ApplicationListItem>>() {})
        ).isEqualTo(Sets.newHashSet(item1, item2));
    }

    @Test
    public void should_return_400_if_query_param_name_is_not_provided() {
        assertThat(
            withoutAuth("/applications/perform_search")
                    .request()
                    .post(Entity.json(null))
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void search_application_should_return_401_if_not_authenticated() {
        assertThat(
            withAuth("/applications/perform_search")
                    .request()
                    .post(Entity.json(null))
                    .getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void should_get_platform_with_application_name_and_platform_name() throws ESServiceException {
        PlatformData.IBuilder builder = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder.build();

        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();
        when(applications.getPlatform(platformKey)).thenReturn(Optional.of(platform));

        assertThat(withoutAuth("/applications/app_name/platforms/pltfm_name")
                .request()
                .get()
                .readEntity(Platform.class))
                .isEqualTo(PLATFORM_CONVERTER.toPlatform(platform));
    }

    @Test
    public void should_get_platform_with_application_name_and_platform_name_without_application_name() throws ESServiceException {
        PlatformData.IBuilder builder = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder.build();

        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();
        when(applications.getPlatform(platformKey)).thenReturn(Optional.of(platform));

        assertThat(withoutAuth("/applications/app_name/platforms/pltfm_name")
                .request()
                .get()
                .readEntity(Platform.class))
                .isEqualTo(PLATFORM_CONVERTER.toPlatform(platform));
    }

    @Test
    public void should_return_404_if_platform_is_not_found() throws ESServiceException {
        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();
        when(applications.getPlatform(platformKey)).thenReturn(Optional.empty());

        assertThat(
            withoutAuth("/applications/app_name/platforms/pltfm_name")
                    .request()
                    .get()
                    .getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void get_platform_should_return_401_if_not_authenticated() {
        assertThat(
            withAuth("/applications/app_name/platforms/pltfm_name")
                    .request()
                    .get()
                    .getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void should_create_new_platform_if_from_application_and_from_platform_query_params_are_not_provided() throws Exception {
        PlatformData.IBuilder builder = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder.build();

        when(applications.createPlatform(platform)).thenReturn(platform);

        assertThat(withoutAuth("/applications/app_name/platforms")
                .request()
                .post(Entity.json(PLATFORM_CONVERTER.toPlatform(platform)))
                .readEntity(Platform.class))
                .isEqualTo(PLATFORM_CONVERTER.toPlatform(platform));
    }

    @Test
    public void should_create_platform_from_another_one_if_from_application_and_from_platform_query_params_are_both_provided() throws JsonProcessingException {
        PlatformKey fromPlatformKey = PlatformKey.withName("the_pltfm_from")
                .withApplicationName("the_app_from")
                .build();

        PlatformData.IBuilder builder = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder.build();

        when(applications.createPlatformFromExistingPlatform(platform, fromPlatformKey)).thenReturn(platform);

        assertThat(withoutAuth("/applications/app_name/platforms")
                .queryParam("from_application", "the_app_from")
                .queryParam("from_platform", "the_pltfm_from")
                .request()
                .post(Entity.json(PLATFORM_CONVERTER.toPlatform(platform)))
                .readEntity(Platform.class))
                .isEqualTo(PLATFORM_CONVERTER.toPlatform(platform));
    }

    @Test
    public void should_return_400_when_creating_platform_from_but_from_application_query_param_is_missing() throws JsonProcessingException {
        Platform platform = new Platform("pltfm_name", "app_name", "app_version", true, Sets.newHashSet(), 1L);
        assertThat(
            withoutAuth("/applications/app_name/platforms")
                    .queryParam("from_platform", "the_pltfm_from")
                    .request()
                    .post(Entity.json(platform))
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_return_400_when_creating_platform_from_but_from_platform_query_param_is_missing() throws JsonProcessingException {
        Platform platform = new Platform("pltfm_name", "app_name", "app_version", true, Sets.newHashSet(), 1L);
        assertThat(
            withoutAuth("/applications/app_name/platforms")
                    .queryParam("from_application", "the_app_from")
                    .request()
                    .post(Entity.json(platform))
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_return_422_when_creating_platform_from_but_body_platformName_is_null() throws JsonProcessingException {
        PlatformKey fromPlatformKey = PlatformKey.withName("the_pltfm_from")
                .withApplicationName("the_app_from")
                .build();

        PlatformData.IBuilder builder = PlatformData
                .withPlatformName(null)
                .withApplicationName("app_name")
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder.build();

        when(applications.createPlatformFromExistingPlatform(platform, fromPlatformKey)).thenReturn(platform);

        assertThat(
                withoutAuth("/applications/app_name/platforms")
                        .queryParam("from_application", "the_app_from")
                        .queryParam("from_platform", "the_pltfm_from")
                        .request()
                        .put(Entity.json(PLATFORM_CONVERTER.toPlatform(platform)))
                        .getStatus()).isEqualTo(CLIENT_ERROR);
    }

    @Test
    public void should_return_422_when_creating_platform_from_but_body_applicationName_is_null() throws JsonProcessingException {
        PlatformKey fromPlatformKey = PlatformKey.withName("the_pltfm_from")
                .withApplicationName("the_app_from")
                .build();

        PlatformData.IBuilder builder = PlatformData
                .withPlatformName("pltfm_name")
                .withApplicationName(null)
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder.build();

        when(applications.createPlatformFromExistingPlatform(platform, fromPlatformKey)).thenReturn(platform);

        assertThat(
                withoutAuth("/applications/app_name/platforms")
                        .queryParam("from_application", "the_app_from")
                        .queryParam("from_platform", "the_pltfm_from")
                        .request()
                        .put(Entity.json(PLATFORM_CONVERTER.toPlatform(platform)))
                        .getStatus()).isEqualTo(CLIENT_ERROR);
    }

    @Test
    public void should_return_422_when_creating_platform_from_but_body_applicationVersion_is_null() throws JsonProcessingException {
        PlatformKey fromPlatformKey = PlatformKey.withName("the_pltfm_from")
                .withApplicationName("the_app_from")
                .build();

        PlatformData.IBuilder builder = PlatformData
                .withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion(null)
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder.build();

        when(applications.createPlatformFromExistingPlatform(platform, fromPlatformKey)).thenReturn(platform);

        assertThat(
                withoutAuth("/applications/app_name/platforms")
                        .queryParam("from_application", "the_app_from")
                        .queryParam("from_platform", "the_pltfm_from")
                        .request()
                        .put(Entity.json(PLATFORM_CONVERTER.toPlatform(platform)))
                        .getStatus()).isEqualTo(CLIENT_ERROR);
    }

    @Test
    public void should_return_422_when_creating_platform_from_but_body_platformName_is_empty() throws JsonProcessingException {
        PlatformKey fromPlatformKey = PlatformKey.withName("the_pltfm_from")
                .withApplicationName("the_app_from")
                .build();

        PlatformData.IBuilder builder = PlatformData
                .withPlatformName("")
                .withApplicationName("app_name")
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder.build();

        when(applications.createPlatformFromExistingPlatform(platform, fromPlatformKey)).thenReturn(platform);

        assertThat(
                withoutAuth("/applications/app_name/platforms")
                        .queryParam("from_application", "the_app_from")
                        .queryParam("from_platform", "the_pltfm_from")
                        .request()
                        .put(Entity.json(PLATFORM_CONVERTER.toPlatform(platform)))
                        .getStatus()).isEqualTo(CLIENT_ERROR);
    }

    @Test
    public void should_return_422_when_creating_platform_from_but_body_applicationName_is_empty() throws JsonProcessingException {
        PlatformKey fromPlatformKey = PlatformKey.withName("the_pltfm_from")
                .withApplicationName("the_app_from")
                .build();

        PlatformData.IBuilder builder = PlatformData
                .withPlatformName("pltfm_name")
                .withApplicationName("")
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder.build();

        when(applications.createPlatformFromExistingPlatform(platform, fromPlatformKey)).thenReturn(platform);

        assertThat(
                withoutAuth("/applications/app_name/platforms")
                        .queryParam("from_application", "the_app_from")
                        .queryParam("from_platform", "the_pltfm_from")
                        .request()
                        .put(Entity.json(PLATFORM_CONVERTER.toPlatform(platform)))
                        .getStatus()).isEqualTo(CLIENT_ERROR);
    }

    @Test
    public void should_return_422_when_creating_platform_from_but_body_applicationVersion_is_empty() throws JsonProcessingException {
        PlatformKey fromPlatformKey = PlatformKey.withName("the_pltfm_from")
                .withApplicationName("the_app_from")
                .build();

        PlatformData.IBuilder builder = PlatformData
                .withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder.build();

        when(applications.createPlatformFromExistingPlatform(platform, fromPlatformKey)).thenReturn(platform);

        assertThat(
                withoutAuth("/applications/app_name/platforms")
                        .queryParam("from_application", "the_app_from")
                        .queryParam("from_platform", "the_pltfm_from")
                        .request()
                        .put(Entity.json(PLATFORM_CONVERTER.toPlatform(platform)))
                        .getStatus()).isEqualTo(CLIENT_ERROR);
    }

    @Test
    public void create_platform_should_return_401_if_not_authenticated() {
        assertThat(
            withAuth("/applications/app_name/platforms")
                    .request()
                    .post(Entity.json(null))
                    .getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void should_update_platform_for_application_without_copying_properties_for_upgraded_modules_if_query_param_is_not_provided() throws
            Exception {
        PlatformData.IBuilder builder1 = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform1 = builder1.build();

        PlatformData.IBuilder builder2 = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet())
                .withVersion(2L)
                .isProduction();

        PlatformData platform2 = builder2.build();

        when(applications.updatePlatform(platform1, false)).thenReturn(platform2);

        assertThat(withoutAuth("/applications/app_name/platforms")
                .request()
                .put(Entity.json(PLATFORM_CONVERTER.toPlatform(platform1)))
                .readEntity(Platform.class))
                .isEqualTo(PLATFORM_CONVERTER.toPlatform(platform2));
    }

    @Test
    public void should_update_platform_for_application_copying_properties_for_upgraded_modules_if_query_param_is_provided() throws Exception {
        PlatformData.IBuilder builder1 = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform1 = builder1.build();

        PlatformData.IBuilder builder2 = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet())
                .withVersion(2L)
                .isProduction();

        PlatformData platform2 = builder2.build();

        when(applications.updatePlatform(platform1, true)).thenReturn(platform2);

        assertThat(withoutAuth("/applications/app_name/platforms")
                .queryParam("copyPropertiesForUpgradedModules", "true")
                .request()
                .put(Entity.json(PLATFORM_CONVERTER.toPlatform(platform1)))
                .readEntity(Platform.class))
                .isEqualTo(PLATFORM_CONVERTER.toPlatform(platform2));
    }

    @Test
    public void should_return_422_when_updating_plateform_with_empty_application_version() throws Exception {
        PlatformData.IBuilder builder = PlatformData
                .withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder.build();

        when(applications.updatePlatform(platform, false)).thenReturn(platform);

        assertThat(
            withoutAuth("/applications/app_name/platforms")
                .queryParam("copyPropertiesForUpgradedModules", "false")
                .request()
                .put(Entity.json(MAPPER.writeValueAsString(PLATFORM_CONVERTER.toPlatform(platform))))
                .getStatus()).isEqualTo(CLIENT_ERROR);
    }

    @Test
    public void update_platform_should_return_401_if_not_authenticated() {
        assertThat(
            withAuth("/applications/app_name/platforms")
                    .request()
                    .put(Entity.json(""))
                    .getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test(expected = Exception.class)
    public void should_return_properties_with_application_name_and_platform_name() throws JsonProcessingException {
        PropertiesData properties = new PropertiesData(Sets.newHashSet(), Sets.newHashSet());
        Properties properties2 = new Properties(Sets.newHashSet(), Sets.newHashSet());
        PlatformKey platformKey = PlatformKey.withName("my_pltfm")
                .withApplicationName("my_app")
                .build();
        when(applications.getProperties(platformKey, "some_path")).thenReturn(properties);
        assertThat(withoutAuth("/applications/my_app/platforms/my_pltfm/properties")
                .queryParam("path", "some_path")
                .request()
                .get()
                .readEntity(Properties.class))
                .isEqualTo(properties2);
    }

    @Test
    public void should_return_400_if_getting_properties_and_path_query_param_is_missing() throws JsonProcessingException {
        assertThat(
            withoutAuth("/applications/my_app/platforms/my_pltfm/properties")
                    .request()
                    .get()
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void get_properties_should_return_401_if_not_authenticated() {
        assertThat(
            withAuth("/applications/my_app/platforms/my_pltfm/properties")
                    .request()
                    .get()
                    .getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void should_save_properties_for_application_and_platform_with_path_and_platform_vid_query_params_not_empty() throws JsonProcessingException {
        Properties properties = new Properties(Sets.newHashSet(), Sets.newHashSet());
        Properties properties2 = new Properties(Sets.newHashSet(), Sets.newHashSet());
        PlatformKey platformKey = PlatformKey.withName("my_pltfm")
                .withApplicationName("my_app")
                .build();
        when(applications.createOrUpdatePropertiesInPlatform(platformKey, "some_path",
                PROPERTIES_CONVERTER.toPropertiesData(properties), 1L, comment)).thenReturn(
                PROPERTIES_CONVERTER.toPropertiesData(properties));

        assertThat(withoutAuth("/applications/my_app/platforms/my_pltfm/properties")
                .queryParam("path", "some_path")
                .queryParam("platform_vid", "1")
                .queryParam("comment", "Test comment")
                .request()
                .post(Entity.json(properties))
                .readEntity(Properties.class))
                .isEqualTo(properties2);
    }

    @Test
    public void should_save_only_valued_properties_and_not_null_or_empty_ones() throws JsonProcessingException {
        Set<KeyValueValorisation> keyValueProperties = Sets.newHashSet();
        keyValueProperties.add(new KeyValueValorisation("name1", "value"));
        keyValueProperties.add(new KeyValueValorisation("name2", ""));
        keyValueProperties.add(new KeyValueValorisation("name3", ""));
        keyValueProperties.add(new KeyValueValorisation("name5", "value"));

        Set<KeyValueValorisationData> keyValuePropertiesCleaned = Sets.newHashSet();
        keyValuePropertiesCleaned.add(new KeyValueValorisationData("name1", "value"));
        keyValuePropertiesCleaned.add(new KeyValueValorisationData("name5", "value"));

        Properties properties = new Properties(keyValueProperties, Sets.newHashSet());
        PropertiesData propertiesCleaned = new PropertiesData(keyValuePropertiesCleaned, Sets.newHashSet());

        PlatformKey platformKey = PlatformKey.withName("my_pltfm")
                .withApplicationName("my_app")
                .build();

        when(applications.createOrUpdatePropertiesInPlatform(platformKey, "some_path", propertiesCleaned, 1L, comment)).thenReturn(propertiesCleaned);

        withoutAuth("/applications/my_app/platforms/my_pltfm/properties")
                .queryParam("path", "some_path")
                .queryParam("platform_vid", "1")
                .queryParam("comment", "Test comment")
                .request()
                .post(Entity.json(properties));

        //Check that service is called with cleaned properties
        verify(applications).createOrUpdatePropertiesInPlatform(platformKey, "some_path",
                propertiesCleaned, 1L, comment);
    }

    @Test
    public void should_save_only_valued_iterable_properties_and_not_null_or_empty_ones() throws JsonProcessingException {
        // Iterable properties
        IterableValorisation.IterableValorisationItem itemIterable2 = new IterableValorisation.IterableValorisationItem("blockOfProperties", Sets.newHashSet(new KeyValueValorisation("name3", "value"), new KeyValueValorisation("name0", "")));
        IterableValorisation iterableValorisation2 = new IterableValorisation("iterable2", Lists.newArrayList(itemIterable2));

        IterableValorisation.IterableValorisationItem item = new IterableValorisation.IterableValorisationItem("blockOfProperties", Sets.newHashSet(new KeyValueValorisation("name2", "value"), iterableValorisation2));
        IterableValorisation iterableValorisation = new IterableValorisation("iterable", Lists.newArrayList(item));

        // Properties
        Properties properties = new Properties(Sets.newHashSet(new KeyValueValorisation("simple1", "value"), new KeyValueValorisation("simple2", "")), Sets.newHashSet(iterableValorisation));

        // Cleaned properties
        Set<KeyValueValorisationData> keyValuePropertiesCleaned = Sets.newHashSet();
        keyValuePropertiesCleaned.add(new KeyValueValorisationData("simple1", "value"));

        IterableValorisationData.IterableValorisationItemData itemIterable2Cleaned = new IterableValorisationData.IterableValorisationItemData("blockOfProperties", Sets.newHashSet(new KeyValueValorisationData("name3", "value")));
        IterableValorisationData iterableValorisation2Cleaned = new IterableValorisationData("iterable2", Lists.newArrayList(itemIterable2Cleaned));

        IterableValorisationData.IterableValorisationItemData itemCleaned = new IterableValorisationData.IterableValorisationItemData("blockOfProperties", Sets.newHashSet(new KeyValueValorisationData("name2", "value"), iterableValorisation2Cleaned));
        Set<IterableValorisationData> iterablePropertiesCleaned = Sets.newHashSet(new IterableValorisationData("iterable", Lists.newArrayList(itemCleaned)));

        PropertiesData propertiesCleaned = new PropertiesData(keyValuePropertiesCleaned, iterablePropertiesCleaned);

        PlatformKey platformKey = PlatformKey.withName("my_pltfm")
                .withApplicationName("my_app")
                .build();

        when(applications.createOrUpdatePropertiesInPlatform(platformKey, "some_path", propertiesCleaned, 1L, comment)).thenReturn(propertiesCleaned);

        Properties p = withoutAuth("/applications/my_app/platforms/my_pltfm/properties")
                .queryParam("path", "some_path")
                .queryParam("platform_vid", "1")
                .queryParam("comment", "Test comment")
                .request()
                .post(Entity.json(properties))
                .readEntity(Properties.class);

        //Check that service is called with cleaned properties
        verify(applications).createOrUpdatePropertiesInPlatform(platformKey, "some_path",
                propertiesCleaned, 1L, comment);
    }

    @Test
    public void should_return_400_if_trying_to_save_properties_without_path_query_param_provided() throws JsonProcessingException {
        Properties properties = new Properties(Sets.newHashSet(), Sets.newHashSet());

        assertThat(
            withoutAuth("/applications/my_app/platforms/my_pltfm/properties")
                    .queryParam("platform_vid", "1")
                    .request()
                    .post(Entity.json(properties))
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_return_400_if_trying_to_save_properties_without_platform_vid_param_provided() throws JsonProcessingException {
        Properties properties = new Properties(Sets.newHashSet(), Sets.newHashSet());

        assertThat(
            withoutAuth("/applications/my_app/platforms/my_pltfm/properties")
                    .queryParam("path", "some_path")
                    .request()
                    .post(Entity.json(properties))
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_return_401_if_saving_properties_and_not_authenticated() throws JsonProcessingException {
        Properties properties = new Properties(Sets.newHashSet(), Sets.newHashSet());

        assertThat(
            withAuth("/applications/my_app/platforms/my_pltfm/properties")
                    .request()
                    .post(Entity.json(properties))
                    .getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void should_return_instance_model_for_given_application_platform_and_instance() {
        InstanceModel instanceModel = new InstanceModel(Sets.newHashSet());
        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();
        when(applications.getInstanceModel(platformKey, "some_path")).thenReturn(instanceModel);

        assertThat(withoutAuth("/applications/app_name/platforms/pltfm_name/properties/instance_model")
                .queryParam("path", "some_path")
                .request()
                    .get(InstanceModel.class)).isEqualTo(instanceModel);
    }

    @Test
    public void should_return_401_if_getting_instance_model_and_not_authenticated(){
        assertThat(
            withAuth("/applications/app_name/platforms/pltfm_name/properties/instance_model")
                    .queryParam("path", "some_path")
                    .request()
                    .get(Response.class)
                    .getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void should_return_400_if_application_name_is_not_valid() {
        check_bad_request_on_get_without_auth("/applications/%20%09%00");
    }

    @Test
    public void should_return_400_if_application_name_is_not_valid_with_get() {
        check_bad_request_on_get_without_auth("/applications/%20%09/platforms/eriotuoeri");
    }

    @Test
    public void should_return_400_if_platform_name_is_not_valid_with_get() {
        check_bad_request_on_get_without_auth("/applications/app_name/platforms/%20%09");
    }

    @Test
    public void should_return_400_if_application_name_is_not_valid_with_delete() {
        check_bad_request_on_delete_without_auth("/applications/%20%09/platforms/eriotuoeri");
    }

    @Test
    public void should_return_400_if_platform_name_is_not_provided_with_delete() {
        check_bad_request_on_delete_without_auth("/applications/app_name/platforms/%20%09");
    }

    @Test
    public void should_return_204_when_create_platform_if_application_name_is_not_valid_with_post()
            throws JsonProcessingException {
        PlatformKey fromPlatformKey = PlatformKey.withName("the_pltfm_from")
                .withApplicationName("the_app_from")
                .build();

        PlatformData.IBuilder builder1 = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder1.build();

        when(applications.createPlatformFromExistingPlatform(platform, fromPlatformKey)).thenReturn(platform);

        assertThat(withoutAuth("/applications/%20%09%00/platforms")
                .queryParam("from_application", "the_app_from")
                .queryParam("from_platform", "the_pltfm_from")
                .request()
                .post(Entity.json(PLATFORM_CONVERTER.toPlatform(platform)))
                .readEntity(Platform.class))
                .isEqualTo(PLATFORM_CONVERTER.toPlatform(platform));
    }

    @Test
    public void should_return_400_when_create_platform_if_from_application_is_not_valid_with_post()
            throws JsonProcessingException {
        PlatformData.IBuilder builder1 = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder1.build();

        PlatformKey fromPlatformKey = PlatformKey.withName("the_pltfm_from")
                .withApplicationName("the_app_from")
                .build();
        when(applications.createPlatformFromExistingPlatform(platform, fromPlatformKey)).thenReturn(platform);

        assertThat(withoutAuth("/applications/_we_dont_care_/platforms")
                .queryParam("from_application", "%20%09%00")
                .queryParam("from_platform", "the_pltfm_from")
                .request()
                .post(Entity.json(platform))
                .getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_return_400_when_create_platform_if_from_platform_is_not_valid_with_post()
            throws JsonProcessingException {
        PlatformKey fromPlatformKey = PlatformKey.withName("the_pltfm_from")
                .withApplicationName("the_app_from")
                .build();

        PlatformData.IBuilder builder1 = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder1.build();

        when(applications.createPlatformFromExistingPlatform(platform, fromPlatformKey)).thenReturn(platform);

        assertThat(withoutAuth("/applications/_we_dont_care_/platforms")
                .queryParam("from_application", "the_app_from")
                .queryParam("from_platform", "%20%09%00")
                .request()
                .post(Entity.json(platform))
                .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_return_400_when_get_properties_if_application_name_is_not_valid() {
        assertThat(
            withoutAuth("/applications/%20%09%00/platforms/dfdfdf/properties")
                    .queryParam("path", "#truc")
                    .request()
                    .get()
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_return_400_when_get_properties_if_platform_name_is_not_valid() {
        assertThat(
            withoutAuth("/applications/tryruytur/platforms/%20%09%00/properties")
                    .queryParam("path", "#truc")
                    .request()
                    .get()
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_return_400_when_get_properties_if_path_is_not_valid() {
        assertThat(
            withoutAuth("/applications/tryruytur/platforms/dfdfdf/properties")
                    .queryParam("path", "%20%09%00")
                    .request()
                    .get()
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_return_400_when_get_properties_instance_model_if_application_name_is_not_valid() {
        assertThat(
            withoutAuth("/applications/%20%09%00/platforms/dfdfdf/properties/instance_model")
                    .queryParam("path", "#truc")
                    .request()
                    .get()
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_return_400_when_get_properties_instance_model_if_platform_name_is_not_valid() {
        assertThat(
            withoutAuth("/applications/tryruytur/platforms/%20%09%00/properties/instance_model")
                    .queryParam("path", "#truc")
                    .request()
                    .get()
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_return_400_when_get_properties_instance_model_if_path_is_not_valid() {
        assertThat(
            withoutAuth("/applications/tryruytur/platforms/dfdfdf/properties/instance_model")
                    .queryParam("path", "%20%09%00")
                    .request()
                    .get()
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_return_400_when_post_properties_if_application_name_is_not_valid() {
        InstanceModel instanceModel = new InstanceModel(Sets.newHashSet());
        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();
        when(applications.getInstanceModel(platformKey, "some_path")).thenReturn(instanceModel);

        assertThat(
            withoutAuth("/applications/%20%09%00/platforms/pltfm_name/properties/instance_model")
                .queryParam("path", "some_path")
                .request()
                .get()
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_return_400_when_post_properties_if_platform_name_is_not_valid() {
        InstanceModel instanceModel = new InstanceModel(Sets.newHashSet());
        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();
        when(applications.getInstanceModel(platformKey, "some_path")).thenReturn(instanceModel);

        assertThat(
            withoutAuth("/applications/app_name/platforms/%20%09%00/properties/instance_model")
                    .queryParam("path", "some_path")
                    .request()
                    .get()
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_return_400_if_trying_to_save_properties_without_platform_name_valid() throws JsonProcessingException {
        Properties properties = new Properties(Sets.newHashSet(), Sets.newHashSet());

        assertThat(
            withoutAuth("/applications/my_app/platforms/%20%09%00/properties")
                    .queryParam("path", "some_path")
                    .queryParam("platform_vid", "1")
                    .request()
                    .post(Entity.json(properties))
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_return_400_if_trying_to_save_properties_without_application_name_valid() throws JsonProcessingException {
        Properties properties = new Properties(Sets.newHashSet(), Sets.newHashSet());

        assertThat(
            withoutAuth("/applications/%20%09%00/platforms/my_pltfm/properties")
                    .queryParam("path", "some_path")
                    .queryParam("platform_vid", "1")
                    .request()
                    .post(Entity.json(properties))
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_create_snapshot() throws JsonProcessingException {
        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();

        when(applications.takeSnapshot(platformKey)).thenReturn(1L);

        assertThat(
            withoutAuth("/applications/app_name/platforms/pltfm_name/take_snapshot")
                .request()
                .post(Entity.json(null))
                .getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void should_return_400_when_post_take_snapshot_if_platform_name_is_not_valid() throws JsonProcessingException {
        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();

        when(applications.takeSnapshot(platformKey)).thenReturn(1L);

        assertThat(
            withoutAuth("/applications/app_name/platforms/%20%09%00/take_snapshot")
                    .request()
                    .post(Entity.json(null))
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_return_400_when_post_take_snapshot_if_application_name_is_not_valid() throws JsonProcessingException {
        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();

        when(applications.takeSnapshot(platformKey)).thenReturn(1L);

        assertThat(
            withoutAuth("/applications/%20%09%00/platforms/pltfm_name/take_snapshot")
                    .request()
                    .post(Entity.json(null))
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_restore_snapshot() throws JsonProcessingException {
        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();

        PlatformData.IBuilder builder1 = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("1.0.0.0")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder1.build();

        when(applications.restoreSnapshot(platformKey, 1L)).thenReturn(platform);

        assertThat(withoutAuth("/applications/app_name/platforms/pltfm_name/restore_snapshot")
                .queryParam("timestamp", "1")
                .request()
                .post(Entity.json(null))
                .readEntity(Platform.class)
                ).isEqualTo(PLATFORM_CONVERTER.toPlatform(platform));
    }

    @Test
    public void should_return_400_when_post_restore_snapshot_if_platform_name_is_not_valid() throws JsonProcessingException {
        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();

        PlatformData.IBuilder builder1 = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("1.0.0.0")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder1.build();

        when(applications.restoreSnapshot(platformKey, 1L)).thenReturn(platform);

        assertThat(
            withoutAuth("/applications/app_name/platforms/%20%09%00/restore_snapshot")
                    .queryParam("timestamp", "1")
                    .request()
                    .post(Entity.json(null))
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_return_400_when_post_restore_snapshot_if_application_name_is_not_valid() throws JsonProcessingException {
        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();

        PlatformData.IBuilder builder1 = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("1.0.0.0")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder1.build();

        when(applications.restoreSnapshot(platformKey, 1L)).thenReturn(platform);

        assertThat(
            withoutAuth("/applications/%20%09%00/platforms/pltfm_name/restore_snapshot")
                    .queryParam("timestamp", "1")
                    .request()
                    .post(Entity.json(null))
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_snapshot() throws JsonProcessingException {
        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();

        List<Long> listInput = new ArrayList<>();
        listInput.add(1L);
        listInput.add(3L);
        listInput.add(2L);

        List<Integer> listOuput = new ArrayList<>();
        listOuput.add(3);
        listOuput.add(2);
        listOuput.add(1);

        when(applications.getSnapshots(platformKey)).thenReturn(listInput);

        assertThat(withoutAuth("/applications/app_name/platforms/pltfm_name/snapshots")
                .request()
                .get(new GenericType<List<Integer>>() {})).isEqualTo(listOuput);
    }

    @Test
    public void should_return_400_when_get_snapshot_if_platform_name_is_not_valid() throws JsonProcessingException {
        check_bad_request_on_get_without_auth("/applications/app_name/platforms/%20%09%00/snapshots");
    }

    @Test
    public void should_return_400_when_get_snapshot_if_application_name_is_not_valid() throws JsonProcessingException {
        check_bad_request_on_get_without_auth("/applications/%20%09%00/platforms/pltfm_name/snapshots");
    }

    @Test
    public void test_getGlobalPropertiesUsage() throws JsonProcessingException {
        ApplicationModuleData module1 = ApplicationModuleData
                .withApplicationName("demoKatana")
                .withVersion("1.0.0")
                .withPath("#DEMO#CMS")
                .withId(0)
                .withInstances(Sets.newHashSet())
                .setWorkingcopy(true)
                .build();

        ApplicationModuleData module2 = ApplicationModuleData
                .withApplicationName("demoKatana")
                .withVersion("1.0.0")
                .withPath("#DEMO#CMS")
                .withId(1)
                .withInstances(Sets.newHashSet())
                .setWorkingcopy(false)
                .build();

        ApplicationModuleData module3 = ApplicationModuleData
                .withApplicationName("demoGlobal")
                .withVersion("1.0.0")
                .withPath("#DEMO#NJS")
                .withId(2)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();


        PlatformKey platformKey1 = PlatformKey.withName("USN1")
                .withApplicationName("KTN")
                .build();


        PlatformData.IBuilder builder1 = PlatformData.withPlatformName(platformKey1.getName())
                .withApplicationName(platformKey1.getApplicationName())
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet(module1, module2, module3))
                .withVersion(1L)
                .isProduction();


        PlatformData pltfm1 = builder1.build();

        Set<KeyValueValorisationData> g_prop = new HashSet<>();
        Set<KeyValueValorisationData> prop_1 = new HashSet<>();
        Set<KeyValueValorisationData> prop_2 = new HashSet<>();
        Set<KeyValueValorisationData> prop_3 = new HashSet<>();


        Set<KeyValuePropertyModel> model_1 = new HashSet<>();
        Set<KeyValuePropertyModel> model_2 = new HashSet<>();
        Set<KeyValuePropertyModel> model_3 = new HashSet<>();

        g_prop.add(new KeyValueValorisationData("first_global", "first_global_value"));
        g_prop.add(new KeyValueValorisationData("second_global", "second_global_value"));
        g_prop.add(new KeyValueValorisationData("third_global", "third_global_value"));
        g_prop.add(new KeyValueValorisationData("whole_global", "whole_global_value"));
        g_prop.add(new KeyValueValorisationData("global", "global_value"));
        g_prop.add(new KeyValueValorisationData("unused", "unused_value"));

        prop_1.add(new KeyValueValorisationData("prop_11", "{{first_global}}"));
        prop_1.add(new KeyValueValorisationData("prop_12", "{{whole_global}}"));

        prop_2.add(new KeyValueValorisationData("prop_21", "{{second_global}}"));
        prop_2.add(new KeyValueValorisationData("prop_22", "{{whole_global}}"));

        prop_3.add(new KeyValueValorisationData("prop_31", "{{third_global}}"));
        prop_3.add(new KeyValueValorisationData("prop_32", "{{whole_global}}"));

        model_1.add(new KeyValuePropertyModel("global", ""));
        model_2.add(new KeyValuePropertyModel("global", ""));
        model_2.add(new KeyValuePropertyModel("prop_21", ""));

        PropertiesData global_properties = new PropertiesData(g_prop, Sets.newHashSet());
        PropertiesData properties_1 = new PropertiesData(prop_1, Sets.newHashSet());
        PropertiesData properties_2 = new PropertiesData(prop_2, Sets.newHashSet());
        PropertiesData properties_3 = new PropertiesData(prop_3, Sets.newHashSet());

        PlatformKey platformKey = PlatformKey.withName("USN1")
                .withApplicationName("KTN")
                .build();

        when(applications.getPlatform(any())).thenReturn(Optional.of(pltfm1));
        when(applications.getProperties(platformKey, "#")).thenReturn(global_properties);
        when(applications.getProperties(platformKey, module1.getPropertiesPath())).thenReturn(properties_1);
        when(applications.getProperties(platformKey, module2.getPropertiesPath())).thenReturn(properties_2);
        when(applications.getProperties(platformKey, module3.getPropertiesPath())).thenReturn(properties_3);

        Optional<HesperidesPropertiesModel> opt_1 = Optional.of(new HesperidesPropertiesModel(model_1, Sets.newHashSet()));
        Optional<HesperidesPropertiesModel> opt_2 = Optional.of(new HesperidesPropertiesModel(model_2, Sets.newHashSet()));
        Optional<HesperidesPropertiesModel> opt_3 = Optional.of(new HesperidesPropertiesModel(model_3, Sets.newHashSet()));

        when(modules.getModel(new ModuleKey(module2.getName(), Release.of(module2.getVersion())))).thenReturn(opt_2);
        when(modules.getModel(new ModuleKey(module1.getName(), WorkingCopy.of(module1.getVersion())))).thenReturn(opt_1);
        when(modules.getModel(new ModuleKey(module3.getName(), WorkingCopy.of(module3.getVersion())))).thenReturn(opt_3);


        HashMap<String, ArrayList<HashMap>> response = withoutAuth("/applications/KTN/platforms/USN1/global_properties_usage")
                .request()
                .get()
                .readEntity(new GenericType<HashMap<String, ArrayList<HashMap>>>() {});

        assertThat(response.get("first_global").size()).isEqualTo(1);
        assertThat(response.get("second_global").size()).isEqualTo(1);
        assertThat(response.get("third_global").size()).isEqualTo(1);
        assertThat(response.get("whole_global").size()).isEqualTo(3);
        assertThat(response.get("global").size()).isEqualTo(2);
        assertThat(response.get("unused").size()).isEqualTo(0);

        assertThat(response.get("first_global").get(0).get("path")).isEqualTo("#DEMO#CMS#demoKatana#1.0.0#WORKINGCOPY");
        assertThat(response.get("second_global").get(0).get("path")).isEqualTo("#DEMO#CMS#demoKatana#1.0.0#RELEASE");
        assertThat(response.get("third_global").get(0).get("path")).isEqualTo("#DEMO#NJS#demoGlobal#1.0.0#WORKINGCOPY");
        assertThat(response.get("global").get(1).get("path")).isEqualTo("#DEMO#CMS#demoKatana#1.0.0#RELEASE");
        assertThat(response.get("global").get(0).get("path")).isEqualTo("#DEMO#CMS#demoKatana#1.0.0#WORKINGCOPY");
        assertThat(response.get("global").get(0).get("inModel")).isEqualTo(true);
        assertThat(response.get("second_global").get(0).get("inModel")).isEqualTo(true);
        assertThat(response.get("third_global").get(0).get("inModel")).isEqualTo(false);
    }

    @Test
    public void no_npe_on_timeline_request_on_snapshot_events() {
        ApplicationModuleData module1 = ApplicationModuleData
                .withApplicationName("module1")
                .withVersion("version")
                .withPath("#path#1#the_module_name#the_module_version#WORKINGCOPY")
                .withId(0)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        final PlatformKey platformKey = PlatformKey.withName("a_pltfm")
                .withApplicationName("an_app")
                .build();

        PlatformData.IBuilder builder1 = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet(module1))
                .withVersion(1L)
                .isProduction();

        applicationsWithEvent.createPlatform(builder1.build());
        applicationsWithEvent.takeSnapshot(platformKey);
        Properties properties = new Properties(Sets.newHashSet(new KeyValueValorisation("key", "prop_{{instance_key}}")), Sets.newHashSet());
        applicationsWithEvent.createOrUpdatePropertiesInPlatform(platformKey,
                "#path#1#the_module_name#the_module_version#WORKINGCOPY",
                PROPERTIES_CONVERTER.toPropertiesData(properties), 1L, comment);
        PropertiesData propertiesDataOutput = applicationsWithEvent.getProperties(
                platformKey,
                "#path#1#the_module_name#the_module_version#WORKINGCOPY",
                System.currentTimeMillis());

        DefaultPropertiesConverter defaultPropertiesConverter = new DefaultPropertiesConverter();
        PropertiesData propertiesDataInput = defaultPropertiesConverter.toPropertiesData(properties);
        assertThat(propertiesDataInput.getKeyValueProperties()).isEqualTo(propertiesDataOutput.getKeyValueProperties());
    }
}

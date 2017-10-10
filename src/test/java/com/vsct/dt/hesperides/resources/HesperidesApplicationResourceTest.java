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
import com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyKey;
import com.vsct.dt.hesperides.templating.modules.Modules;
import com.vsct.dt.hesperides.templating.modules.template.TemplateData;
import com.vsct.dt.hesperides.templating.platform.*;
import com.vsct.dt.hesperides.util.HesperidesCacheConfiguration;
import com.vsct.dt.hesperides.util.HesperidesVersion;
import com.vsct.dt.hesperides.util.JedisMock;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tests.type.UnitTests;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by william_montaz on 01/09/14.
 */
@Category(UnitTests.class)
public class HesperidesApplicationResourceTest extends AbstractDisableUserResourcesTest {
    private static final ApplicationSearch applicationSearch = mock(ApplicationSearch.class);
    private static final ModuleSearch moduleSearch = mock(ModuleSearch.class);

    public static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    public static final PlatformConverter PLATFORM_CONVERTER = new DefaultPlatformConverter(new DefaultInstanceConverter());
    public static final TimeStampedPlatformConverter TIME_CONVERTER = new DefaultTimeStampedPlatformConverter(PLATFORM_CONVERTER);
    public static final ApplicationConverter APPLI_CONVERTER = new DefaultApplicationConverter(PLATFORM_CONVERTER);
    private static final PropertiesConverter PROPERTIES_CONVERTER = new DefaultPropertiesConverter();

    private final HesperidesModuleResource MODULE_RESOURCE = new HesperidesModuleResource(modulesWithEvent, moduleSearch);

    private final String comment = "Test comment";

    @Rule
    public ResourceTestRule simpleAuthResources = createSimpleAuthResource(
            new HesperidesApplicationResource(applicationsWithEvent, modulesWithEvent, applicationSearch, TIME_CONVERTER,
                    APPLI_CONVERTER, PROPERTIES_CONVERTER, MODULE_RESOURCE)
    );

    @Rule
    public ResourceTestRule disabledAuthResources  = createDisabledAuthResource(
            new HesperidesApplicationResource(applicationsWithEvent, modulesWithEvent, applicationSearch, TIME_CONVERTER,
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
    @Override
    public void setUp() throws Exception {
        super.setUp();
        reset(applicationSearch);
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

        PlatformKey ptfKey = new PlatformKey("my_name", "my_platform");

        generateApplication(ptfKey, 1);

        Application app = withoutAuth("/applications/my_name")
                .request()
                .get()
                .readEntity(Application.class);

        assertThat(application.getName()).isEqualTo(app.getName());
        assertThat(application.getPlatforms().size()).isEqualTo(app.getPlatforms().size());
    }

    @Test
    public void should_return_404_if_not_found() throws ESServiceException {
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

        generateApplication(platformKey, 1);

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

        generateApplication(platformKey, 1);

        assertThat(withoutAuth("/applications/app_name/platforms/pltfm_name")
                .request()
                .get()
                .readEntity(Platform.class))
                .isEqualTo(PLATFORM_CONVERTER.toPlatform(platform));
    }

    @Test
    public void should_return_404_if_platform_is_not_found() throws ESServiceException {
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

        generateApplication(fromPlatformKey, 1);

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

        generateApplication(fromPlatformKey, 1);

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

        generateApplication(fromPlatformKey, 1);

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

        generateApplication(fromPlatformKey, 1);

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

        generateApplication(fromPlatformKey, 1);

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

        generateApplication(fromPlatformKey, 1);

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

        generateApplication(fromPlatformKey, 1);

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
        PlatformData.IBuilder builder0 = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet())
                .withVersion(-1L)
                .isProduction();

        applicationsWithEvent.createPlatform(builder0.build());

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

        assertThat(withoutAuth("/applications/app_name/platforms")
                .request()
                .put(Entity.json(PLATFORM_CONVERTER.toPlatform(platform1)))
                .readEntity(Platform.class))
                .isEqualTo(PLATFORM_CONVERTER.toPlatform(platform2));
    }

    @Test
    public void should_update_platform_for_application_copying_properties_for_upgraded_modules_if_query_param_is_provided() throws Exception {
        PlatformData.IBuilder builder0 = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet())
                .withVersion(-1L)
                .isProduction();

        applicationsWithEvent.createPlatform(builder0.build());

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

        assertThat(withoutAuth("/applications/app_name/platforms")
                .queryParam("copyPropertiesForUpgradedModules", "true")
                .request()
                .put(Entity.json(PLATFORM_CONVERTER.toPlatform(platform1)))
                .readEntity(Platform.class))
                .isEqualTo(PLATFORM_CONVERTER.toPlatform(platform2));
    }

    @Test
    public void should_return_422_when_updating_plateform_with_empty_application_version() throws Exception {
        PlatformData.IBuilder builder0 = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet())
                .withVersion(-1L)
                .isProduction();

        applicationsWithEvent.createPlatform(builder0.build());

        PlatformData.IBuilder builder = PlatformData
                .withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder.build();

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

        final KeyValueValorisationData kv = new KeyValueValorisationData("prop", "val1");
        final Set<KeyValueValorisationData> keyValues = new HashSet<>();
        keyValues.add(kv);

        final InstanceData instance = InstanceData
                .withInstanceName("TOTO")
                .withKeyValue(keyValues)
                .build();

        final Set<InstanceData> instances = new HashSet<>();
        instances.add(instance);

        final ApplicationModuleData module = ApplicationModuleData
                .withApplicationName("app_name")
                .withVersion("app_version")
                .withPath("some_path")
                .withId(-1)
                .withInstances(instances)
                .build();

        final Set<ApplicationModuleData> modules = new HashSet<>();
        modules.add(module);

        PlatformData.IBuilder builder0 = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("app_version")
                .withModules(modules)
                .withVersion(-1L)
                .isProduction();

        applicationsWithEvent.createPlatform(builder0.build());

        when(applicationsWithEvent.getProperties(platformKey, "some_path")).thenReturn(properties);
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

        generateApplication(platformKey, 2);

        assertThat(withoutAuth("/applications/my_app/platforms/my_pltfm/properties")
                .queryParam("path", "some_path")
                .queryParam("platform_vid", "2")
                .queryParam("comment", "Test comment")
                .request()
                .post(Entity.json(properties))
                .readEntity(Properties.class))
                .isEqualTo(properties2);
    }

    @Test
    public void should_save_only_valued_properties_and_not_null_or_empty_ones() throws JsonProcessingException {
        final ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("module", "2.1.1.0");

        generateModule(moduleKey, 1);

        Set<KeyValueValorisation> keyValueProperties = Sets.newHashSet();
        keyValueProperties.add(new KeyValueValorisation("name1", "value"));
        keyValueProperties.add(new KeyValueValorisation("name2", ""));
        keyValueProperties.add(new KeyValueValorisation("name3", ""));
        keyValueProperties.add(new KeyValueValorisation("name5", "value"));

        Set<KeyValueValorisation> keyValuePropertiesCleaned = Sets.newHashSet();
        keyValuePropertiesCleaned.add(new KeyValueValorisation("name1", "value"));
        keyValuePropertiesCleaned.add(new KeyValueValorisation("name5", "value"));

        Properties properties = new Properties(keyValueProperties, Sets.newHashSet());

        PlatformKey platformKey = PlatformKey.withName("my_pltfm")
                .withApplicationName("my_app")
                .build();

        generateApplication(platformKey, 1);

        withoutAuth("/applications/my_app/platforms/my_pltfm/properties")
                .queryParam("path", "#TITI#TOTO#module#2.1.1.0#WORKINGCOPY")
                .queryParam("platform_vid", "1")
                .queryParam("comment", "Test comment")
                .request()
                .post(Entity.json(properties));

        final Properties propResult = withoutAuth("/applications/my_app/platforms/my_pltfm/properties")
                .queryParam("path", "#TITI#TOTO#module#2.1.1.0#WORKINGCOPY")
                .request()
                .get()
                .readEntity(Properties.class);

        final Set<KeyValueValorisation> kvResult = propResult.getKeyValueProperties();
        kvResult.removeAll(keyValuePropertiesCleaned);

        assertThat(kvResult.size()).isEqualTo(0);
    }

    @Test
    public void should_save_only_valued_iterable_properties_and_not_null_or_empty_ones() throws JsonProcessingException {
        final ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("module", "2.1.1.0");

        generateModule(moduleKey, 1);

        // Iterable properties
        IterableValorisation.IterableValorisationItem itemIterable2 = new IterableValorisation.IterableValorisationItem("blockOfProperties", Sets.newHashSet(new KeyValueValorisation("name3", "value"), new KeyValueValorisation("name0", "")));
        IterableValorisation iterableValorisation2 = new IterableValorisation("iterable2", Lists.newArrayList(itemIterable2));

        IterableValorisation.IterableValorisationItem item = new IterableValorisation.IterableValorisationItem("blockOfProperties", Sets.newHashSet(new KeyValueValorisation("name2", "value"), iterableValorisation2));
        IterableValorisation iterableValorisation = new IterableValorisation("iterable", Lists.newArrayList(item));

        // Properties
        Properties properties = new Properties(Sets.newHashSet(new KeyValueValorisation("simple1", "value"), new KeyValueValorisation("simple2", "")), Sets.newHashSet(iterableValorisation));

        // Cleaned properties
        Set<KeyValueValorisation> keyValuePropertiesCleaned = Sets.newHashSet();
        keyValuePropertiesCleaned.add(new KeyValueValorisation("simple1", "value"));

        IterableValorisation.IterableValorisationItem itemIterable2Cleaned = new IterableValorisation.IterableValorisationItem("blockOfProperties", Sets.newHashSet(new KeyValueValorisation("name3", "value")));
        IterableValorisation iterableValorisation2Cleaned = new IterableValorisation("iterable2", Lists.newArrayList(itemIterable2Cleaned));

        IterableValorisation.IterableValorisationItem itemCleaned = new IterableValorisation.IterableValorisationItem("blockOfProperties", Sets.newHashSet(new KeyValueValorisation("name2", "value"), iterableValorisation2Cleaned));
        Set<IterableValorisation> iterablePropertiesCleaned = Sets.newHashSet(new IterableValorisation("iterable", Lists.newArrayList(itemCleaned)));

        PlatformKey platformKey = PlatformKey.withName("my_pltfm")
                .withApplicationName("my_app")
                .build();

        generateApplication(platformKey, 1);

        Properties propResult = withoutAuth("/applications/my_app/platforms/my_pltfm/properties")
                .queryParam("path", "#TITI#TOTO#module#2.1.1.0#WORKINGCOPY")
                .queryParam("platform_vid", "1")
                .queryParam("comment", "Test comment")
                .request()
                .post(Entity.json(properties))
                .readEntity(Properties.class);

        final Set<KeyValueValorisation> kvResult = propResult.getKeyValueProperties();

        kvResult.removeAll(keyValuePropertiesCleaned);

        kvResult.stream().forEach(kv -> System.out.println(kv.getName() + " : " + kv.getValue()));

        assertThat(kvResult.size()).isEqualTo(0);

        final Set<IterableValorisation> itResult = propResult.getIterableProperties();

        itResult.stream().forEach(it -> System.out.println(it.getName()));

        itResult.removeAll(iterablePropertiesCleaned);

        assertThat(itResult.size()).isEqualTo(0);
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
        final ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("module", "2.1.1.0");

        // We must generate module with template
        generateModule(moduleKey, "{{prop1}}, {{prop2}}", 1);

        // Create platform
        final KeyValueValorisationData kv = new KeyValueValorisationData("prop", "val1");
        final Set<KeyValueValorisationData> keyValues = new HashSet<>();
        keyValues.add(kv);

        final InstanceData instance = InstanceData
                .withInstanceName("TOTO")
                .withKeyValue(keyValues)
                .build();

        final Set<InstanceData> instances = new HashSet<>();
        instances.add(instance);

        final ApplicationModuleData module = ApplicationModuleData
                .withApplicationName("app_name")
                .withVersion("app_version")
                .withPath("#TITI#TOTO#module#2.1.1.0#WORKINGCOPY")
                .withId(1)
                .withInstances(instances)
                .build();

        final Set<ApplicationModuleData> modules = new HashSet<>();
        modules.add(module);

        PlatformData.IBuilder builder0 = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("app_version")
                .withModules(modules)
                .withVersion(-1L)
                .isProduction();

        applicationsWithEvent.createPlatform(builder0.build());

        // Set properties to have  instance model
        Set<KeyValueValorisation> keyValueProperties = Sets.newHashSet();
        keyValueProperties.add(new KeyValueValorisation("prop1", "{{aaa}}"));
        keyValueProperties.add(new KeyValueValorisation("prop2", "{{bbb}}"));

        Properties properties = new Properties(keyValueProperties, Sets.newHashSet());

        withoutAuth("/applications/app_name/platforms/pltfm_name/properties")
                .queryParam("path", "#TITI#TOTO#module#2.1.1.0#WORKINGCOPY")
                .queryParam("platform_vid", "1")
                .queryParam("comment", "Test comment")
                .request()
                .post(Entity.json(properties));

        // Generate output list to compare
        final KeyValuePropertyModel kvPro1 = new KeyValuePropertyModel("aaa", "");
        final KeyValuePropertyModel kvPro2 = new KeyValuePropertyModel("bbb", "");

        final Set<KeyValuePropertyModel> listModel = new HashSet<>();
        listModel.add(kvPro1);
        listModel.add(kvPro2);

        final InstanceModel instanceModel = new InstanceModel(listModel);

        final InstanceModel instanceModelResult = withoutAuth("/applications/app_name/platforms/pltfm_name/properties/instance_model")
                .queryParam("path", "#TITI#TOTO#module#2.1.1.0#WORKINGCOPY")
                .request()
                .get(InstanceModel.class);

        final Set<KeyValuePropertyModel> keys = instanceModelResult.getKeys();

        keys.removeAll(instanceModel.getKeys());

        assertThat(keys.size()).isEqualTo(0);
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

        generateApplication(fromPlatformKey, 1);

        PlatformData.IBuilder builder1 = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder1.build();

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
        PlatformData.IBuilder builder1 = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder1.build();

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
        assertThat(
            withoutAuth("/applications/%20%09%00/platforms/pltfm_name/properties/instance_model")
                .queryParam("path", "some_path")
                .request()
                .get()
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_return_400_when_post_properties_if_platform_name_is_not_valid() {
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

        generateApplication(platformKey, 1);

        assertThat(
            withoutAuth("/applications/app_name/platforms/pltfm_name/take_snapshot")
                .request()
                .post(Entity.json(null))
                .getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void should_return_400_when_post_take_snapshot_if_platform_name_is_not_valid() throws JsonProcessingException {
        assertThat(
            withoutAuth("/applications/app_name/platforms/%20%09%00/take_snapshot")
                    .request()
                    .post(Entity.json(null))
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_return_400_when_post_take_snapshot_if_application_name_is_not_valid() throws JsonProcessingException {
        assertThat(
            withoutAuth("/applications/%20%09%00/platforms/pltfm_name/take_snapshot")
                    .request()
                    .post(Entity.json(null))
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_restore_snapshot() throws JsonProcessingException {
        should_create_snapshot();

        PlatformData.IBuilder builder1 = PlatformData.withPlatformName("pltfm_name")
                .withApplicationName("app_name")
                .withApplicationVersion("1.0.0.0")
                .withModules(Sets.newHashSet())
                .withVersion(1L)
                .isProduction();

        PlatformData platform = builder1.build();

        // Search timestamp in database
        final JedisMock jedis = this.poolRedis.getPool().getResource();

        // Search in redis key with pattern to get timestamp
        // snapshot-platform-app_name-pltfm_name-1507551413983
        final String patternKey = "snapshot-platform-app_name-pltfm_name-";
        final Set<String> listKeys = jedis.keys(patternKey + "*");

        final String theKey = listKeys.iterator().next();

        final String timestamp = theKey.substring(patternKey.length());

        assertThat(withoutAuth("/applications/app_name/platforms/pltfm_name/restore_snapshot")
                .queryParam("timestamp", timestamp)
                .request()
                .post(Entity.json(null))
                .readEntity(Platform.class)
                ).isEqualTo(PLATFORM_CONVERTER.toPlatform(platform));
    }

    @Test
    public void should_return_400_when_post_restore_snapshot_if_platform_name_is_not_valid() throws JsonProcessingException {
        assertThat(
            withoutAuth("/applications/app_name/platforms/%20%09%00/restore_snapshot")
                    .queryParam("timestamp", "1")
                    .request()
                    .post(Entity.json(null))
                    .getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void should_return_400_when_post_restore_snapshot_if_application_name_is_not_valid() throws JsonProcessingException {
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

        // Create platform
        generateApplication(platformKey, 1);

        // Generate 5 snpahsot
        for (int index = 0; index < 5; index++) {
            assertThat(
                    withoutAuth("/applications/app_name/platforms/pltfm_name/take_snapshot")
                            .request()
                            .post(Entity.json(null))
                            .getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        }

        // Get list of timestamp in database
        // Search timestamp in database
        final JedisMock jedis = this.poolRedis.getPool().getResource();

        // Search in redis key with pattern to get timestamp
        // snapshot-platform-app_name-pltfm_name-1507551413983
        final String patternKey = "snapshot-platform-app_name-pltfm_name-";
        final Set<String> listKeys = jedis.keys(patternKey + "*");

        final List<Long> listTimestamp = new ArrayList<>();
        String timestamp;

        for (String currentKeyName : listKeys) {
            timestamp = currentKeyName.substring(patternKey.length());

            listTimestamp.add(Long.valueOf(timestamp));
        }

        listTimestamp.sort((o1, o2) -> o1.compareTo(o2));

        Collections.reverse(listTimestamp);

        assertThat(withoutAuth("/applications/app_name/platforms/pltfm_name/snapshots")
                .request()
                .get(new GenericType<List<Long>>() {})).isEqualTo(listTimestamp);
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
        // Create module 1 to convert to module 2
        final ModuleWorkingCopyKey demoKatanaWc = new ModuleWorkingCopyKey("demoKatana", "1.0.0");
        generateModule(demoKatanaWc, "{{prop_21}} {{prop_22}} {{global}}", 2); // 2 to create template

        // Now create release module 1 to module 2
        modulesWithEvent.createRelease(demoKatanaWc, "1.0.0");

        // Update module 1
        TemplateData templateData = TemplateData.withTemplateName("nom du template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("{{prop_11}} {{prop_12}} {{global}}")
                .withRights(null)
                .build();
        modulesWithEvent.updateTemplateInWorkingCopy(demoKatanaWc, templateData);

        // Create module 3
        final ModuleWorkingCopyKey demoGlobalWc = new ModuleWorkingCopyKey("demoGlobal", "1.0.0");
        generateModule(demoGlobalWc, "{{prop_31}} {{prop_32}}", 1);

        // Create platform
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

        applicationsWithEvent.createPlatform(pltfm1);

        // Add properties for module 1
        Set<KeyValueValorisationData> prop_1 = new HashSet<>();
        prop_1.add(new KeyValueValorisationData("prop_11", "{{first_global}}"));
        prop_1.add(new KeyValueValorisationData("prop_12", "{{whole_global}}"));
        PropertiesData properties_1 = new PropertiesData(prop_1, Sets.newHashSet());

        applicationsWithEvent.createOrUpdatePropertiesInPlatform(platformKey1, "#DEMO#CMS#demoKatana#1.0.0#WORKINGCOPY", properties_1,
                1L, "Maj prop1");

        // Add properties for module 2
        Set<KeyValueValorisationData> prop_2 = new HashSet<>();
        prop_2.add(new KeyValueValorisationData("prop_21", "{{second_global}}"));
        prop_2.add(new KeyValueValorisationData("prop_22", "{{whole_global}}"));
        PropertiesData properties_2 = new PropertiesData(prop_2, Sets.newHashSet());

        applicationsWithEvent.createOrUpdatePropertiesInPlatform(platformKey1, "#DEMO#CMS#demoKatana#1.0.0#RELEASE", properties_2,
                2L, "Maj prop2");

        // Add properties for module 3
        Set<KeyValueValorisationData> prop_3 = new HashSet<>();
        prop_3.add(new KeyValueValorisationData("prop_31", "{{third_global}}"));
        prop_3.add(new KeyValueValorisationData("prop_32", "{{whole_global}}"));
        PropertiesData properties_3 = new PropertiesData(prop_3, Sets.newHashSet());

        applicationsWithEvent.createOrUpdatePropertiesInPlatform(platformKey1, "#DEMO#NJS#demoGlobal#1.0.0#WORKINGCOPY", properties_3,
                3L, "Maj prop3");

        // Add globals properties
        Set<KeyValueValorisationData> g_prop = new HashSet<>();
        g_prop.add(new KeyValueValorisationData("first_global", "first_global_value"));
        g_prop.add(new KeyValueValorisationData("second_global", "second_global_value"));
        g_prop.add(new KeyValueValorisationData("third_global", "third_global_value"));
        g_prop.add(new KeyValueValorisationData("whole_global", "whole_global_value"));
        g_prop.add(new KeyValueValorisationData("global", "global_value"));
        g_prop.add(new KeyValueValorisationData("unused", "unused_value"));
        PropertiesData global_properties = new PropertiesData(g_prop, Sets.newHashSet());

        applicationsWithEvent.createOrUpdatePropertiesInPlatform(platformKey1, "#", global_properties, 4L, "Maj prop globale");

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

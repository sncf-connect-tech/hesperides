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

package com.vsct.dt.hesperides.resources;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.TemplateContext;
import com.github.mustachejava.codes.DefaultCode;
import com.github.mustachejava.codes.ValueCode;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vsct.dt.hesperides.applications.*;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.exception.wrapper.IllegalArgumentExceptionMapper;
import com.vsct.dt.hesperides.exception.wrapper.*;
import com.vsct.dt.hesperides.files.Files;
import com.vsct.dt.hesperides.files.HesperidesFile;
import com.vsct.dt.hesperides.security.DisabledAuthProvider;
import com.vsct.dt.hesperides.security.SimpleAuthenticator;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import com.vsct.dt.hesperides.templating.models.KeyValuePropertyModel;
import com.vsct.dt.hesperides.templating.modules.ModuleKey;
import com.vsct.dt.hesperides.templating.modules.ModulesAggregate;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.packages.TemplatePackagesAggregate;
import com.vsct.dt.hesperides.templating.platform.*;
import com.vsct.dt.hesperides.util.HesperidesVersion;
import io.dropwizard.auth.basic.BasicAuthProvider;
import io.dropwizard.testing.junit.ResourceTestRule;
import tests.type.UnitTests;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.mockito.Mockito.*;

/**
 * Created by william_montaz on 01/09/14.
 */
/* AUTHENTICATION -> John_Doe:secret => Basic Sm9obl9Eb2U6c2VjcmV0 */
@Category(UnitTests.class)
public class HesperidesFilesResourceTest {

    private static final Files files = mock(Files.class);

    private static final ApplicationsAggregate applicationsAggregate = mock(ApplicationsAggregate.class);
    private static final ModulesAggregate modulesAggregate = mock(ModulesAggregate.class);
    private static final TemplatePackagesAggregate templatePackages = mock(TemplatePackagesAggregate.class);

    private static final HesperidesModuleResource modulesResource = new HesperidesModuleResource(modulesAggregate, null);
    private static final HesperidesPropertiesModel model = HesperidesPropertiesModel.empty();

    @ClassRule
    public static ResourceTestRule simpleAuthResources = ResourceTestRule.builder()
            .addProvider(new BasicAuthProvider<>(
                    new SimpleAuthenticator(),
                    "AUTHENTICATION_PROVIDER"))
            .addResource(new HesperidesFilesResource(files, modulesResource))
            .addProvider(new DefaultExceptionMapper())
            .addProvider(new DuplicateResourceExceptionMapper())
            .addProvider(new IncoherentVersionExceptionMapper())
            .addProvider(new OutOfDateVersionExceptionMapper())
            .addProvider(new MissingResourceExceptionMapper())
            .addProvider(new IllegalArgumentExceptionMapper())
            .build();

    @ClassRule
    public static ResourceTestRule disabledAuthResources = ResourceTestRule.builder()
            .addProvider(new DisabledAuthProvider())
            .addResource(new HesperidesFilesResource(files, modulesResource))
            .addProvider(new DefaultExceptionMapper())
            .addProvider(new DuplicateResourceExceptionMapper())
            .addProvider(new IncoherentVersionExceptionMapper())
            .addProvider(new OutOfDateVersionExceptionMapper())
            .addProvider(new MissingResourceExceptionMapper())
            .addProvider(new IllegalArgumentExceptionMapper())
            .build();


    public com.sun.jersey.api.client.WebResource withAuth(String url) {
        return simpleAuthResources.client().resource(url);
    }

    public com.sun.jersey.api.client.WebResource withoutAuth(String url) {
        return disabledAuthResources.client().resource(url);
    }


    @Before
    public void setup() {
        reset(files);
    }

    @Test
    public void should_return_fileListItems_with_correct_content_location_url() throws Exception {
        HesperidesFile file1 = new HesperidesFile("the_template_namespace", "the_template_name", "/some/location", "some_filename1.sh", null);
        HesperidesFile file2 = new HesperidesFile("the_template_namespace", "the_template_name", "/some/location", "some_filename2.sh", null);

        when(files.getLocations("my_app", "my_pltfm", "the_path", "my_module", "the_module_version", true, "my_instance", false)).thenReturn(Sets.newHashSet(file1, file2));

        FileListItem fileListItem1 = new FileListItem("/some/location/some_filename1.sh", "/rest/files/applications/my_app/platforms/my_pltfm/the_path/my_module/the_module_version/instances/my_instance/the_template_name?isWorkingCopy=true&template_namespace=the_template_namespace&simulate=false");
        FileListItem fileListItem2 = new FileListItem("/some/location/some_filename2.sh", "/rest/files/applications/my_app/platforms/my_pltfm/the_path/my_module/the_module_version/instances/my_instance/the_template_name?isWorkingCopy=true&template_namespace=the_template_namespace&simulate=false");

        assertThat(withoutAuth("/files/applications/my_app/platforms/my_pltfm/the_path/my_module/the_module_version/instances/my_instance")
                .queryParam("isWorkingCopy", "true")
                .queryParam("simulate", "false")
                .get(new GenericType<Set<FileListItem>>() {
                })).isEqualTo(Sets.newHashSet(fileListItem1, fileListItem2));
    }

    @Test
    public void should_escape_content_location_and_specifically_use_per_cent_20_for_empty_spaces_instead_of_plus_sign(){
        //Same as the one above, but use some special characters for url generation
        HesperidesFile file1 = new HesperidesFile("templates#techno#1.0#RELEASE", "name with spaces", "/some/location", "some_filename1.sh", null);

        when(files.getLocations("my app", "my pltfm", "the path", "my#module", "the#module#version", true, "my instance", false)).thenReturn(Sets.newHashSet(file1));

        FileListItem fileListItem1 = new FileListItem("/some/location/some_filename1.sh", "/rest/files/applications/my%20app/platforms/my%20pltfm/the%20path/my%23module/the%23module%23version/instances/my%20instance/name%20with%20spaces?isWorkingCopy=true&template_namespace=templates%23techno%231.0%23RELEASE&simulate=false");

        assertThat(withoutAuth("/files/applications/my%20app/platforms/my%20pltfm/the%20path/my%23module/the%23module%23version/instances/my%20instance")
                .queryParam("isWorkingCopy", "true")
                .queryParam("simulate", "false")
                .get(new GenericType<Set<FileListItem>>() {
                })).isEqualTo(Sets.newHashSet(fileListItem1));
    }

    @Test
    public void should_return_400_if_getting_file_list_and_query_param_is_working_copy_is_missing(){
        try {
            withoutAuth("/files/applications/my%20app/platforms/my%20pltfm/the%20path/my%23module/the%23module%23version/instances/my%20instance")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_401_if_getting_file_list_and_not_authenticated(){
        try {
            withAuth("/files/applications/my%20app/platforms/my%20pltfm/the%20path/my%23module/the%23module%23version/instances/my%20instance")
                    .get(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    @Test(expected = UniformInterfaceException.class)
    public void should_get_generated_file_for_application_platform_path_module_infos_instance_filename_with_isWorkingcopy_and_template_namespace_params() throws Exception {

        when(files.getFile("some_app", "some_pltfm", "a_given_path", "module_name", "module_version", true, "the_instance_name", "the_template_namespace", "the_filename", model, false)).thenReturn("Ze file content");

        assertThat(withoutAuth("/files/applications/some_app/platforms/some_pltfm/a_given_path/module_name/module_version/instances/the_instance_name/the_filename")
                .queryParam("isWorkingCopy", "true")
                .queryParam("template_namespace", "the_template_namespace")
                .get(String.class))
                .isEqualTo("Ze file content");
    }

    @Test
    public void should_return_400_if_getting_file_without_isWorkingcopy_query_param(){
        try {
            withoutAuth("/files/applications/some_app/platforms/some_pltm/a_given_path/module_name/module_version/instances/the_instance_name/the_filename")
                    .queryParam("template_namespace", "the_template_namespace")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_file_without_template_namespace_query_param(){
        try {
            withoutAuth("/files/applications/some_app/platforms/some_pltm/a_given_path/module_name/module_version/instances/the_instance_name/the_filename")
                    .queryParam("isWorkingCopy", "true")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_401_if_getting_file_and_not_authenticated(){
        try {
            withAuth("/files/applications/some_app/platforms/some_pltm/a_given_path/module_name/module_version/instances/the_instance_name/the_filename")
                    .queryParam("isWorkingCopy", "true")
                    .queryParam("template_namespace", "the_template_namespace")
                    .get(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_files_with_application_name_not_valid(){
        try {
            withoutAuth("/files/applications/%20%09%00/platforms/some_pltm/a_given_path/module_name/module_version/instances/the_instance_name")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_files_with_platform_name_not_valid(){
        try {
            withoutAuth("/files/applications/some_app/platforms/%20%09%00/a_given_path/module_name/module_version/instances/the_instance_name")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_files_with_path_not_valid(){
        try {
            withoutAuth("/files/applications/some_app/platforms/some_pltm/%20%09%00/module_name/module_version/instances/the_instance_name")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_files_with_module_name_not_valid(){
        try {
            withoutAuth("/files/applications/some_app/platforms/some_pltm/a_given_path/%20%09%00/module_version/instances/the_instance_name")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_files_with_module_version_not_valid(){
        try {
            withoutAuth("/files/applications/some_app/platforms/some_pltm/a_given_path/module_name/%20%09%00/instances/the_instance_name")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_files_with_instance_name_not_valid(){
        try {
            withoutAuth("/files/applications/some_app/platforms/some_pltm/a_given_path/module_name/module_version/instances/%20%09%00")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_one_file_with_application_name_not_valid(){
        try {
            withoutAuth("/files/applications/%20%09%00/platforms/some_pltm/a_given_path/module_name/module_version/instances/the_instance_name")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_one_file_with_platform_name_not_valid(){
        try {
            withoutAuth("/files/applications/some_app/platforms/%20%09%00/a_given_path/module_name/module_version/instances/the_instance_name/file_name")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_one_file_with_path_not_valid(){
        try {
            withoutAuth("/files/applications/some_app/platforms/some_pltm/%20%09%00/module_name/module_version/instances/the_instance_name/file_name")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_one_file_with_module_name_not_valid(){
        try {
            withoutAuth("/files/applications/some_app/platforms/some_pltm/a_given_path/%20%09%00/module_version/instances/the_instance_name/file_name")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_one_file_with_module_version_not_valid(){
        try {
            withoutAuth("/files/applications/some_app/platforms/some_pltm/a_given_path/module_name/%20%09%00/instances/the_instance_name/file_name")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_one_file_with_instance_name_not_valid(){
        try {
            withoutAuth("/files/applications/some_app/platforms/some_pltm/a_given_path/module_name/module_version/instances/%20%09%00/file_name")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_404_if_getting_file_with_required_property() throws NoSuchFieldException, IllegalAccessException {
        PlatformKey platformKey = PlatformKey.withName("CUR1")
                .withApplicationName("RAC")
                .build();

        // Appel 1
        String propertiesPath = "#WAS#EuronetWS#1.0.0.0#WORKINGCOPY";

        InstanceData instance = InstanceData.withInstanceName("TOTO")
                .withKeyValue(ImmutableSet.of())
                .build();

        ApplicationModuleData module = ApplicationModuleData.withApplicationName("EuronetWS")
                .withVersion("1.0.0.0")
                .withPath(propertiesPath)
                .withId(1)
                .withInstances(ImmutableSet.of(instance))
                .isWorkingcopy()
                .build();

        PlatformData platform = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(ImmutableSet.of(module))
                .withVersion(11L)
                .build();

        when(applicationsAggregate.getPlatform(platformKey)).thenReturn(Optional.of(platform));

        // Appel 2 getProperties()
        PropertiesData platformGlobalProperties = new PropertiesData(ImmutableSet.of(), ImmutableSet.of());

        when(applicationsAggregate.getProperties(platformKey, "#WAS#EuronetWS#1.0.0.0#WORKINGCOPY#EuronetWS#1.0.0.0#WORKINGCOPY")).thenReturn(platformGlobalProperties);
        when(applicationsAggregate.getProperties(platformKey, "#")).thenReturn(platformGlobalProperties);

        // Appel 3 modules.getTemplate()
        String templateName = "TitiEtRominet";

        Template template = new Template("modules#EuronetWS#1.0.0.0#WORKINGCOPY", templateName, "truc.txt",
                "/tmp", "prop1={{prop1|@required}}\n" +
                "prop2={{prop2|@default 'truc machin chose' @comment \"cool !\"}}", null, 2);

        KeyValuePropertyModel prop1 = new KeyValuePropertyModel(createProperty("prop1|@required"));
        KeyValuePropertyModel prop2 = new KeyValuePropertyModel(createProperty("prop2|@default 'truc machin chose' @comment \"cool !\""));

        ModuleKey moduleKey = new ModuleKey(
                "EuronetWS",
                new HesperidesVersion("1.0.0.0", true));

        when(modulesAggregate.getTemplate(moduleKey, templateName)).thenReturn(Optional.of(template));

        // Appel 4 modules.getModel)
        HesperidesPropertiesModel templateModel = new HesperidesPropertiesModel(ImmutableSet.of(prop1, prop2),
                ImmutableSet.of());

        when(modulesAggregate.getModel(moduleKey)).thenReturn(Optional.of(templateModel));
        when(applicationsAggregate.getSecuredProperties(any(), any(), any())).thenReturn(platformGlobalProperties);

        Files hesperidesFiles = new Files(applicationsAggregate, modulesAggregate, templatePackages);

        try {
            hesperidesFiles.getFile(
                    platformKey.getApplicationName(),
                    platformKey.getName(),
                    propertiesPath,
                    moduleKey.getName(),
                    module.getVersion(),
                    module.isWorkingCopy(),
                    instance.getName(),
                    template.getNamespace(),
                    template.getName(), model, false);
            fail("An error must be occure");
        } catch (MissingResourceException e) {
            assertThat(e.getMessage()).isEqualTo(String.format("Property 'prop1' in template '%s/%s' must be set.", template.getNamespace(), template.getName()));
        }

    }

    @Test
    public void should_return_404_if_getting_file_with_default_property() throws NoSuchFieldException, IllegalAccessException {
        PlatformKey platformKey = PlatformKey.withName("CUR1")
                .withApplicationName("RAC")
                .build();

        // Appel 1
        String propertiesPath = "#WAS#EuronetWS#1.0.0.0#WORKINGCOPY";

        InstanceData instance = InstanceData.withInstanceName("TOTO")
                .withKeyValue(ImmutableSet.of())
                .build();

        ApplicationModuleData module = ApplicationModuleData.withApplicationName("EuronetWS")
                .withVersion("1.0.0.0")
                .withPath(propertiesPath)
                .withId(1)
                .withInstances(ImmutableSet.of(instance))
                .isWorkingcopy()
                .build();

        PlatformData platform = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(ImmutableSet.of(module))
                .withVersion(11L)
                .build();

        when(applicationsAggregate.getPlatform(platformKey)).thenReturn(Optional.of(platform));

        // Appel 2 getProperties()
        PropertiesData platformGlobalProperties = new PropertiesData(ImmutableSet.of(), ImmutableSet.of());

        when(applicationsAggregate.getProperties(platformKey, "#WAS#EuronetWS#1.0.0.0#WORKINGCOPY#EuronetWS#1.0.0.0#WORKINGCOPY")).thenReturn(platformGlobalProperties);
        when(applicationsAggregate.getProperties(platformKey, "#")).thenReturn(platformGlobalProperties);

        when(applicationsAggregate.getSecuredProperties(platformKey, "#WAS#EuronetWS#1.0.0.0#WORKINGCOPY#EuronetWS#1.0.0.0#WORKINGCOPY", model)).thenReturn(platformGlobalProperties);
        when(applicationsAggregate.getSecuredProperties(platformKey, "#", model)).thenReturn(platformGlobalProperties);

        // Appel 3 modules.getTemplate()
        String templateName = "TitiEtRominet";

        Template template = new Template("modules#EuronetWS#1.0.0.0#WORKINGCOPY", templateName, "truc.txt",
                "/tmp", "prop1={{prop1|@required}}\n" +
                "prop2={{prop2|@default 'truc machin chose' @comment \"cool !\"}}", null, 2);

        KeyValuePropertyModel prop1 = new KeyValuePropertyModel(createProperty("prop1|@comment 'commentaire'"));
        KeyValuePropertyModel prop2 = new KeyValuePropertyModel(createProperty("prop2|@default 'truc machin chose' @comment \"cool !\""));

        ModuleKey moduleKey = new ModuleKey(
                "EuronetWS",
                new HesperidesVersion("1.0.0.0", true));

        when(modulesAggregate.getTemplate(moduleKey, templateName)).thenReturn(Optional.of(template));

        // Appel 4 modules.getModel)
        HesperidesPropertiesModel templateModel = new HesperidesPropertiesModel(ImmutableSet.of(prop1, prop2),
                ImmutableSet.of());

        when(modulesAggregate.getModel(moduleKey)).thenReturn(Optional.of(templateModel));

        Files hesperidesFiles = new Files(applicationsAggregate, modulesAggregate, templatePackages);


        String content = hesperidesFiles.getFile(
                platformKey.getApplicationName(),
                platformKey.getName(),
                propertiesPath,
                moduleKey.getName(),
                module.getVersion(),
                module.isWorkingCopy(),
                instance.getName(),
                template.getNamespace(),
                template.getName(), model, false);

        assertThat(content).isEqualTo("prop1=\nprop2=truc machin chose");
    }

    @Test
    public void should_get_generated_file_with_empty_instance_valuation() throws Exception {

        PlatformKey platformKey = PlatformKey.withName("CUR1")
                .withApplicationName("RAC")
                .build();

        // Appel 1
        String propertiesPath = "#WAS#EuronetWS#1.0.0.0#WORKINGCOPY";

        InstanceData instance = InstanceData.withInstanceName("TOTO")
                .withKeyValue(ImmutableSet.of())
                .build();

        ApplicationModuleData module = ApplicationModuleData.withApplicationName("EuronetWS")
                .withVersion("1.0.0.0")
                .withPath(propertiesPath)
                .withId(1)
                .withInstances(ImmutableSet.of(instance))
                .isWorkingcopy()
                .build();

        PlatformData platform = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(ImmutableSet.of(module))
                .withVersion(11L)
                .build();

        when(applicationsAggregate.getPlatform(platformKey)).thenReturn(Optional.of(platform));


        Set<KeyValueValorisationData> prop = new HashSet<>();

        prop.add(new KeyValueValorisationData("put_value_here","this_is_working"));
        prop.add(new KeyValueValorisationData("instance_prop","{{instance_var}}"));

        // Appel 2 getProperties()
        PropertiesData platformGlobalProperties = new PropertiesData(prop, ImmutableSet.of());

        when(applicationsAggregate.getProperties(platformKey, "#WAS#EuronetWS#1.0.0.0#WORKINGCOPY#EuronetWS#1.0.0.0#WORKINGCOPY")).thenReturn(platformGlobalProperties);
        when(applicationsAggregate.getProperties(platformKey, "#")).thenReturn(platformGlobalProperties);

        when(applicationsAggregate.getSecuredProperties(platformKey, "#WAS#EuronetWS#1.0.0.0#WORKINGCOPY#EuronetWS#1.0.0.0#WORKINGCOPY", model)).thenReturn(platformGlobalProperties);
        when(applicationsAggregate.getSecuredProperties(platformKey, "#", model)).thenReturn(platformGlobalProperties);

        // Appel 3 modules.getTemplate()
        String templateName = "TitiEtRominet";

        Template template = new Template("modules#EuronetWS#1.0.0.0#WORKINGCOPY", templateName, "truc.txt",
                "/tmp", "test_instance=[{{instance_prop}}][{{put_value_here}}]", null, 2);

        KeyValuePropertyModel prop1 = new KeyValuePropertyModel(createProperty("instance_prop"));
        KeyValuePropertyModel prop2 = new KeyValuePropertyModel(createProperty("put_value_here"));

        ModuleKey moduleKey = new ModuleKey(
                "EuronetWS",
                new HesperidesVersion("1.0.0.0", true));

        when(modulesAggregate.getTemplate(moduleKey, templateName)).thenReturn(Optional.of(template));

        // Appel 4 modules.getModel)
        HesperidesPropertiesModel templateModel = new HesperidesPropertiesModel(ImmutableSet.of(prop1, prop2),
                ImmutableSet.of());

        when(modulesAggregate.getModel(moduleKey)).thenReturn(Optional.of(templateModel));

        Files hesperidesFiles = new Files(applicationsAggregate, modulesAggregate, templatePackages);

        String content = hesperidesFiles.getFile(
                platformKey.getApplicationName(),
                platformKey.getName(),
                propertiesPath,
                moduleKey.getName(),
                module.getVersion(),
                module.isWorkingCopy(),
                instance.getName(),
                template.getNamespace(),
                template.getName(), model, false);

        assertThat(content).isEqualTo("test_instance=[][this_is_working]");
    }

    @Test
    public void should_return_404_if_instance_name_wrong() throws NoSuchFieldException, IllegalAccessException {
        PlatformKey platformKey = PlatformKey.withName("CUR1")
                .withApplicationName("RAC")
                .build();

        // Appel 1
        String propertiesPath = "#WAS#EuronetWS#1.0.0.0#WORKINGCOPY";

        InstanceData instance = InstanceData.withInstanceName("TOTO")
                .withKeyValue(ImmutableSet.of())
                .build();

        ApplicationModuleData module = ApplicationModuleData.withApplicationName("EuronetWS")
                .withVersion("1.0.0.0")
                .withPath(propertiesPath)
                .withId(1)
                .withInstances(ImmutableSet.of(instance))
                .isWorkingcopy()
                .build();

        PlatformData platform = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(ImmutableSet.of(module))
                .withVersion(11L)
                .build();

        when(applicationsAggregate.getPlatform(platformKey)).thenReturn(Optional.of(platform));

        // Appel 2 getProperties()
        PropertiesData platformGlobalProperties = new PropertiesData(ImmutableSet.of(), ImmutableSet.of());

        when(applicationsAggregate.getProperties(platformKey, "#WAS#EuronetWS#1.0.0.0#WORKINGCOPY#EuronetWS#1.0.0.0#WORKINGCOPY")).thenReturn(platformGlobalProperties);
        when(applicationsAggregate.getProperties(platformKey, "#")).thenReturn(platformGlobalProperties);

        when(applicationsAggregate.getSecuredProperties(platformKey, "#WAS#EuronetWS#1.0.0.0#WORKINGCOPY#EuronetWS#1.0.0.0#WORKINGCOPY", model)).thenReturn(platformGlobalProperties);
        when(applicationsAggregate.getSecuredProperties(platformKey, "#", model)).thenReturn(platformGlobalProperties);

        // Appel 3 modules.getTemplate()
        String templateName = "TitiEtRominet";

        Template template = new Template("modules#EuronetWS#1.0.0.0#WORKINGCOPY", templateName, "truc.txt",
                "/tmp", "prop1={{prop1|@required}}\n" +
                "prop2={{prop2|@default 'truc machin chose' @comment \"cool !\"}}", null, 2);

        ModuleKey moduleKey = new ModuleKey(
                "EuronetWS",
                new HesperidesVersion("1.0.0.0", true));

        when(modulesAggregate.getTemplate(moduleKey, templateName)).thenReturn(Optional.of(template));

        // Appel 4 modules.getModel)
        HesperidesPropertiesModel templateModel = new HesperidesPropertiesModel(ImmutableSet.of(), ImmutableSet.of());

        when(modulesAggregate.getModel(moduleKey)).thenReturn(Optional.of(templateModel));

        Files hesperidesFiles = new Files(applicationsAggregate, modulesAggregate, templatePackages);


        try {
            hesperidesFiles.getFile(
                    platformKey.getApplicationName(),
                    platformKey.getName(),
                    propertiesPath,
                    moduleKey.getName(),
                    module.getVersion(),
                    module.isWorkingCopy(),
                    "AYA",
                    template.getNamespace(),
                    template.getName(), model, false);
            fail("An error must be occure");
        } catch (MissingResourceException e) {
            assertThat(e.getMessage()).isEqualTo(String.format("There is no instance AYA in platform %s/%s", platformKey.getApplicationName(), platformKey.getName()));
        }
    }

    @Test
    public void should_not_return_404_if_instance_name_wrong_and_simulate_true() throws NoSuchFieldException, IllegalAccessException {
        PlatformKey platformKey = PlatformKey.withName("CUR1")
                .withApplicationName("RAC")
                .build();

        // Appel 1
        String propertiesPath = "#WAS#EuronetWS#1.0.0.0#WORKINGCOPY";

        InstanceData instance = InstanceData.withInstanceName("TOTO")
                .withKeyValue(ImmutableSet.of())
                .build();

        ApplicationModuleData module = ApplicationModuleData.withApplicationName("EuronetWS")
                .withVersion("1.0.0.0")
                .withPath(propertiesPath)
                .withId(1)
                .withInstances(ImmutableSet.of(instance))
                .isWorkingcopy()
                .build();

        PlatformData platform = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(ImmutableSet.of(module))
                .withVersion(11L)
                .build();

        when(applicationsAggregate.getPlatform(platformKey)).thenReturn(Optional.of(platform));

        // Appel 2 getProperties()
        PropertiesData platformGlobalProperties = new PropertiesData(ImmutableSet.of(), ImmutableSet.of());

        when(applicationsAggregate.getProperties(platformKey, "#WAS#EuronetWS#1.0.0.0#WORKINGCOPY#EuronetWS#1.0.0.0#WORKINGCOPY")).thenReturn(platformGlobalProperties);
        when(applicationsAggregate.getProperties(platformKey, "#")).thenReturn(platformGlobalProperties);

        when(applicationsAggregate.getSecuredProperties(platformKey, "#WAS#EuronetWS#1.0.0.0#WORKINGCOPY#EuronetWS#1.0.0.0#WORKINGCOPY", model)).thenReturn(platformGlobalProperties);
        when(applicationsAggregate.getSecuredProperties(platformKey, "#", model)).thenReturn(platformGlobalProperties);

        // Appel 3 modules.getTemplate()
        String templateName = "TitiEtRominet";

        Template template = new Template("modules#EuronetWS#1.0.0.0#WORKINGCOPY", templateName, "truc.txt",
                "/tmp", "{{hesperides.instance.name}}", null, 2);

        ModuleKey moduleKey = new ModuleKey(
                "EuronetWS",
                new HesperidesVersion("1.0.0.0", true));

        when(modulesAggregate.getTemplate(moduleKey, templateName)).thenReturn(Optional.of(template));

        // Appel 4 modules.getModel)
        HesperidesPropertiesModel templateModel = new HesperidesPropertiesModel(ImmutableSet.of(), ImmutableSet.of());

        when(modulesAggregate.getModel(moduleKey)).thenReturn(Optional.of(templateModel));

        Files hesperidesFiles = new Files(applicationsAggregate, modulesAggregate, templatePackages);

        String simulateInstanceName = "test_hesperides_instance_name_in_fake_instance";

        String content = hesperidesFiles.getFile(
                platformKey.getApplicationName(),
                platformKey.getName(),
                propertiesPath,
                moduleKey.getName(),
                module.getVersion(),
                module.isWorkingCopy(),
                simulateInstanceName,
                template.getNamespace(),
                template.getName(), model, true);
        assertThat(content).isEqualTo(simulateInstanceName);
    }

    private static final ValueCode createProperty(final String value) throws NoSuchFieldException, IllegalAccessException {
        Field f = DefaultCode.class.getDeclaredField("name");
        f.setAccessible(true);

        ValueCode code = new ValueCode(
                new TemplateContext("", "", "toto", 0, true),
                new DefaultMustacheFactory(), "???", false);
        f.set(code, value);

        return code;
    }
}

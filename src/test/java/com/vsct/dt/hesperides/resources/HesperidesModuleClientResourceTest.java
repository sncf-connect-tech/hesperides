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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.vsct.dt.hesperides.exception.runtime.DuplicateResourceException;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.exception.wrapper.*;
import com.vsct.dt.hesperides.indexation.search.ModuleSearch;
import com.vsct.dt.hesperides.indexation.search.ModuleSearchResponse;
import com.vsct.dt.hesperides.security.DisabledAuthProvider;
import com.vsct.dt.hesperides.security.SimpleAuthenticator;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import com.vsct.dt.hesperides.templating.modules.Module;
import com.vsct.dt.hesperides.templating.modules.ModuleKey;
import com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyKey;
import com.vsct.dt.hesperides.templating.modules.Modules;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateData;
import com.vsct.dt.hesperides.util.Release;
import com.vsct.dt.hesperides.util.WorkingCopy;
import io.dropwizard.auth.basic.BasicAuthProvider;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import tests.type.UnitTests;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Created by william_montaz on 01/09/14.
 */
/* AUTHENTICATION -> John_Doe:secret => Basic Sm9obl9Eb2U6c2VjcmV0 */
@Category(UnitTests.class)
public class HesperidesModuleClientResourceTest {

    private static final Modules      modules      = mock(Modules.class);
    private static final ModuleSearch moduleSearch = mock(ModuleSearch.class);

    public static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @ClassRule
    public static ResourceTestRule simpleAuthResources = ResourceTestRule.builder()
            .addProvider(new BasicAuthProvider<>(
                    new SimpleAuthenticator(),
                    "AUTHENTICATION_PROVIDER"))
            .addResource(new HesperidesModuleResource(modules, moduleSearch))
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
            .addResource(new HesperidesModuleResource(modules, moduleSearch))
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
        reset(modules);
        reset(moduleSearch);
    }

    /**
     * Tests for getModuleNames
     */

    @Test
    public void should_return_modulename_list() {
        Module module = new Module("module_name", "module_version", false, Sets.newHashSet(), 1L);
        ModuleKey.withModuleName("module_name")
                .withVersion(Release.of("module_version"))
                .build();

        Module[] moduleArray = {module};
        when(modules.getAllModules()).thenReturn(Arrays.asList(moduleArray));

        String[] awaitedResponseList = {"module_name"};

        assert(Arrays.equals(
                withoutAuth("/modules").get(List.class).toArray(), awaitedResponseList));
    }

    /**
     * Tests for getModuleVersions
     */

    @Test
    public void should_return_moduleversion_list() {
        Module module = new Module("module_name", "module_version", false, Sets.newHashSet(), 1L);
        ModuleKey.withModuleName("module_name")
                .withVersion(Release.of("module_version"))
                .build();

        Module[] moduleArray = {module};
        when(modules.getAllModules()).thenReturn(Arrays.asList(moduleArray));

        String[] awaitedResponseList = {"module_version"};

        assert(Arrays.equals(
                withoutAuth("/modules/module_name").get(List.class).toArray(), awaitedResponseList));
    }

    /**
     * Tests for getModuleTypes_list
     */

    @Test
    public void should_return_moduletype_list() {
        Module module = new Module("module_name", "module_version", false, Sets.newHashSet(), 1L);
        ModuleKey.withModuleName("module_name")
                .withVersion(Release.of("module_version"))
                .build();
        Module[] moduleArray = {module};
        when(modules.getAllModules()).thenReturn(Arrays.asList(moduleArray));

        String[] awaitedResponseList = {Release.LC};

        assert(Arrays.equals(
                withoutAuth("/modules/module_name/module_version").get(List.class).toArray(), awaitedResponseList));
    }

    /**
     * Tests for searchPackages
     * ----- Useless test due to change of module search format
     * ----- cf -> should_perform_search_on_term_separated_with_space_character
     */

    @Test
    @Ignore
    public void should_perform_search_on_term_separated_with_diese_character() {
        ModuleSearchResponse moduleSearchResponse1 = new ModuleSearchResponse("module_name1", "module_version", true);
        ModuleSearchResponse moduleSearchResponse2 = new ModuleSearchResponse("module_name2", "module_version", true);

        Module module1 = new Module("module_name1", "module_version", true, Sets.newHashSet(), 1L);
        Module module2 = new Module("module_name2", "module_version", true, Sets.newHashSet(), 1L);

        when(moduleSearch.getModulesByNameAndVersionLike(new String[]{"*term1*", "*term2*", "*term3*"})).thenReturn(Lists.newArrayList(moduleSearchResponse1, moduleSearchResponse2));

        ModuleKey moduleInfo1 = ModuleKey.withModuleName("module_name1")
                .withVersion(WorkingCopy.of("module_version"))
                .build();
        ModuleKey moduleInfo2 = ModuleKey.withModuleName("module_name2")
                .withVersion(WorkingCopy.of("module_version"))
                .build();
        when(modules.getModule(moduleInfo1)).thenReturn(Optional.of(module1));
        when(modules.getModule(moduleInfo2)).thenReturn(Optional.of(module2));

        assertThat(withoutAuth("/modules/perform_search")
                .queryParam("terms", "term1#term2#term3")
                .post(new GenericType<List<Module>>() {
                })).isEqualTo(Lists.newArrayList(module1, module2));
    }

    /**
     * Tests for searchPackages
     */

    @Test
    public void should_perform_search_on_term_separated_with_space_character() {
        ModuleSearchResponse moduleSearchResponse1 = new ModuleSearchResponse("module_name1", "module_version", true);
        ModuleSearchResponse moduleSearchResponse2 = new ModuleSearchResponse("module_name2", "module_version", true);

        Module module1 = new Module("module_name1", "module_version", true, Sets.newHashSet(), 1L);
        Module module2 = new Module("module_name2", "module_version", true, Sets.newHashSet(), 1L);

        when(moduleSearch.getModulesByNameAndVersionLike(new String[]{"module_name1", "module_version"})).thenReturn(Lists.newArrayList(moduleSearchResponse1));

        ModuleKey moduleInfo1 = ModuleKey.withModuleName("module_name1")
                .withVersion(WorkingCopy.of("module_version"))
                .build();
        ModuleKey moduleInfo2 = ModuleKey.withModuleName("module_name2")
                .withVersion(WorkingCopy.of("module_version"))
                .build();
        when(modules.getModule(moduleInfo1)).thenReturn(Optional.of(module1));
        when(modules.getModule(moduleInfo2)).thenReturn(Optional.of(module2));

        assertThat(withoutAuth("/modules/perform_search")
                .queryParam("terms", "module_name1 module_version")
                .post(new GenericType<List<Module>>() {
                })).isEqualTo(Lists.newArrayList(module1));
    }


    @Test
    public void should_return_400_if_terms_query_param_is_missing() {
        try {
            withoutAuth("/modules/perform_search")
                    .post(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_401_if_perform_search_and_not_authenticated() {
        try {
            withAuth("/modules/perform_search")
                    .post(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    /**
     * Test for getAllTemplatesInWorkingCopy
     */

    @Test
    public void should_return_list_of_all_templates_in_working_copy() {
        Template template1 = new Template(
                "modules#module_name#module_version#WORKINGCOPY",
                "name1",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        Template template2 = new Template(
                "modules#module_name#module_version#WORKINGCOPY",
                "name2",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        TemplateListItem templateListItem1 = new TemplateListItem(template1);
        TemplateListItem templateListItem2 = new TemplateListItem(template2);

        ModuleKey moduleInfo = ModuleKey.withModuleName("module_name")
                .withVersion(WorkingCopy.of("module_version"))
                .build();
        when(modules.getAllTemplates(moduleInfo)).thenReturn(Lists.newArrayList(template1, template2));

        assertThat(withoutAuth("/modules/module_name/module_version/workingcopy/templates")
                .get(new GenericType<List<TemplateListItem>>() {
                })).isEqualTo(Lists.newArrayList(templateListItem1, templateListItem2));
    }

    @Test
    public void should_return_401_if_getting_list_of_all_templates_in_working_copy_and_not_authenticated() {
        try {
            withAuth("/modules/module_name/module_version/workingcopy/templates")
                    .post(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    /**
     * Tests for getAllTemplatesInRelease
     */

    @Test
    public void should_return_list_of_all_templates_in_release() {
        Template template1 = new Template(
                "modules#module_name#module_version#RELEASE",
                "name1",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        Template template2 = new Template(
                "modules#module_name#module_version#RELEASE",
                "name2",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        TemplateListItem templateListItem1 = new TemplateListItem(template1);
        TemplateListItem templateListItem2 = new TemplateListItem(template2);

        ModuleKey moduleInfo = ModuleKey.withModuleName("module_name")
                .withVersion(Release.of("module_version"))
                .build();
        when(modules.getAllTemplates(moduleInfo)).thenReturn(Lists.newArrayList(template1, template2));

        assertThat(withoutAuth("/modules/module_name/module_version/release/templates")
                .get(new GenericType<List<TemplateListItem>>() {
                })).isEqualTo(Lists.newArrayList(templateListItem1, templateListItem2));
    }

    @Test
    public void should_return_401_if_getting_list_of_all_templates_in_release_and_not_authenticated() {
        try {
            withAuth("/modules/module_name/module_version/release/templates")
                    .get(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    /**
     * Tests for getTemplateInWorkingCopy
     */

    @Test
    public void should_return_template_from_workingcopy() {
        Template template = new Template(
                "modules#module_name#module_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        ModuleKey moduleInfo = ModuleKey.withModuleName("module_name")
                .withVersion(WorkingCopy.of("module_version"))
                .build();
        when(modules.getTemplate(moduleInfo, "template_name")).thenReturn(Optional.of(template));

        assertThat(withoutAuth("/modules/module_name/module_version/workingcopy/templates/template_name")
                .get(Template.class)).isEqualTo(template);
    }

    @Test
    public void should_return_404_not_found_if_template_missing() {
        ModuleKey moduleInfo = ModuleKey.withModuleName("module_name")
                .withVersion(WorkingCopy.of("module_version"))
                .build();
        when(modules.getTemplate(moduleInfo, "unknown")).thenReturn(Optional.empty());

        try {
            withoutAuth("/modules/module_name/module_version/workingcopy/templates/unknown").get(Response.class);
            fail("Ne renvoie pas le status 404");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        }
    }

    @Test
    public void should_return_401_if_getting_template_from_working_copy_and_not_authenticated() {
        try {
            withAuth("/modules/module_name/module_version/workingcopy/templates/unknown")
                    .get(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    /**
     * Tests for getTemplateInRelease
     */

    @Test
    public void should_return_template_from_release() {
        Template template = new Template(
                "modules#module_name#module_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        ModuleKey moduleInfo = ModuleKey.withModuleName("module_name")
                .withVersion(Release.of("module_version"))
                .build();
        when(modules.getTemplate(moduleInfo, "name")).thenReturn(Optional.of(template));

        assertThat(withoutAuth("/modules/module_name/module_version/release/templates/name")
                .get(Template.class)).isEqualTo(template);
    }

    @Test
    public void should_return_404_not_found_if_template_missing_in_release() {
        ModuleKey moduleInfo = ModuleKey.withModuleName("module_name")
                .withVersion(Release.of("module_version"))
                .build();
        when(modules.getTemplate(moduleInfo, "unknown")).thenReturn(Optional.empty());

        try {
            withoutAuth("/modules/module_name/module_version/release/templates/unknown").get(Response.class);
            fail("Ne renvoie pas le status 404");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        }
    }

    @Test
    public void should_return_401_if_getting_template_from_release_and_not_authenticated() {
        try {
            withAuth("/modules/module_name/module_version/release/templates/unknown")
                    .get(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    /**
     * Tests for getReleaseModel
     */

    @Test
    public void should_return_properties_model_for_module_released() {
        HesperidesPropertiesModel model = new HesperidesPropertiesModel(Sets.newHashSet(), Sets.newHashSet());

        when(modules.getModel(new ModuleKey("module_name", Release.of("module_version")))).thenReturn(Optional.of(model));

        assertThat(withoutAuth("/modules/module_name/module_version/release/model")
                .get(HesperidesPropertiesModel.class))
                .isEqualTo(model);
    }

    @Test
    public void should_return_401_if_getting_properties_model_for_release_and_not_authenticated() {
        try {
            withAuth("/modules/module_name/module_version/release/model")
                    .get(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    @Test
    public void should_return_404_if_getting_release_model_and_module_is_missing() {
        ModuleKey moduleInfo = ModuleKey.withModuleName("module_name")
                .withVersion(Release.of("module_version"))
                .build();
        when(modules.getModel(moduleInfo)).thenReturn(Optional.empty());
        try {
            withoutAuth("/modules/module_name/module_version/release/model").get(Response.class);
            fail("Ne renvoie pas le status 404");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        }
    }

    /**
     * Tests for getWorkingCopyModel
     */

    @Test
    public void should_return_properties_model_for_module_working_copy() {
        HesperidesPropertiesModel model = new HesperidesPropertiesModel(Sets.newHashSet(), Sets.newHashSet());

        when(modules.getModel(new ModuleKey("module_name", WorkingCopy.of("module_version")))).thenReturn(Optional.of(model));

        assertThat(withoutAuth("/modules/module_name/module_version/workingcopy/model")
                .get(HesperidesPropertiesModel.class))
                .isEqualTo(model);
    }

    @Test
    public void should_return_401_if_getting_properties_model_for_working_copy_and_not_authenticated() {
        try {
            withAuth("/modules/module_name/module_version/workingcopy/model")
                    .get(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    @Test
    public void should_return_404_if_getting_workingcopy_model_and_module_is_missing() {
        ModuleKey moduleInfo = ModuleKey.withModuleName("module_name")
                .withVersion(WorkingCopy.of("module_version"))
                .build();
        when(modules.getModel(moduleInfo)).thenReturn(Optional.empty());
        try {
            withoutAuth("/modules/module_name/module_version/workingcopy/model").get(Response.class);
            fail("Ne renvoie pas le status 404");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        }
    }

    /**
     * Tests for createTemplateInWorkingCopy
     */

    @Test
    public void should_create_template_in_workingcopy_if_missing() throws Exception {
        Template templateBefore = new Template(
                "modules#module_name#module_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                0L);

        TemplateData templateData = TemplateData.withTemplateName("template_name")
                .withFilename("filename")
                .withLocation("/some/location")
                .withContent("some content")
                .withRights(null)
                .build();

        Template templateAfter = new Template(
                "modules#module_name#module_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        ModuleWorkingCopyKey moduleInfo = new ModuleWorkingCopyKey("module_name", "module_version");
        when(modules.createTemplateInWorkingCopy(moduleInfo, templateData)).thenReturn(templateAfter);

        assertThat(withoutAuth("/modules/module_name/module_version/workingcopy/templates")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(Template.class, MAPPER.writeValueAsString(templateBefore))).isEqualTo(templateAfter);
    }

    @Test
    public void should_return_409_conflict_if_create_existing_template_in_workingcopy() throws Exception {
        Template templateBefore = new Template(
                "modules#module_name#module_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                0L);

        TemplateData templateData = TemplateData.withTemplateName("template_name")
                .withFilename("filename")
                .withLocation("/some/location")
                .withContent("some content")
                .withRights(null)
                .build();

        ModuleWorkingCopyKey moduleInfo = new ModuleWorkingCopyKey("module_name", "module_version");
        when(modules.createTemplateInWorkingCopy(moduleInfo, templateData)).thenThrow(new DuplicateResourceException("Non unique"));

        try {
            withoutAuth("/modules/module_name/module_version/workingcopy/templates")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Response.class, MAPPER.writeValueAsString(templateBefore));
            fail("Ne renvoie pas 409");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        }

    }

    @Test
    public void should_return_404_not_found_if_create_template_but_module_not_found() throws Exception {
        Template templateBefore = new Template(
                "modules#module_name#module_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                0L);

        TemplateData templateData = TemplateData.withTemplateName("template_name")
                .withFilename("filename")
                .withLocation("/some/location")
                .withContent("some content")
                .withRights(null)
                .build();

        ModuleWorkingCopyKey moduleInfo = new ModuleWorkingCopyKey("module_name", "module_version");
        when(modules.createTemplateInWorkingCopy(moduleInfo, templateData)).thenThrow(new MissingResourceException("ModuleClient not found"));

        try {
            withoutAuth("/modules/module_name/module_version/workingcopy/templates")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Response.class, MAPPER.writeValueAsString(templateBefore));
            fail("Ne renvoie pas 404");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        }

    }

    @Test
    public void should_return_401_if_creating_template_and_not_authenticated() {
        try {
            withAuth("/modules/module_name/module_version/workingcopy/templates")
                    .post(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    /**
     * Tests for updateTempalteInWorkingCopy
     */

    @Test
    public void should_update_template_in_working_copy_if_exists() throws JsonProcessingException {
        Template templateBefore = new Template(
                "modules#module_name#module_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        TemplateData templateData = TemplateData.withTemplateName("template_name")
                .withFilename("filename")
                .withLocation("/some/location")
                .withContent("some content")
                .withRights(null)
                .build();

        Template templateAfter = new Template(
                "modules#module_name#module_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                2L);

        ModuleWorkingCopyKey moduleInfo = new ModuleWorkingCopyKey("module_name", "module_version");
        when(modules.updateTemplateInWorkingCopy(moduleInfo, templateData)).thenReturn(templateAfter);

        assertThat(withoutAuth("/modules/module_name/module_version/workingcopy/templates")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .put(Template.class, MAPPER.writeValueAsString(templateBefore))).isEqualTo(templateAfter);
    }

    @Test
    public void should_return_404_not_found_if_update_missing_template_in_working_copy() throws JsonProcessingException {
        Template templateBefore = new Template(
                "modules#module_name#module_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        TemplateData templateData = TemplateData.withTemplateName("template_name")
                .withFilename("filename")
                .withLocation("/some/location")
                .withContent("some content")
                .withRights(null)
                .build();

        ModuleWorkingCopyKey moduleInfo = new ModuleWorkingCopyKey("module_name", "module_version");
        when(modules.updateTemplateInWorkingCopy(moduleInfo, templateData)).thenThrow(new MissingResourceException("Not found"));

        try {
            withoutAuth("/modules/module_name/module_version/workingcopy/templates")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .put(Response.class, MAPPER.writeValueAsString(templateBefore));
            fail("Ne renvoie pas 404");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        }
    }

    @Test
    public void should_return_401_if_updating_template_and_not_authenticated() {
        try {
            withAuth("/modules/module_name/module_version/workingcopy/templates")
                    .put(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    /**
     * Tests for createWorkingCopy
     */

    @Test
    public void should_create_new_empty_module_working_copy_if_no_query_param_is_provided() throws JsonProcessingException {
        Module moduleBefore = new Module("module_name", "module_version", true, Sets.newHashSet(), 0L);
        Module moduleAfter = new Module("module_name", "module_version", true, Sets.newHashSet(), 1L);

        ModuleKey.withModuleName("module_name")
                .withVersion(WorkingCopy.of("module_version"))
                .build();
        when(modules.createWorkingCopy(moduleBefore)).thenReturn(moduleAfter);

        assertThat(withoutAuth("/modules")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(Module.class, MAPPER.writeValueAsString(moduleBefore)))
                .isEqualTo(moduleAfter);
    }

    @Test
    public void should_return_409_conflict_when_creating_module_working_copy_if_module_already_exists() throws JsonProcessingException {
        Module moduleBefore = new Module("module_name", "1.6.0", true, Sets.newHashSet(), 1);
        ModuleKey.withModuleName("module_name")
                .withVersion(WorkingCopy.of("1.6.0"))
                .build();
        doThrow(new DuplicateResourceException("")).when(modules).createWorkingCopy(moduleBefore);
        try {
            withoutAuth("/modules").type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Module.class, MAPPER.writeValueAsString(moduleBefore));
            fail("Ne renvoie pas 409");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        }
    }

    @Test
    public void should_return_422_when_creating_module_from_but_body_name_is_null() throws JsonProcessingException {
        Module moduleBefore = new Module(null, "the_module_version", false, Sets.newHashSet(), 0L);
        Module moduleAfter = new Module("the_module_name", "the_module_version", true, Sets.newHashSet(), 1L);

        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("the_module_name", "the_module_version");
        ModuleKey fromModuleKey = ModuleKey.withModuleName("the_name_from")
                .withVersion(Release.of("the_version_from"))
                .build();
        when(modules.createWorkingCopyFrom(moduleKey, fromModuleKey)).thenReturn(moduleAfter);

        try {
            withoutAuth("/modules")
                    .queryParam("from_module_name", "the_name_from")
                    .queryParam("from_module_version", "the_version_from")
                    .queryParam("from_is_working_copy", "false")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Module.class, MAPPER.writeValueAsString(moduleBefore));
            Assertions.fail("Ne renvoie pas 422");
        } catch (UniformInterfaceException e) {
            assertThat(Response.Status.Family.CLIENT_ERROR.equals(e.getResponse().getStatus()));
        }
    }

    @Test
    public void should_return_422_when_creating_module_from_but_body_version_is_null() throws JsonProcessingException {
        Module moduleBefore = new Module("the_module_name", null, false, Sets.newHashSet(), 0L);
        Module moduleAfter = new Module("the_module_name", "the_module_version", true, Sets.newHashSet(), 1L);

        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("the_module_name", "the_module_version");
        ModuleKey fromModuleKey = ModuleKey.withModuleName("the_name_from")
                .withVersion(Release.of("the_version_from"))
                .build();
        when(modules.createWorkingCopyFrom(moduleKey, fromModuleKey)).thenReturn(moduleAfter);

        try {
            withoutAuth("/modules")
                    .queryParam("from_module_name", "the_name_from")
                    .queryParam("from_module_version", "the_version_from")
                    .queryParam("from_is_working_copy", "false")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Module.class, MAPPER.writeValueAsString(moduleBefore));
            Assertions.fail("Ne renvoie pas 422");
        } catch (UniformInterfaceException e) {
            assertThat(Response.Status.Family.CLIENT_ERROR.equals(e.getResponse().getStatus()));
        }
    }

    @Test
    public void should_create_working_copy_from_existing_release_if_from_module_name_and_from_module_version_and_from_is_working_copy_query_params_are_provided() throws JsonProcessingException {
        Module moduleBefore = new Module("the_module_name", "the_module_version", false, Sets.newHashSet(), 0L);
        Module moduleAfter = new Module("the_module_name", "the_module_version", true, Sets.newHashSet(), 1L);

        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("the_module_name", "the_module_version");
        ModuleKey fromModuleKey = ModuleKey.withModuleName("the_name_from")
                .withVersion(Release.of("the_version_from"))
                .build();
        when(modules.createWorkingCopyFrom(moduleKey, fromModuleKey)).thenReturn(moduleAfter);

        assertThat(withoutAuth("/modules")
                .queryParam("from_module_name", "the_name_from")
                .queryParam("from_module_version", "the_version_from")
                .queryParam("from_is_working_copy", "false")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(Module.class, MAPPER.writeValueAsString(moduleBefore))).isEqualTo(moduleAfter);
    }

    @Test
    public void should_return_400_if_create_working_copy_from_and_from_module_name_query_param_is_missing() throws JsonProcessingException {
        Module moduleBefore = new Module("module_name", "module_version", true, Sets.newHashSet(), 0L);
        try {
            withoutAuth("/modules")
                    .queryParam("from_module_version", "the_version_from")
                    .queryParam("from_is_working_copy", "false")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Response.class, MAPPER.writeValueAsString(moduleBefore));
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_create_working_copy_from_and_from_module_version_query_param_is_missing() throws JsonProcessingException {
        Module moduleBefore = new Module("module_name", "module_version", true, Sets.newHashSet(), 0L);
        try {
            withoutAuth("/modules")
                    .queryParam("from_module_name", "the__name_from")
                    .queryParam("from_is_working_copy", "false")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Response.class, MAPPER.writeValueAsString(moduleBefore));
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_create_working_copy_from_and_from_is_working_copy_query_param_is_missing() throws JsonProcessingException {
        Module moduleBefore = new Module("module_name", "module_version", true, Sets.newHashSet(), 0L);
        try {
            withoutAuth("/modules")
                    .queryParam("from_module_version", "the_version_from")
                    .queryParam("from_module_name", "the__name_from")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Response.class, MAPPER.writeValueAsString(moduleBefore));
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_404_not_found_if_create_workingcopy_when_release_missing() throws JsonProcessingException {
        ModuleWorkingCopyKey moduleInfo = new ModuleWorkingCopyKey("module_name", "module_version");
        ModuleKey fromModuleKey = ModuleKey.withModuleName("the_name_from")
                .withVersion(Release.of("the_version_from"))
                .build();

        when(modules.createWorkingCopyFrom(moduleInfo, fromModuleKey)).thenThrow(new MissingResourceException(""));

        Module moduleBefore = new Module("module_name", "module_version", true, Sets.newHashSet(), 0L);

        try {
            withoutAuth("/modules")
                    .queryParam("from_module_name", "the_name_from")
                    .queryParam("from_module_version", "the_version_from")
                    .queryParam("from_is_working_copy", "false")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Module.class, MAPPER.writeValueAsString(moduleBefore));
            fail("Ne renvoie pas 404");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        }
    }

    @Test
    public void should_return_401_if_creating_working_copy_and_not_authenticated() {
        try {
            withAuth("/modules")
                    .post(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    /**
     * Tests for updateWorkingCopy
     */
    @Test
    public void should_update_module_working_copy() throws JsonProcessingException {
        Module moduleBefore = new Module("module_name", "module_version", true, Sets.newHashSet(), 0L);
        Module moduleAfter = new Module("module_name", "module_version", true, Sets.newHashSet(), 1L);

        ModuleKey.withModuleName("module_name")
                .withVersion(WorkingCopy.of("module_version"))
                .build();
        when(modules.updateWorkingCopy(moduleBefore)).thenReturn(moduleAfter);

        assertThat(withoutAuth("/modules")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .put(Module.class, MAPPER.writeValueAsString(moduleBefore)))
                .isEqualTo(moduleAfter);
    }

    @Test
    public void should_return_404_when_updating_missing_module() throws JsonProcessingException {
        Module moduleBefore = new Module("module_name", "1.6.0", true, Sets.newHashSet(), 1L);

        doThrow(new MissingResourceException("")).when(modules).updateWorkingCopy(moduleBefore);
        try {
            withoutAuth("/modules")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .put(Module.class, MAPPER.writeValueAsString(moduleBefore));
            fail("Ne renvoie pas 404");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        }
    }

    @Test
    public void should_return_401_if_updating_working_copy_and_not_authenticated() {
        try {
            withAuth("/modules")
                    .put(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    /**
     * Tests for deleteTemplateInWorkingCopy
     */

    @Test
    public void should_delete_template_in_workingcopy_if_exists() {
        ModuleWorkingCopyKey moduleInfo = new ModuleWorkingCopyKey("the_module_name", "the_module_version");
        withoutAuth("/modules/the_module_name/the_module_version/workingcopy/templates/the_name").delete();
        verify(modules).deleteTemplateInWorkingCopy(moduleInfo, "the_name");
    }

    @Test
    public void should_return_404_not_found_if_delete_missing_template() {
        ModuleWorkingCopyKey moduleInfo = new ModuleWorkingCopyKey("the_module_name", "the_module_version");
        doThrow(new MissingResourceException("Not found")).when(modules).deleteTemplateInWorkingCopy(moduleInfo, "the_name");
        try {
            withoutAuth("/modules/the_module_name/the_module_version/workingcopy/templates/the_name")
                    .delete();
            fail("Ne renvoie pas 404");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        }
    }

    @Test
    public void should_return_401_if_deleting_template_in_working_copy_and_not_authenticated() {
        try {
            withAuth("/modules/the_module_name/the_module_version/workingcopy/templates/the_name")
                    .delete(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    /**
     * Tests for getWorkingCopy
     */


    @Test
    public void should_return_module_workingcopy() {
        Module module = new Module("module_name", "module_version", true, Sets.newHashSet(), 1L);
        ModuleKey moduleInfo = ModuleKey.withModuleName("module_name")
                .withVersion(WorkingCopy.of("module_version"))
                .build();
        when(modules.getModule(moduleInfo)).thenReturn(Optional.of(module));

        assertThat(withoutAuth("/modules/module_name/module_version/workingcopy").get(Module.class)).isEqualTo(module);
    }

    @Test
    public void should_return_404_if_module_workingcopy_is_missing() {
        ModuleKey moduleInfo = ModuleKey.withModuleName("module_name")
                .withVersion(WorkingCopy.of("module_version"))
                .build();
        when(modules.getModule(moduleInfo)).thenThrow(new MissingResourceException("missing"));

        try {
            withoutAuth("/modules/module_name/module_version/workingcopy").get(Response.class);
            fail("Ne renvoie pas le status 404");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        }
    }

    @Test
    public void should_return_401_if_getting_module_working_copy_and_not_authenticated() {
        try {
            withAuth("/modules/module_name/module_version/workingcopy")
                    .get(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    /**
     * Tests for getRelease
     */

    @Test
    public void should_return_module_release() {
        Module module = new Module("module_name", "module_version", false, Sets.newHashSet(), 1L);
        ModuleKey moduleInfo = ModuleKey.withModuleName("module_name")
                .withVersion(Release.of("module_version"))
                .build();
        when(modules.getModule(moduleInfo)).thenReturn(Optional.of(module));

        assertThat(withoutAuth("/modules/module_name/module_version/release").get(Module.class)).isEqualTo(module);
    }


    @Test
    public void should_return_404_if_module_release_is_missing() {
        ModuleKey moduleInfo = ModuleKey.withModuleName("module_name")
                .withVersion(Release.of("module_version"))
                .build();
        when(modules.getModule(moduleInfo)).thenThrow(new MissingResourceException("missing"));

        try {
            withoutAuth("/modules/module_name/module_version/release").get(Response.class);
            fail("Ne renvoie pas le status 404");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        }
    }

    @Test
    public void should_return_401_if_getting_module_release_and_not_authenticated() {
        try {
            withAuth("/modules/module_name/module_version/release")
                    .get(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    /**
     * Tests for createRelease
     */

    @Test
    public void should_create_release_from_existing_workingcopy_when_module_name_and_module_version_and_release_version_query_params_are_provided() {
        Module module = new Module("the_module_name", "the_module_version", false, Sets.newHashSet(), 1L);
        ModuleWorkingCopyKey moduleInfo = new ModuleWorkingCopyKey("the_module_name", "the_module_version");
        when(modules.createRelease(moduleInfo, "the_release_version")).thenReturn(module);

        assertThat(withoutAuth("/modules/create_release")
                .queryParam("module_name", "the_module_name")
                .queryParam("module_version", "the_module_version")
                .queryParam("release_version", "the_release_version")
                .post(Module.class)).isEqualTo(module);
    }

    @Test
    public void should_return_400_if_create_release_and_module_name_query_param_is_missing() {
        try {
            withoutAuth("/modules/create_release")
                    .queryParam("module_version", "the_module_version")
                    .queryParam("release_version", "the_release_version")
                    .post(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_create_release_and_module_version_query_param_is_missing() {
        try {
            withoutAuth("/modules/create_release")
                    .queryParam("module_name", "the_module_name")
                    .queryParam("release_version", "the_release_version")
                    .post(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_create_release_with_same_version_as_the_module_from_if_query_param_is_missing() {
        Module module = new Module("the_module_name", "the_module_version", false, Sets.newHashSet(), 1L);
        ModuleWorkingCopyKey moduleInfo = new ModuleWorkingCopyKey("the_module_name", "the_module_version");
        when(modules.createRelease(moduleInfo, "the_module_version")).thenReturn(module);

        assertThat(withoutAuth("/modules/create_release")
                .queryParam("module_name", "the_module_name")
                .queryParam("module_version", "the_module_version")
                .post(Module.class)).isEqualTo(module);
    }

    @Test
    public void should_return_missing_if_create_release_when_working_copy_missing() {
        ModuleWorkingCopyKey moduleInfo = new ModuleWorkingCopyKey("module_name", "1.6.0");
        doThrow(new MissingResourceException("There is no working copy for version 1.6.0. You should create a working copy before releasing")).when(modules).createRelease(moduleInfo, "the_release_version");
        try {
            withoutAuth("/modules/create_release")
                    .queryParam("module_name", "module_name")
                    .queryParam("module_version", "1.6.0")
                    .queryParam("release_version", "the_release_version")
                    .post();
            fail("Ne renvoie pas 404");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        }
    }

    @Test
    public void should_return_409_conflict_if_create_existing_release() {
        ModuleWorkingCopyKey moduleInfo = new ModuleWorkingCopyKey("module_name", "1.6.0");
        doThrow(new DuplicateResourceException("There is no working copy for version 1.6.0. You should create a working copy before releasing")).when(modules).createRelease(moduleInfo, "the_release_version");
        try {
            withoutAuth("/modules/create_release")
                    .queryParam("module_name", "module_name")
                    .queryParam("module_version", "1.6.0")
                    .queryParam("release_version", "the_release_version")
                    .post();
            fail("Ne renvoie pas 409");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        }
    }

    @Test
    public void should_return_401_if_creating_release_and_not_authenticated() {
        try {
            withAuth("/modules/create_release")
                    .queryParam("module_name", "module_name")
                    .queryParam("module_version", "1.6.0")
                    .queryParam("release_version", "the_release_version")
                    .post(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_modulename_list() {
        try {
                withoutAuth("/modules/%20%09%00").get(List.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_modulename_with_version_list_with_module_name_not_valid() {
        try {
            withoutAuth("/modules/%20%09%00/1.0.0.0").get(List.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_modulename_with_version_list_with_version_not_valid() {
        try {
            withoutAuth("/modules/module_name/%20%09%00").get(List.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_moduletype_with_version_list_with_module_name_not_valid() {
        try {
            withoutAuth("/modules/%20%09%00/1.0.0.0/module_type").get(List.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_moduletype_with_version_list_with_version_not_valid() {
        try {
            withoutAuth("/modules/module_name/%20%09%00/module_type").get(List.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_moduletype_with_version_list_with_module_type_not_valid() {
        try {
            withoutAuth("/modules/module_name/1.0.0.0/%20%09%00")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .get(List.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_list_template_workingcopy_with_module_name_not_valid() {
        try {
            withoutAuth("/modules/%20%09%00/1.0.0.0/workingcopy/templates").get(List.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_list_template_workingcopy_with_version_not_valid() {
        try {
            withoutAuth("/modules/module_name/%20%09%00/workingcopy/templates").get(List.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_delete_module_workingcopy_with_module_name_not_valid() {
        try {
            withoutAuth("/modules/%20%09%00/1.0.0.0/workingcopy").delete();
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_delete_module_workingcopy_with_version_not_valid() {
        try {
            withoutAuth("/modules/module_name/%20%09%00/workingcopy").delete();
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_delete_module_release_with_module_name_not_valid() {
        try {
            withoutAuth("/modules/%20%09%00/1.0.0.0/release").delete();
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_delete_module_release_with_version_not_valid() {
        try {
            withoutAuth("/modules/module_name/%20%09%00/release").delete();
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_template_release_with_module_name_not_valid() {
        try {
            withoutAuth("/modules/%20%09%00/1.0.0.0/release/templates").get(List.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_template_release_with_version_not_valid() {
        try {
            withoutAuth("/modules/module_name/%20%09%00/release/templates").get(List.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_one_template_workingcopy_with_module_name_not_valid() {
        try {
            withoutAuth("/modules/%20%09%00/1.0.0.0/workingcopy/templates/template_name").get(List.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
         public void should_return_400_when_get_one_template_workingcopy_with_version_not_valid() {
        try {
            withoutAuth("/modules/module_name/%20%09%00/workingcopy/templates/template_name").get(List.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_one_template_workingcopy_with_template_name_not_valid() {
        try {
            withoutAuth("/modules/module_name/1.0.0.0/workingcopy/templates/%20%09%00").get(List.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_one_template_release_with_module_name_not_valid() {
        try {
            withoutAuth("/modules/%20%09%00/1.0.0.0/release/templates/template_name").get(List.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_one_template_release_with_version_not_valid() {
        try {
            withoutAuth("/modules/module_name/%20%09%00/release/templates/template_name").get(List.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_one_template_release_with_template_name_not_valid() {
        try {
            withoutAuth("/modules/module_name/1.0.0.0/release/templates/%20%09%00").get(List.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_model_release_with_module_name_not_valid() {
        try {
            withoutAuth("/modules/%20%09%00/1.0.0.0/release/model").get(List.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_model_release_with_version_not_valid() {
        try {
            withoutAuth("/modules/module_name/%20%09%00/release/model").get(List.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_model_workingcopy_with_module_name_not_valid() {
        try {
            withoutAuth("/modules/%20%09%00/1.0.0.0/workingcopy/model").get(List.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_model_workingcopy_with_version_not_valid() {
        try {
            withoutAuth("/modules/module_name/%20%09%00/workingcopy/model").get(List.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_put_template_release_with_module_name_not_valid() throws JsonProcessingException {
        Template templateBefore = new Template(
                "modules#module_name#module_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        try {
            withoutAuth("/modules/%20%09%00/1.0.0.0/workingcopy/templates")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .put(Template.class, MAPPER.writeValueAsString(templateBefore));
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_put_template_release_with_version_not_valid() throws JsonProcessingException {
        Template templateBefore = new Template(
                "modules#module_name#module_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);
        try {
            withoutAuth("/modules/module_name/%20%09%00/workingcopy/templates")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .put(Template.class, MAPPER.writeValueAsString(templateBefore));
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_post_template_release_with_module_name_not_valid() throws JsonProcessingException {
        Template templateBefore = new Template(
                "modules#module_name#module_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        try {
            withoutAuth("/modules/%20%09%00/1.0.0.0/workingcopy/templates")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Template.class, MAPPER.writeValueAsString(templateBefore));
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_post_template_release_with_version_not_valid() throws JsonProcessingException {
        Template templateBefore = new Template(
                "modules#module_name#module_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        try {
            withoutAuth("/modules/module_name/%20%09%00/workingcopy/templates")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Template.class, MAPPER.writeValueAsString(templateBefore));
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_create_working_with_from_module_name_not_valid() throws JsonProcessingException {
        Module module = new Module("module_name", "module_version", false, Sets.newHashSet(), 1L);

        try {
            withoutAuth("/modules/module_name/%20%09%00/workingcopy/templates")
                    .queryParam("from_module_name", "%20%09%00")
                    .queryParam("from_module_version", "from_module_version")
                    .queryParam("from_is_working_copy", "true")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Module.class, MAPPER.writeValueAsString(module));
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_create_working_with_from_module_version_not_valid() throws JsonProcessingException {
        Template templateBefore = new Template(
                "modules#module_name#module_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        try {
            withoutAuth("/modules/module_name/%20%09%00/workingcopy/templates")
                    .queryParam("from_module_name", "from_module_name")
                    .queryParam("from_module_version", "%20%09%00")
                    .queryParam("from_is_working_copy", "true")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Template.class, MAPPER.writeValueAsString(templateBefore));
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_create_working_with_from_is_working_copy_not_valid() throws JsonProcessingException {
        Template templateBefore = new Template(
                "modules#module_name#module_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        try {
            withoutAuth("/modules/module_name/%20%09%00/workingcopy/templates")
                    .queryParam("from_module_name", "from_module_name")
                    .queryParam("from_module_version", "from_module_version")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Template.class, MAPPER.writeValueAsString(templateBefore));
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    // TODO updateWorkingCopy
    @Test
    public void should_return_400_when_delete_template_in_workingcopy_when_module_name_not_valid() {
        try {
            withoutAuth("/modules/%20%09%00/the_module_version/workingcopy/templates/the_name").delete();
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_delete_template_in_workingcopy_when_version_name_not_valid() {
        try {
            withoutAuth("/modules/the_module_name/%20%09%00/workingcopy/templates/the_name").delete();
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_delete_template_in_workingcopy_when_tamplate_name_not_valid() {
        try {
            withoutAuth("/modules/the_module_name/the_module_version/workingcopy/templates/%20%09%00").delete();
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_module_workingcopy_when_module_name_not_valid() {
        try {
            withoutAuth("/modules/%20%09%00/module_version/workingcopy").get(Module.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_module_workingcopy_when_version_not_valid() {
        try {
            withoutAuth("/modules/module_name/%20%09%00/workingcopy").get(Module.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_module_release_when_module_name_not_valid() {
        try {
            withoutAuth("/modules/%20%09%00/module_version/release").get(Module.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_when_get_module_release_when_version_not_valid() {
        try {
            withoutAuth("/modules/module_name/%20%09%00/release").get(Module.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_whencreate_release_from_existing_workingcopy_when_module_name_is_not_valid() {
        try {
            withoutAuth("/modules/create_release")
                    .queryParam("%20%09%00", "the_module_name")
                    .queryParam("module_version", "the_module_version")
                    .queryParam("release_version", "the_release_version")
                    .post(Module.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_whencreate_release_from_existing_workingcopy_when_module_version_is_not_valid() {
        try {
            withoutAuth("/modules/create_release")
                    .queryParam("module_name", "the_module_name")
                    .queryParam("%20%09%00", "the_module_version")
                    .queryParam("release_version", "the_release_version")
                    .post(Module.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
            }
        }
}

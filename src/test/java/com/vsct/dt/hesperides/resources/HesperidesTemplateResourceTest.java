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
import com.vsct.dt.hesperides.exception.wrapper.IllegalArgumentExceptionMapper;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.exception.wrapper.*;
import com.vsct.dt.hesperides.indexation.search.TemplateSearch;
import com.vsct.dt.hesperides.indexation.search.TemplateSearchResponse;
import com.vsct.dt.hesperides.security.DisabledAuthProvider;
import com.vsct.dt.hesperides.security.SimpleAuthenticator;
import com.vsct.dt.hesperides.templating.Template;
import com.vsct.dt.hesperides.templating.TemplateData;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageKey;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageWorkingCopyKey;
import com.vsct.dt.hesperides.templating.packages.TemplatePackages;
import com.vsct.dt.hesperides.util.Release;
import com.vsct.dt.hesperides.util.WorkingCopy;
import io.dropwizard.auth.basic.BasicAuthProvider;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.mockito.Mockito.*;

/**
 * Created by william_montaz on 01/09/14.
 */
/* AUTHENTICATION -> John_Doe:secret => Basic Sm9obl9Eb2U6c2VjcmV0 */
public class HesperidesTemplateResourceTest {

    private static final TemplatePackages templatePackages = mock(TemplatePackages.class);
    private static final TemplateSearch   templateSearch   = mock(TemplateSearch.class);
    private static final ObjectMapper     MAPPER           = Jackson.newObjectMapper();

    @ClassRule
    public static ResourceTestRule simpleAuthResources = ResourceTestRule.builder()
            .addProvider(new BasicAuthProvider<>(
                    new SimpleAuthenticator(),
                    "AUTHENTICATION_PROVIDER"))
            .addResource(new HesperidesTemplateResource(templatePackages, templateSearch))
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
            .addResource(new HesperidesTemplateResource(templatePackages, templateSearch))
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
        reset(templatePackages);
        reset(templateSearch);
    }

    /**
     * Test getAllTemplatesInWorkingCopy
     */

    @Test
    public void should_return_list_of_all_templates_in_working_copy() {
        Template template1 = new Template(
                "packages#name#version#WORKINGCOPY",
                "name1",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        Template template2 = new Template(
                "packages#name#version#WORKINGCOPY",
                "name2",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        TemplateListItem templateListItem1 = new TemplateListItem(template1);
        TemplateListItem templateListItem2 = new TemplateListItem(template2);

        TemplatePackageKey packageInfo = TemplatePackageKey.withName("name")
                .withVersion(WorkingCopy.of("version"))
                .build();

        when(templatePackages.getAllTemplates(packageInfo)).thenReturn(Sets.newHashSet(template1, template2));

        assertThat(withoutAuth("/templates/packages/name/version/workingcopy/templates")
                .get(new GenericType<Set<TemplateListItem>>() {
                })).isEqualTo(Sets.newHashSet(templateListItem1, templateListItem2));
    }

    @Test
    public void should_return_401_if_getting_list_of_all_templates_in_working_copy_and_not_authenticated() {
        try {
            withAuth("/templates/packages/name/version/workingcopy/templates")
                    .post(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    /**
     * Test for getAllTemplatesInRelease
     */

    @Test
    public void should_return_list_of_all_templates_in_release() {
        Template template1 = new Template(
                "packages#name#version#RELEASE",
                "name1",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        Template template2 = new Template(
                "packages#name#version#RELEASE",
                "name2",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        TemplateListItem templateListItem1 = new TemplateListItem(template1);
        TemplateListItem templateListItem2 = new TemplateListItem(template2);

        TemplatePackageKey packageInfo = TemplatePackageKey.withName("name")
                .withVersion(Release.of("version"))
                .build();

        when(templatePackages.getAllTemplates(packageInfo)).thenReturn(Sets.newHashSet(template1, template2));

        assertThat(withoutAuth("/templates/packages/name/version/release/templates")
                .get(new GenericType<List<TemplateListItem>>() {
                })).isEqualTo(Lists.newArrayList(templateListItem1, templateListItem2));
    }

    @Test
    public void should_return_401_if_getting_list_of_all_templates_in_release_and_not_authenticated(){
        try {
            withAuth("/templates/packages/name/version/release/templates")
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
    public void should_return_properties_model_for_template_package_released(){
        HesperidesPropertiesModel model = new HesperidesPropertiesModel(Sets.newHashSet(), Sets.newHashSet());

        when(templatePackages.getModel("the_name", "the_version", false)).thenReturn(model);

        assertThat(withoutAuth("/templates/packages/the_name/the_version/release/model")
                .get(HesperidesPropertiesModel.class))
                .isEqualTo(model);
    }

    @Test
    public void should_return_401_if_getting_properties_model_for_release_and_not_authenticated(){
        try {
            withAuth("/templates/packages/the_name/the_version/release/model")
                    .get(Response.class);
            Assert.fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    /**
     * Tests for getWorkingCopyModel
     */

    @Test
    public void should_return_properties_model_for_template_package_working_copy(){
        HesperidesPropertiesModel model = new HesperidesPropertiesModel(Sets.newHashSet(), Sets.newHashSet());

        when(templatePackages.getModel("the_name", "the_version", true)).thenReturn(model);

        assertThat(withoutAuth("/templates/packages/the_name/the_version/workingcopy/model")
                .get(HesperidesPropertiesModel.class))
                .isEqualTo(model);
    }

    @Test
    public void should_return_401_if_getting_properties_model_for_working_copy_and_not_authenticated(){
        try {
            withAuth("/templates/packages/the_name/the_version/workingcopy/model")
                    .get(Response.class);
            Assert.fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    /**
     * Tests for searchPackages
     */

    @Test
    public void should_perform_search_on_term_separated_with_diese_character(){
        TemplateSearchResponse templateSearchResponse1 = new TemplateSearchResponse("packages#a_name#a_version#WORKINGCOPY", "template_name");
        TemplateSearchResponse templateSearchResponse2 = new TemplateSearchResponse("packages#a_name#a_version#WORKINGCOPY", "template_name");
        TemplateSearchResponse templateSearchResponse3 = new TemplateSearchResponse("packages#b_name#b_version#WORKINGCOPY", "template_name");

        TemplatePackageKey package1 = new TemplatePackageKey("a_name", "a_version", true);
        TemplatePackageKey package2 = new TemplatePackageKey("b_name", "b_version", true);

        when(templateSearch.getTemplatesByNamespaceLike(new String[]{"packages", "*term1*", "*term2*", "*term3*"})).thenReturn(Sets.newHashSet(templateSearchResponse1, templateSearchResponse2, templateSearchResponse3));

        assertThat(withoutAuth("/templates/packages/perform_search")
                .queryParam("terms", "term1#term2#term3")
                .post(new GenericType<List<TemplatePackageKey>>() {
                })).isEqualTo(Lists.newArrayList(package1, package2));
    }

    @Test
    public void should_return_400_if_terms_query_param_is_missing(){
        try {
            withoutAuth("/templates/packages/perform_search")
                    .post(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_401_if_perform_search_and_not_authenticated(){
        try {
            withAuth("/templates/packages/perform_search")
                    .post(Response.class);
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
        Template template1 = new Template(
                "packages#pckg_name#pckg_version#WORKINGCOPY",
                "tplt_name",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        TemplatePackageKey packageInfo = TemplatePackageKey.withName("pckg_name")
                .withVersion(WorkingCopy.of("pckg_version"))
                .build();

        when(templatePackages.getTemplate(packageInfo, "tplt_name")).thenReturn(Optional.of(
                template1
        ));

        assertThat(withoutAuth("/templates/packages/pckg_name/pckg_version/workingcopy/templates/tplt_name")
                .get(Template.class)).isEqualTo(template1);
    }

    @Test
    public void should_return_404_if_template_is_missing_from_working_copy(){
        TemplatePackageKey packageInfo = TemplatePackageKey.withName("pckg_name")
                .withVersion(WorkingCopy.of("pckg_version"))
                .build();

        when(templatePackages.getTemplate(packageInfo, "unknown")).thenReturn(Optional.empty());
        try {
            withoutAuth("/templates/packages/pckg_name/pckg_version/workingcopy/templates/unknown").get(Response.class);
            fail("Ne renvoie pas le status 404");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        }
    }

    @Test
    public void should_return_401_if_getting_template_from_working_copy_and_not_authenticated(){
        try {
            withAuth("/templates/packages/pckg_name/pckg_version/workingcopy/templates/unknown")
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
        Template template1 = new Template(
                "packages#pckg_name#pckg_version#RELEASE",
                "tplt_name",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        TemplatePackageKey packageInfo = TemplatePackageKey.withName("pckg_name")
                .withVersion(Release.of("pckg_version"))
                .build();

        when(templatePackages.getTemplate(packageInfo, "tplt_name")).thenReturn(Optional.of(
                template1
        ));

        assertThat(withoutAuth("/templates/packages/pckg_name/pckg_version/release/templates/tplt_name")
                .get(Template.class)).isEqualTo(template1);
    }

    @Test
    public void should_return_404_if_template_is_missing_from_release(){
        TemplatePackageKey packageInfo = TemplatePackageKey.withName("pckg_name")
                .withVersion(Release.of("pckg_version"))
                .build();

        when(templatePackages.getTemplate(packageInfo, "unknown")).thenReturn(Optional.empty());
        try {
            withoutAuth("/templates/packages/pckg_name/pckg_version/release/templates/unknown").get(Response.class);
            fail("Ne renvoie pas le status 404");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        }
    }

    @Test
    public void should_return_401_if_getting_template_from_release_and_not_authenticated(){
        try {
            withAuth("/templates/packages/pckg_name/pckg_version/release/templates/unknown")
                    .get(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    /**
     * Tests for createTemplateInWorkingCopy
     */

    @Test
    public void should_create_template_in_workingcopy_if_missing() throws Exception {
        Template templateBefore = new Template(
                "packages#pckg_name#pckg_version#WORKINGCOPY",
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
                "packages#pckg_name#pckg_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                1L);

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("pckg_name", "pckg_version");

        when(templatePackages.createTemplateInWorkingCopy(packageInfo, templateData)).thenReturn(templateAfter);

        assertThat(withoutAuth("/templates/packages/pckg_name/pckg_version/workingcopy/templates")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(Template.class, MAPPER.writeValueAsString(templateBefore))).isEqualTo(templateAfter);
    }

    @Test
    public void should_return_409_conflict_if_create_existing_template_in_workingcopy() throws Exception {
        Template templateBefore = new Template(
                "packages#pckg_name#pckg_version#WORKINGCOPY",
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

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("pckg_name", "pckg_version");

        when(templatePackages.createTemplateInWorkingCopy(packageInfo, templateData)).thenThrow(new DuplicateResourceException("Non unique"));

        try {
            withoutAuth("/templates/packages/pckg_name/pckg_version/workingcopy/templates")
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
                "packages#pckg_name#pckg_version#WORKINGCOPY",
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

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("pckg_name", "pckg_version");

        when(templatePackages.createTemplateInWorkingCopy(packageInfo, templateData)).thenThrow(new MissingResourceException("Module not found"));

        try {
            withoutAuth("/templates/packages/pckg_name/pckg_version/workingcopy/templates")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Response.class, MAPPER.writeValueAsString(templateBefore));
            fail("Ne renvoie pas 404");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        }

    }

    @Test
    public void should_return_401_if_creating_template_and_not_authenticated(){
        try {
            withAuth("/templates/packages/pckg_name/pckg_version/workingcopy/templates")
                    .post(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    /**
     * Tests for updateTemplateInWorkingCopy
     */

    @Test
    public void should_update_template_in_working_copy_if_exists() throws JsonProcessingException {
        Template templateBefore = new Template(
                "packages#pckg_name#pckg_version#WORKINGCOPY",
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
                "packages#pckg_name#pckg_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                2L);

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("pckg_name", "pckg_version");

        when(templatePackages.updateTemplateInWorkingCopy(packageInfo, templateData)).thenReturn(templateAfter);

        assertThat(withoutAuth("/templates/packages/pckg_name/pckg_version/workingcopy/templates")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .put(Template.class, MAPPER.writeValueAsString(templateBefore))).isEqualTo(templateAfter);
    }

    @Test
    public void should_return_404_not_found_if_update_missing_template_in_working_copy() throws JsonProcessingException {
        Template templateBefore = new Template(
                "packages#pckg_name#pckg_version#WORKINGCOPY",
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

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("pckg_name", "pckg_version");

        when(templatePackages.updateTemplateInWorkingCopy(packageInfo, templateData)).thenThrow(new MissingResourceException("Not found"));

        try {
            withoutAuth("/templates/packages/pckg_name/pckg_version/workingcopy/templates")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .put(Response.class, MAPPER.writeValueAsString(templateBefore));
            fail("Ne renvoie pas 404");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        }
    }

    @Test
    public void should_return_401_if_updating_template_and_not_authenticated(){
        try {
            withAuth("/templates/packages/pckg_name/pckg_version/workingcopy/templates")
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
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("pckg_name", "pckg_version");
        withoutAuth("/templates/packages/pckg_name/pckg_version/workingcopy/templates/the_name").delete();
        verify(templatePackages).deleteTemplateInWorkingCopy(packageInfo, "the_name");
    }

    @Test
    public void should_return_404_not_found_if_delete_missing_template() {
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("pckg_name", "pckg_version");
        doThrow(new MissingResourceException("Not found")).when(templatePackages).deleteTemplateInWorkingCopy(packageInfo, "the_name");
        try {
            withoutAuth("/templates/packages/pckg_name/pckg_version/workingcopy/templates/the_name")
                    .delete();
            fail("Ne renvoie pas 404");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        }
    }

    @Test
    public void should_return_401_if_deleting_template_in_working_copy_and_not_authenticated(){
        try {
            withAuth("/templates/packages/pckg_name/pckg_version/workingcopy/templates/the_name")
                    .delete(Response.class);
            fail("Ne renvoie pas 401");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.UNAUTHORIZED.getStatusCode());
        }
    }

    /**
     * Tests for createWorkingCopy
     */

    @Test
    public void should_create_working_copy_from_existing_release_when_from_package_name_and_from_package_version_and_from_is_working_copy_query_params_are_provided() throws JsonProcessingException {
        TemplatePackageKey templatePackage = new TemplatePackageKey("the_package_name", "the_package_version", true);

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("the_package_name", "the_package_version");

        TemplatePackageKey fromInfos = TemplatePackageKey.withName("pckg_name_from")
                .withVersion(Release.of("pckg_version_from"))
                .build();

        when(templatePackages.createWorkingCopyFrom(packageInfo, fromInfos)).thenReturn(packageInfo);

        assertThat(withoutAuth("/templates/packages")
                .queryParam("from_package_name", "pckg_name_from")
                .queryParam("from_package_version", "pckg_version_from")
                .queryParam("from_is_working_copy", "false")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(TemplatePackageKey.class, MAPPER.writeValueAsString(templatePackage)))
                .isEqualTo(templatePackage);
    }

    @Test
    public void should_return_400_if_create_working_copy_from_and_from_package_name_query_param_is_missing() throws JsonProcessingException {
        TemplatePackageKey templatePackage = new TemplatePackageKey("the_package_name", "the_package_version", true);
        try {
            withoutAuth("/templates/packages")
                    .queryParam("from_package_version", "pckg_version_from")
                    .queryParam("from_is_working_copy", "false")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Response.class, MAPPER.writeValueAsString(templatePackage));
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_create_working_copy_from_and_from_package_version_query_param_is_missing() throws JsonProcessingException {
        TemplatePackageKey templatePackage = new TemplatePackageKey("the_package_name", "the_package_version", true);
        try {
            withoutAuth("/templates/packages")
                    .queryParam("from_package_name", "pckg_name_from")
                    .queryParam("from_is_working_copy", "false")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Response.class, MAPPER.writeValueAsString(templatePackage));
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_create_working_copy_from_and_from_is_working_copy_query_param_is_missing() throws JsonProcessingException {
        TemplatePackageKey templatePackage = new TemplatePackageKey("the_package_name", "the_package_version", true);
        try {
            withoutAuth("/templates/packages")
                    .queryParam("from_package_name", "pckg_name_from")
                    .queryParam("from_package_version", "pckg_version_from")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Response.class, MAPPER.writeValueAsString(templatePackage));
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_404_not_found_if_create_workingcopy_when_release_missing() throws JsonProcessingException {
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("the_package_name", "the_package_version");

        TemplatePackageKey fromInfos = TemplatePackageKey.withName("pckg_name_from")
                .withVersion(Release.of("pckg_version_from"))
                .build();

        when(templatePackages.createWorkingCopyFrom(packageInfo, fromInfos)).thenThrow(new MissingResourceException(""));

        TemplatePackageKey templatePackage = new TemplatePackageKey("the_package_name", "the_package_version", true);

        try {
            withoutAuth("/templates/packages")
                    .queryParam("from_package_name", "pckg_name_from")
                    .queryParam("from_package_version", "pckg_version_from")
                    .queryParam("from_is_working_copy", "false")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(TemplatePackageKey.class, MAPPER.writeValueAsString(templatePackage));
            fail("Ne renvoie pas 404");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        }
    }

    /**
     * Tests for createRelease
     */

    @Test
    public void should_create_release_from_existing_working_copy_when_package_name_and_package_version_query_params_are_provided() throws JsonProcessingException {
        TemplatePackageKey templatePackage = new TemplatePackageKey("pckg_name", "pckg_version", true);
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("pckg_name", "pckg_version");
        when(templatePackages.createRelease(packageInfo)).thenReturn(packageInfo);

        assertThat(withoutAuth("/templates/packages/create_release")
                .queryParam("package_name", "pckg_name")
                .queryParam("package_version", "pckg_version")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(TemplatePackageKey.class, MAPPER.writeValueAsString(templatePackage)))
                .isEqualTo(templatePackage);
    }

    @Test
    public void should_return_400_if_create_release_and_package_name_query_param_is_missing() throws JsonProcessingException {
        TemplatePackageKey templatePackage = new TemplatePackageKey("pckg_name", "pckg_version", true);
        try {
            withoutAuth("/templates/packages/create_release")
                    .queryParam("package_name", "pckg_name")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Response.class, MAPPER.writeValueAsString(templatePackage));
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_create_release_and_package_version_query_param_is_missing() throws JsonProcessingException {
        TemplatePackageKey templatePackage = new TemplatePackageKey("pckg_name", "pckg_version", true);
        try {
            withoutAuth("/templates/packages/create_release")
                    .queryParam("package_version", "pckg_version")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Response.class, MAPPER.writeValueAsString(templatePackage));
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_404_not_found_if_create_release_when_working_copy_missing() throws JsonProcessingException {
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("pckg_name", "pckg_version");
        when(templatePackages.createRelease(packageInfo)).thenThrow(new MissingResourceException(""));

        TemplatePackageKey templatePackage = new TemplatePackageKey("pckg_name", "pckg_version", true);

        try {
            withoutAuth("/templates/packages/create_release")
                    .queryParam("package_name", "pckg_name")
                    .queryParam("package_version", "pckg_version")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(TemplatePackageKey.class, MAPPER.writeValueAsString(templatePackage));
            fail("Ne renvoie pas 404");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_list_of_all_templates_in_workingcopy_package_name_not_valid() {
        try {
            withoutAuth("/templates/packages/%20%09%00/version/workingcopy/templates")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_list_of_all_templates_in_workingcopy_package_version_not_valid() {
        try {
            withoutAuth("/templates/packages/name/%20%09%00/workingcopy/templates")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_list_of_all_templates_in_release_package_name_not_valid() {
        try {
            withoutAuth("/templates/packages/%20%09%00/version/release/templates")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_list_of_all_templates_in_release_package_version_not_valid() {
        try {
            withoutAuth("/templates/packages/name/%20%09%00/release/templates")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_delete_list_of_all_templates_in_workingcopy_package_name_not_valid() {
        try {
            withoutAuth("/templates/packages/%20%09%00/version/workingcopy")
                    .delete(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_delte_list_of_all_templates_in_workingcopy_package_version_not_valid() {
        try {
            withoutAuth("/templates/packages/name/%20%09%00/workingcopy")
                    .delete(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_delete_list_of_all_templates_in_release_package_name_not_valid() {
        try {
            withoutAuth("/templates/packages/%20%09%00/version/release")
                    .delete(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_delte_list_of_all_templates_in_release_package_version_not_valid() {
        try {
            withoutAuth("/templates/packages/name/%20%09%00/release")
                    .delete(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_model_in_release_package_name_not_valid() {
        try {
            withoutAuth("/templates/packages/%20%09%00/version/release/model")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_model_in_release_package_version_not_valid() {
        try {
            withoutAuth("/templates/packages/name/%20%09%00/release/model")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_model_in_workingcopy_package_name_not_valid() {
        try {
            withoutAuth("/templates/packages/%20%09%00/version/workingcopy/model")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_getting_model_in_workingcopy_package_version_not_valid() {
        try {
            withoutAuth("/templates/packages/name/%20%09%00/workingcopy/model")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_get_template_in_workingcopy_package_name_not_valid(){
        try {
            withoutAuth("/templates/packages/%20%09%00/pckg_version/workingcopy/templates/the_name")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_get_template_in_workingcopy_package_version_not_valid(){
        try {
            withoutAuth("/templates/packages/pckg_name/%20%09%00/workingcopy/templates/the_name")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_get_template_in_workingcopy_template_name_not_valid(){
        try {
            withoutAuth("/templates/packages/pckg_name/pckg_version/workingcopy/templates/%20%09%00")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_get_template_in_release_package_name_not_valid(){
        try {
            withoutAuth("/templates/packages/%20%09%00/pckg_version/release/templates/the_name")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_get_template_in_release_package_version_not_valid(){
        try {
            withoutAuth("/templates/packages/pckg_name/%20%09%00/release/templates/the_name")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_get_template_in_release_template_name_not_valid(){
        try {
            withoutAuth("/templates/packages/pckg_name/pckg_version/release/templates/%20%09%00")
                    .get(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_put_templates_in_workingcopy_with_package_name_not_valid() throws JsonProcessingException {
        Template templateBefore = new Template(
                "packages#pckg_name#pckg_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                0L);

        try {
            withoutAuth("/templates/packages/%20%09%00/version/workingcopy/templates")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .put(Template.class, MAPPER.writeValueAsString(templateBefore));
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_put_templates_in_workingcopy_with_package_version_not_valid() throws JsonProcessingException {
        Template templateBefore = new Template(
                "packages#pckg_name#pckg_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                0L);

        try {
            withoutAuth("/templates/packages/name/%20%09%00/workingcopy/templates")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .put(Template.class, MAPPER.writeValueAsString(templateBefore));
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_post_templates_in_workingcopy_with_package_name_not_valid() throws JsonProcessingException {
        Template templateBefore = new Template(
                "packages#pckg_name#pckg_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                0L);
        try {
            withoutAuth("/templates/packages/%20%09%00/version/workingcopy/templates")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Template.class, MAPPER.writeValueAsString(templateBefore));
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_post_templates_in_workingcopy_with_package_version_not_valid() throws JsonProcessingException {
        Template templateBefore = new Template(
                "packages#pckg_name#pckg_version#WORKINGCOPY",
                "template_name",
                "filename",
                "/some/location",
                "some content",
                null,
                0L);
        try {
            withoutAuth("/templates/packages/name/%20%09%00/workingcopy/templates")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Template.class, MAPPER.writeValueAsString(templateBefore));
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_delete_template_in_workingcopy_package_name_not_valid() {
        try {
            withoutAuth("/templates/packages/%20%09%00/version/workingcopy/templates/template_name")
                    .delete(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_delete_template_in_workingcopy_package_version_not_valid() {
        try {
            withoutAuth("/templates/packages/name/%20%09%00/workingcopy/templates/template_name")
                    .delete(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_delete_template_in_workingcopy_template_name_not_valid() {
        try {
            withoutAuth("/templates/packages/name/version/workingcopy/templates/%20%09%00")
                    .delete(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_delete_template_in_workingcopy_template_name_valid() {
        try {
            withoutAuth("/templates/packages/name/version/workingcopy/templates/%00template_name")
                    .delete(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }

    @Test
    public void should_return_400_if_delete_template_in_workingcopy_template_name_valid2() {
        try {
            withoutAuth("/templates/packages/name/version/workingcopy/templates/%09template_name")
                    .delete(Response.class);
            fail("Ne renvoie pas 400");
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        }
    }
}

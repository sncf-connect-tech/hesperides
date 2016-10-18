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

package com.vsct.dt.hesperides.templating.packages;

import com.google.common.eventbus.EventBus;
import com.vsct.dt.hesperides.EventStoreMock;
import com.vsct.dt.hesperides.exception.runtime.DuplicateResourceException;
import com.vsct.dt.hesperides.exception.runtime.IncoherentVersionException;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.exception.runtime.OutOfDateVersionException;
import com.vsct.dt.hesperides.templating.Template;
import com.vsct.dt.hesperides.templating.TemplateData;
import com.vsct.dt.hesperides.util.Release;
import com.vsct.dt.hesperides.util.WorkingCopy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by william_montaz on 28/11/2014.
 */
public class TemplatePackagesTest {

    EventBus       eventBus       = new EventBus();
    EventStoreMock eventStoreMock = new EventStoreMock();

    TemplatePackagesAggregate templatePackages;

    @Before
    public void setUp() throws Exception {
        templatePackages = new TemplatePackagesAggregate(eventBus, eventStoreMock);
        eventStoreMock.reset();
    }

    @After
    public void checkUnwantedEvents() {
        eventStoreMock.verifyUnexpectedEvents();
    }

    @Test
    public void should_create_template_in_working_copy_if_it_does_not_already_exists() {
        eventStoreMock.ignoreEvents();

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("nom du template")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        Template result = templatePackages.createTemplateInWorkingCopy(packageInfo, templateData);

        assertThat(result.getNamespace()).isEqualTo("packages#some_package#package_version#WORKINGCOPY");
        assertThat(result.getName()).isEqualTo("nom du template");
        assertThat(result.getFilename()).isEqualTo("filename");
        assertThat(result.getLocation()).isEqualTo("location");
        assertThat(result.getContent()).isEqualTo("content");
        //Version id should be one when just created
        assertThat(result.getVersionID()).isEqualTo(1L);
    }

    @Test
    public void should_fire_template_created_event_when_creating_template(){
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("nom du template")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        Template result = templatePackages.createTemplateInWorkingCopy(packageInfo, templateData);

        eventStoreMock.checkSavedTheEventOnStream("template_package-some_package-package_version-wc", new TemplateCreatedEvent(new Template("packages#some_package#package_version#WORKINGCOPY", "nom du template", "filename", "location", "content", null, 1L)));
    }

    @Test(expected = Exception.class)
    public void should_not_create_a_template_with_not_parseable_content() {
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("nom du template")
                .withFilename("filename")
                .withLocation("location")
                .withContent("{{not closed")
                .withRights(null)
                .build();

        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData);
    }

    @Test(expected = DuplicateResourceException.class)
    public void should_not_create_a_template_if_it_already_exists(){
        eventStoreMock.ignoreEvents();

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("nom du template")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData);
        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData);
    }

    @Test
    public void should_return_all_templates_in_working_copy() {
        eventStoreMock.ignoreEvents();

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplatePackageWorkingCopyKey packageInfo2 = new TemplatePackageWorkingCopyKey("some_other_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();
        TemplateData templateData2 = TemplateData.withTemplateName("template2")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();
        TemplateData templateData3 = TemplateData.withTemplateName("template3")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData);
        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData2);
        templatePackages.createTemplateInWorkingCopy(packageInfo2, templateData3);

        Set<Template> templatesFromSomePackage = templatePackages.getAllTemplates(packageInfo);
        Set<Template> templatesFromSomeOtherPackage = templatePackages.getAllTemplates(packageInfo2);

        assertThat(templatesFromSomePackage.size()).isEqualTo(2);
        assertThat(templatesFromSomeOtherPackage.size()).isEqualTo(1);
    }

    @Test
    public void should_return_template(){
        eventStoreMock.ignoreEvents();

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData);

        Template template = templatePackages.getTemplate(packageInfo, "template1").get();

        assertThat(template.getNamespace()).isEqualTo("packages#some_package#package_version#WORKINGCOPY");
        assertThat(template.getName()).isEqualTo("template1");
        assertThat(template.getFilename()).isEqualTo("filename");
        assertThat(template.getLocation()).isEqualTo("location");
        assertThat(template.getContent()).isEqualTo("content");
        assertThat(template.getVersionID()).isEqualTo(1L);
    }

    @Test
    public void should_return_empty_option_if_getting_non_existing_template(){
        TemplatePackageKey packageInfo = TemplatePackageKey.withName("some_package").withVersion(WorkingCopy.of("package_version")).build();

        Optional<Template> templateOptional = templatePackages.getTemplate(packageInfo, "template1");

        assertThat(templateOptional.isPresent()).isFalse();
    }

    @Test
    public void should_update_template_in_working_copy_if_it_already_exists() {
        eventStoreMock.ignoreEvents();

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData);

        TemplateData templateDataUpdate = TemplateData.withTemplateName("template1")
                .withFilename("new_filename")
                .withLocation("new_location")
                .withContent("new_content")
                .withRights(null)
                .withVersionID(1L)
                .build();

        Template template = templatePackages.updateTemplateInWorkingCopy(packageInfo, templateDataUpdate);
        assertThat(template.getNamespace()).isEqualTo("packages#some_package#package_version#WORKINGCOPY");
        assertThat(template.getName()).isEqualTo("template1");
        assertThat(template.getFilename()).isEqualTo("new_filename");
        assertThat(template.getLocation()).isEqualTo("new_location");
        assertThat(template.getContent()).isEqualTo("new_content");
        assertThat(template.getVersionID()).isEqualTo(2L);
    }

    @Test
    public void should_fire_template_updated_event_when_has_updated_a_template(){
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData);
        eventStoreMock.reset();

        TemplateData templateDataUpdate = TemplateData.withTemplateName("template1")
                .withFilename("new_filename")
                .withLocation("new_location")
                .withContent("new_content")
                .withRights(null)
                .withVersionID(1L)
                .build();

        Template template = templatePackages.updateTemplateInWorkingCopy(packageInfo, templateDataUpdate);

        eventStoreMock.checkSavedTheEventOnStream("template_package-some_package-package_version-wc", new TemplateUpdatedEvent(new Template("packages#some_package#package_version#WORKINGCOPY", "template1", "new_filename", "new_location", "new_content", null, 2L)));
    }

    @Test(expected = MissingResourceException.class)
    public void should_not_update_template_if_it_does_not_already_exists(){
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackages.updateTemplateInWorkingCopy(packageInfo, templateData);
    }

    @Test(expected = Exception.class)
    public void should_not_update_a_template_with_invalid_content(){
        eventStoreMock.ignoreEvents();

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData);

        TemplateData templateDataUpdate = TemplateData.withTemplateName("template1")
                .withFilename("new_filename")
                .withLocation("new_location")
                .withContent("{{invalid")
                .withRights(null)
                .withVersionID(1L)
                .build();

        Template template = templatePackages.updateTemplateInWorkingCopy(packageInfo, templateDataUpdate);
    }

    @Test(expected = OutOfDateVersionException.class)
    public void should_not_update_template_if_provided_version_id_is_too_low(){
        eventStoreMock.ignoreEvents();

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData);
        //Update to set higher versionID
        templatePackages.updateTemplateInWorkingCopy(packageInfo, templateData);

        TemplateData templateDataUpdate = TemplateData.withTemplateName("template1")
                .withFilename("new_filename")
                .withLocation("new_location")
                .withContent("new_content")
                .withRights(null)
                .withVersionID(1L)
                .build();

        Template template = templatePackages.updateTemplateInWorkingCopy(packageInfo, templateDataUpdate);
    }

    @Test(expected = IncoherentVersionException.class)
    public void should_not_update_template_if_version_id_is_too_high(){
        eventStoreMock.ignoreEvents();

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData);

        TemplateData templateDataUpdate = TemplateData.withTemplateName("template1")
                .withFilename("new_filename")
                .withLocation("new_location")
                .withContent("new_content")
                .withRights(null)
                .withVersionID(2L)
                .build();

        Template template = templatePackages.updateTemplateInWorkingCopy(packageInfo, templateDataUpdate);
    }

    @Test
    public void should_delete_template_in_working_copy_if_it_already_exists() {
        eventStoreMock.ignoreEvents();

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData);

        templatePackages.deleteTemplateInWorkingCopy(packageInfo, "template1");

        Optional<Template> templateOptional = templatePackages.getTemplate(packageInfo, "template1");

        assertThat(templateOptional.isPresent()).isFalse();
    }

    @Test
    public void should_fire_a_template_deleted_event_when_has_deleted_template() {
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData);
        eventStoreMock.reset();

        templatePackages.deleteTemplateInWorkingCopy(packageInfo, "template1");

        eventStoreMock.checkSavedTheEventOnStream("template_package-some_package-package_version-wc", new TemplateDeletedEvent("packages#some_package#package_version#WORKINGCOPY", "template1", 1L));
    }

    @Test(expected = MissingResourceException.class)
    public void should_throw_an_exception_when_deleting_a_non_existing_template(){
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        templatePackages.deleteTemplateInWorkingCopy(packageInfo, "template1");
    }

    @Test
    public void should_create_release_from_the_matching_working_copy_by_copying_templates_with_new_namespace() {
        eventStoreMock.ignoreEvents();

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData1 = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();
        TemplateData templateData2 = TemplateData.withTemplateName("template2")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData1);
        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData2);

        TemplatePackageKey returnedTemplateInfo = templatePackages.createRelease(packageInfo);

        TemplatePackageKey releaseInfo = TemplatePackageKey.withName("some_package").withVersion(Release.of("package_version")).build();
        assertThat(returnedTemplateInfo).isEqualTo(releaseInfo);

        //Workingcopy templates should still exist
        Set<Template> wcTemplates = templatePackages.getAllTemplates(packageInfo);
        assertThat(wcTemplates.size()).isEqualTo(2);

        //Release tmeplates should have been created
        Set<Template> releaseTemplates = templatePackages.getAllTemplates(releaseInfo);

        assertThat(releaseTemplates.size()).isEqualTo(2);
        for(Template template : releaseTemplates){
            assertThat(template.getFilename()).isEqualTo("filename");
            assertThat(template.getLocation()).isEqualTo("location");
            assertThat(template.getContent()).isEqualTo("content");
            assertThat(template.getVersionID()).isEqualTo(1L);
            assertThat(template.getNamespace()).isEqualTo("packages#some_package#package_version#RELEASE");
        }

    }

    @Test(expected = DuplicateResourceException.class)
    public void should_throw_duplicate_ressource_exception_if_release_already_exists_when_creating_a_release() {
        eventStoreMock.ignoreEvents();

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData1 = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();
        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData1);

        templatePackages.createRelease(packageInfo);
        templatePackages.createRelease(packageInfo);
    }

    @Test
    public void create_working_copy_from_workingcopy_should_copy_templates_with_new_namespace() {
        eventStoreMock.ignoreEvents();

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData1 = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();
        TemplateData templateData2 = TemplateData.withTemplateName("template2")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData1);
        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData2);

        TemplatePackageWorkingCopyKey newPackageInfo = new TemplatePackageWorkingCopyKey("new_package", "new_version");
        TemplatePackageKey returnedTemplateInfo = templatePackages.createWorkingCopyFrom(newPackageInfo, packageInfo);

        assertThat(returnedTemplateInfo).isEqualTo(newPackageInfo);

        //Workingcopy templates should still exist
        Set<Template> wcTemplates = templatePackages.getAllTemplates(packageInfo);
        assertThat(wcTemplates.size()).isEqualTo(2);

        //Release tmeplates should have been created
        Set<Template> newTemplates = templatePackages.getAllTemplates(newPackageInfo);

        assertThat(newTemplates.size()).isEqualTo(2);
        for(Template template : newTemplates){
            assertThat(template.getFilename()).isEqualTo("filename");
            assertThat(template.getLocation()).isEqualTo("location");
            assertThat(template.getContent()).isEqualTo("content");
            assertThat(template.getVersionID()).isEqualTo(1L);
            assertThat(template.getNamespace()).isEqualTo("packages#new_package#new_version#WORKINGCOPY");
        }
    }

    @Test
    public void create_working_copy_from_release_should_copy_templates_with_new_namespace() {
        eventStoreMock.ignoreEvents();

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData1 = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();
        TemplateData templateData2 = TemplateData.withTemplateName("template2")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData1);
        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData2);

        templatePackages.createRelease(packageInfo);

        TemplatePackageKey releaseInfo = TemplatePackageKey.withName("some_package").withVersion(Release.of("package_version")).build();
        TemplatePackageWorkingCopyKey newPackageInfo = new TemplatePackageWorkingCopyKey("new_package", "new_version");
        templatePackages.createWorkingCopyFrom(newPackageInfo, releaseInfo);

        //Workingcopy templates should still exist
        Set<Template> releaseTemplates = templatePackages.getAllTemplates(releaseInfo);
        assertThat(releaseTemplates.size()).isEqualTo(2);

        //Release tmeplates should have been created
        Set<Template> newTemplates = templatePackages.getAllTemplates(newPackageInfo);

        assertThat(newTemplates.size()).isEqualTo(2);
        for(Template template : newTemplates){
            assertThat(template.getFilename()).isEqualTo("filename");
            assertThat(template.getLocation()).isEqualTo("location");
            assertThat(template.getContent()).isEqualTo("content");
            assertThat(template.getVersionID()).isEqualTo(1L);
            assertThat(template.getNamespace()).isEqualTo("packages#new_package#new_version#WORKINGCOPY");
        }
    }

    @Test(expected = DuplicateResourceException.class)
    public void should_throw_duplicate_ressource_exception_when_trying_to_create_existing_working_copy() {
        eventStoreMock.ignoreEvents();

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData1 = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData1);

        templatePackages.createWorkingCopyFrom(packageInfo, packageInfo);
    }

    @Test
    public void should_delete_all_templates_related_to_template_package(){
        eventStoreMock.ignoreEvents();

        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData1 = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        TemplateData templateData2 = TemplateData.withTemplateName("template2")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData1);
        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData2);
        //also create the release to test both deletions
        TemplatePackageKey releaseInfo = TemplatePackageKey.withName("some_package").withVersion(Release.of("package_version")).build();
        templatePackages.createRelease(packageInfo);

        assertThat(templatePackages.getAllTemplates(packageInfo).size()).isEqualTo(2);
        assertThat(templatePackages.getAllTemplates(releaseInfo).size()).isEqualTo(2);

        templatePackages.delete(packageInfo);
        templatePackages.delete(releaseInfo);

        assertThat(templatePackages.getAllTemplates(packageInfo).size()).isEqualTo(0);
        assertThat(templatePackages.getAllTemplates(releaseInfo).size()).isEqualTo(0);
    }

    @Test
    public void deleting_template_package_should_fire_an_event(){
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData1 = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackages.createTemplateInWorkingCopy(packageInfo, templateData1);
        eventStoreMock.reset();

        templatePackages.delete(packageInfo);

        eventStoreMock.checkSavedTheEventOnStream("template_package-some_package-package_version-wc", new TemplatePackageDeletedEvent("some_package", "package_version", true));
    }

    @Test(expected = MissingResourceException.class)
    public void should_throw_exception_when_trying_to_delete_unknown_template_package(){
        eventStoreMock.ignoreEvents();
        TemplatePackageKey packageInfo = TemplatePackageKey.withName("some_package").withVersion(WorkingCopy.of("package_version")).build();
        templatePackages.delete(packageInfo);
    }

}

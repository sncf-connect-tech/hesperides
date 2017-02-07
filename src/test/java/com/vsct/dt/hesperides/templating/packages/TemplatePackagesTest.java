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
import com.vsct.dt.hesperides.HesperidesCacheParameter;
import com.vsct.dt.hesperides.HesperidesConfiguration;
import com.vsct.dt.hesperides.exception.runtime.DuplicateResourceException;
import com.vsct.dt.hesperides.exception.runtime.IncoherentVersionException;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.exception.runtime.OutOfDateVersionException;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.storage.RedisEventStore;
import com.vsct.dt.hesperides.storage.RetryRedisConfiguration;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateData;
import com.vsct.dt.hesperides.util.HesperidesCacheConfiguration;
import com.vsct.dt.hesperides.util.ManageableConnectionPoolMock;
import com.vsct.dt.hesperides.util.Release;
import com.vsct.dt.hesperides.util.WorkingCopy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Created by william_montaz on 28/11/2014.
 */
public class TemplatePackagesTest {

    private final EventBus       eventBus       = new EventBus();
    private final ManageableConnectionPoolMock poolRedis = new ManageableConnectionPoolMock();
    private final EventStore eventStore = new RedisEventStore(poolRedis, poolRedis);
    private TemplatePackagesAggregate templatePackagesWithEvent;

    @Before
    public void setUp() throws Exception {
        final RetryRedisConfiguration retryRedisConfiguration = new RetryRedisConfiguration();
        final HesperidesCacheParameter hesperidesCacheParameter = new HesperidesCacheParameter();

        final HesperidesCacheConfiguration hesperidesCacheConfiguration = new HesperidesCacheConfiguration();
        hesperidesCacheConfiguration.setRedisConfiguration(retryRedisConfiguration);
        hesperidesCacheConfiguration.setPlatformTimeline(hesperidesCacheParameter);

        final HesperidesConfiguration hesperidesConfiguration = new HesperidesConfiguration();
        hesperidesConfiguration.setCacheConfiguration(hesperidesCacheConfiguration);

        templatePackagesWithEvent = new TemplatePackagesAggregate(eventBus, eventStore, hesperidesConfiguration);
        poolRedis.reset();
    }

    @After
    public void checkUnwantedEvents() {

    }

    @Test
    public void should_create_template_in_working_copy_if_it_does_not_already_exists() {
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("nom du template")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        Template result = templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData);

        assertThat(result.getNamespace()).isEqualTo("packages#some_package#package_version#WORKINGCOPY");
        assertThat(result.getName()).isEqualTo("nom du template");
        assertThat(result.getFilename()).isEqualTo("filename");
        assertThat(result.getLocation()).isEqualTo("location");
        assertThat(result.getContent()).isEqualTo("content");
        //Version id should be one when just created
        assertThat(result.getVersionID()).isEqualTo(1L);
    }

    @Test
    public void should_fire_template_created_event_when_creating_template() throws IOException, ClassNotFoundException {
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("nom du template")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData);

        poolRedis.checkSavedLastEventOnStream("template_package-some_package-package_version-wc",
                new TemplateCreatedEvent(new Template("packages#some_package#package_version#WORKINGCOPY",
                        "nom du template", "filename", "location", "content", null, 1L)));
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

        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData);
    }

    @Test(expected = DuplicateResourceException.class)
    public void should_not_create_a_template_if_it_already_exists(){
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("nom du template")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData);
        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData);
    }

    @Test
    public void should_return_all_templates_in_working_copy() {
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

        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData);
        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData2);
        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo2, templateData3);

        Set<Template> templatesFromSomePackage = templatePackagesWithEvent.getAllTemplates(packageInfo);
        Set<Template> templatesFromSomeOtherPackage = templatePackagesWithEvent.getAllTemplates(packageInfo2);

        assertThat(templatesFromSomePackage.size()).isEqualTo(2);
        assertThat(templatesFromSomeOtherPackage.size()).isEqualTo(1);
    }

    @Test
    public void should_return_template(){
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData);

        Template template = templatePackagesWithEvent.getTemplate(packageInfo, "template1").get();

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

        Optional<Template> templateOptional = templatePackagesWithEvent.getTemplate(packageInfo, "template1");

        assertThat(templateOptional.isPresent()).isFalse();
    }

    @Test
    public void should_update_template_in_working_copy_if_it_already_exists() {
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData);

        TemplateData templateDataUpdate = TemplateData.withTemplateName("template1")
                .withFilename("new_filename")
                .withLocation("new_location")
                .withContent("new_content")
                .withRights(null)
                .withVersionID(1L)
                .build();

        Template template = templatePackagesWithEvent.updateTemplateInWorkingCopy(packageInfo, templateDataUpdate);
        assertThat(template.getNamespace()).isEqualTo("packages#some_package#package_version#WORKINGCOPY");
        assertThat(template.getName()).isEqualTo("template1");
        assertThat(template.getFilename()).isEqualTo("new_filename");
        assertThat(template.getLocation()).isEqualTo("new_location");
        assertThat(template.getContent()).isEqualTo("new_content");
        assertThat(template.getVersionID()).isEqualTo(2L);
    }

    @Test
    public void should_fire_template_updated_event_when_has_updated_a_template() throws IOException, ClassNotFoundException {
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData);

        TemplateData templateDataUpdate = TemplateData.withTemplateName("template1")
                .withFilename("new_filename")
                .withLocation("new_location")
                .withContent("new_content")
                .withRights(null)
                .withVersionID(1L)
                .build();

        templatePackagesWithEvent.updateTemplateInWorkingCopy(packageInfo, templateDataUpdate);

        poolRedis.checkSavedLastEventOnStream("template_package-some_package-package_version-wc",
                new TemplateUpdatedEvent(new Template("packages#some_package#package_version#WORKINGCOPY",
                        "template1", "new_filename", "new_location", "new_content", null, 2L)));
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

        templatePackagesWithEvent.updateTemplateInWorkingCopy(packageInfo, templateData);
    }

    @Test(expected = Exception.class)
    public void should_not_update_a_template_with_invalid_content(){
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData);

        TemplateData templateDataUpdate = TemplateData.withTemplateName("template1")
                .withFilename("new_filename")
                .withLocation("new_location")
                .withContent("{{invalid")
                .withRights(null)
                .withVersionID(1L)
                .build();

        templatePackagesWithEvent.updateTemplateInWorkingCopy(packageInfo, templateDataUpdate);
    }

    @Test(expected = OutOfDateVersionException.class)
    public void should_not_update_template_if_provided_version_id_is_too_low(){
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData);
        //Update to set higher versionID
        templatePackagesWithEvent.updateTemplateInWorkingCopy(packageInfo, templateData);

        TemplateData templateDataUpdate = TemplateData.withTemplateName("template1")
                .withFilename("new_filename")
                .withLocation("new_location")
                .withContent("new_content")
                .withRights(null)
                .withVersionID(1L)
                .build();

        templatePackagesWithEvent.updateTemplateInWorkingCopy(packageInfo, templateDataUpdate);
    }

    @Test(expected = IncoherentVersionException.class)
    public void should_not_update_template_if_version_id_is_too_high(){
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData);

        TemplateData templateDataUpdate = TemplateData.withTemplateName("template1")
                .withFilename("new_filename")
                .withLocation("new_location")
                .withContent("new_content")
                .withRights(null)
                .withVersionID(2L)
                .build();

        templatePackagesWithEvent.updateTemplateInWorkingCopy(packageInfo, templateDataUpdate);
    }

    @Test
    public void should_delete_template_in_working_copy_if_it_already_exists() {
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData);

        templatePackagesWithEvent.deleteTemplateInWorkingCopy(packageInfo, "template1");

        Optional<Template> templateOptional = templatePackagesWithEvent.getTemplate(packageInfo, "template1");

        assertThat(templateOptional.isPresent()).isFalse();
    }

    @Test
    public void should_fire_a_template_deleted_event_when_has_deleted_template() throws IOException, ClassNotFoundException {
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData);

        templatePackagesWithEvent.deleteTemplateInWorkingCopy(packageInfo, "template1");

        poolRedis.checkSavedLastEventOnStream("template_package-some_package-package_version-wc",
                new TemplateDeletedEvent("packages#some_package#package_version#WORKINGCOPY", "template1", 1L));
    }

    @Test(expected = MissingResourceException.class)
    public void should_throw_an_exception_when_deleting_a_non_existing_template(){
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        templatePackagesWithEvent.deleteTemplateInWorkingCopy(packageInfo, "template1");
    }

    @Test
    public void should_create_release_from_the_matching_working_copy_by_copying_templates_with_new_namespace() {
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

        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData1);
        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData2);

        TemplatePackageKey returnedTemplateInfo = templatePackagesWithEvent.createRelease(packageInfo);

        TemplatePackageKey releaseInfo = TemplatePackageKey.withName("some_package").withVersion(Release.of("package_version")).build();
        assertThat(returnedTemplateInfo).isEqualTo(releaseInfo);

        //Workingcopy templates should still exist
        Set<Template> wcTemplates = templatePackagesWithEvent.getAllTemplates(packageInfo);
        assertThat(wcTemplates.size()).isEqualTo(2);

        //Release tmeplates should have been created
        Set<Template> releaseTemplates = templatePackagesWithEvent.getAllTemplates(releaseInfo);

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
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData1 = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();
        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData1);

        templatePackagesWithEvent.createRelease(packageInfo);
        templatePackagesWithEvent.createRelease(packageInfo);
    }

    @Test
    public void create_working_copy_from_workingcopy_should_copy_templates_with_new_namespace() {
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

        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData1);
        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData2);

        TemplatePackageWorkingCopyKey newPackageInfo = new TemplatePackageWorkingCopyKey("new_package", "new_version");
        TemplatePackageKey returnedTemplateInfo = templatePackagesWithEvent.createWorkingCopyFrom(newPackageInfo, packageInfo);

        assertThat(returnedTemplateInfo).isEqualTo(newPackageInfo);

        //Workingcopy templates should still exist
        Set<Template> wcTemplates = templatePackagesWithEvent.getAllTemplates(packageInfo);
        assertThat(wcTemplates.size()).isEqualTo(2);

        //Release tmeplates should have been created
        Set<Template> newTemplates = templatePackagesWithEvent.getAllTemplates(newPackageInfo);

        assertThat(newTemplates.size()).isEqualTo(2);
        checkTemplates(newTemplates);
    }

    private void checkTemplates(Set<Template> newTemplates) {
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

        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData1);
        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData2);

        templatePackagesWithEvent.createRelease(packageInfo);

        TemplatePackageKey releaseInfo = TemplatePackageKey.withName("some_package").withVersion(Release.of("package_version")).build();
        TemplatePackageWorkingCopyKey newPackageInfo = new TemplatePackageWorkingCopyKey("new_package", "new_version");
        templatePackagesWithEvent.createWorkingCopyFrom(newPackageInfo, releaseInfo);

        //Workingcopy templates should still exist
        Set<Template> releaseTemplates = templatePackagesWithEvent.getAllTemplates(releaseInfo);
        assertThat(releaseTemplates.size()).isEqualTo(2);

        //Release tmeplates should have been created
        Set<Template> newTemplates = templatePackagesWithEvent.getAllTemplates(newPackageInfo);

        assertThat(newTemplates.size()).isEqualTo(2);
        checkTemplates(newTemplates);
    }

    @Test(expected = DuplicateResourceException.class)
    public void should_throw_duplicate_ressource_exception_when_trying_to_create_existing_working_copy() {
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData1 = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData1);

        templatePackagesWithEvent.createWorkingCopyFrom(packageInfo, packageInfo);
    }

    @Test
    public void should_delete_all_templates_related_to_template_package(){
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

        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData1);
        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData2);
        //also create the release to test both deletions
        TemplatePackageKey releaseInfo = TemplatePackageKey.withName("some_package").withVersion(Release.of("package_version")).build();
        templatePackagesWithEvent.createRelease(packageInfo);

        assertThat(templatePackagesWithEvent.getAllTemplates(packageInfo).size()).isEqualTo(2);
        assertThat(templatePackagesWithEvent.getAllTemplates(releaseInfo).size()).isEqualTo(2);

        templatePackagesWithEvent.delete(packageInfo);
        templatePackagesWithEvent.delete(releaseInfo);

        assertThat(templatePackagesWithEvent.getAllTemplates(packageInfo).size()).isEqualTo(0);
        assertThat(templatePackagesWithEvent.getAllTemplates(releaseInfo).size()).isEqualTo(0);
    }

    @Test
    public void deleting_template_package_should_fire_an_event() throws IOException, ClassNotFoundException {
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");
        TemplateData templateData1 = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData1);

        templatePackagesWithEvent.delete(packageInfo);

        poolRedis.checkSavedLastEventOnStream("template_package-some_package-package_version-wc",
                new TemplatePackageDeletedEvent("some_package", "package_version", true));
    }

    @Test(expected = MissingResourceException.class)
    public void should_throw_exception_when_trying_to_delete_unknown_template_package(){
        TemplatePackageKey packageInfo = TemplatePackageKey.withName("some_package").withVersion(WorkingCopy.of("package_version")).build();
        templatePackagesWithEvent.delete(packageInfo);
    }

}

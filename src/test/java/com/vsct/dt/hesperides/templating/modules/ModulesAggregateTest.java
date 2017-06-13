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

package com.vsct.dt.hesperides.templating.modules;

import com.google.common.collect.Sets;
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
import com.vsct.dt.hesperides.templating.packages.TemplatePackagesAggregate;
import com.vsct.dt.hesperides.util.HesperidesCacheConfiguration;
import com.vsct.dt.hesperides.util.ManageableConnectionPoolMock;
import com.vsct.dt.hesperides.util.Release;
import com.vsct.dt.hesperides.util.WorkingCopy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import tests.type.UnitTests;

/**
 * Created by william_montaz on 02/12/2014.
 */
@Category(UnitTests.class)
public class ModulesAggregateTest {

    private final EventBus       eventBus       = new EventBus();
    private final ManageableConnectionPoolMock poolRedis = new ManageableConnectionPoolMock();
    private final EventStore eventStore = new RedisEventStore(poolRedis, poolRedis);
    private final TemplatePackagesAggregate templatePackages = mock(TemplatePackagesAggregate.class);

    private ModulesAggregate modulesWithEvent;

    @Before
    public void setUp() throws Exception {
        final RetryRedisConfiguration retryRedisConfiguration = new RetryRedisConfiguration();
        final HesperidesCacheParameter hesperidesCacheParameter = new HesperidesCacheParameter();

        final HesperidesCacheConfiguration hesperidesCacheConfiguration = new HesperidesCacheConfiguration();
        hesperidesCacheConfiguration.setRedisConfiguration(retryRedisConfiguration);
        hesperidesCacheConfiguration.setPlatformTimeline(hesperidesCacheParameter);

        final HesperidesConfiguration hesperidesConfiguration = new HesperidesConfiguration();
        hesperidesConfiguration.setCacheConfiguration(hesperidesCacheConfiguration);

        poolRedis.reset();
        modulesWithEvent = new ModulesAggregate(eventBus, eventStore, templatePackages, hesperidesConfiguration);
    }

    @After
    public void checkUnwantedEvents() {
        
    }

    /**
     * Create working copy
     */
    @Test
    public void should_create_working_copy_with_provided_technos_and_no_templates() {
        ModuleKey moduleKey = ModuleKey.withModuleName("my_module")
                .withVersion(WorkingCopy.of("the_version"))
                .build();

        Techno techno = new Techno("tomcat", "1", false);

        Module module = modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        assertThat(module.getName()).isEqualTo("my_module");
        assertThat(module.getVersion()).isEqualTo("the_version");
        assertThat(module.isWorkingCopy()).isTrue();
        assertThat(module.getVersionID()).isEqualTo(1L);
    }

    @Test
    public void working_copy_creation_should_fire_a_module_created_event() throws IOException, ClassNotFoundException {
        ModuleKey moduleKey = ModuleKey.withModuleName("my_module")
                .withVersion(WorkingCopy.of("the_version"))
                .build();

        Techno techno = new Techno("tomcat", "1", false);

        Module module = modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        poolRedis.checkSavedLastEventOnStream("module-my_module-the_version-wc",
                new ModuleCreatedEvent(module, Sets.newHashSet()));
    }

    @Test(expected = DuplicateResourceException.class)
    public void should_not_create_working_copy_if_it_already_exists(){
        ModuleKey moduleKey = ModuleKey.withModuleName("my_module")
                .withVersion(WorkingCopy.of("the_version"))
                .build();

        Techno techno = new Techno("tomcat", "1", false);

        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));
        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));
    }

    /**
     * GetModule
     */
    @Test
    public void should_return_module(){
        ModuleKey moduleKey = ModuleKey.withModuleName("my_module")
                .withVersion(WorkingCopy.of("the_version"))
                .build();

        Techno techno = new Techno("tomcat", "1", false);

        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        Module module = modulesWithEvent.getModule(moduleKey).get();

        assertThat(module.getName()).isEqualTo("my_module");
        assertThat(module.getVersion()).isEqualTo("the_version");
        assertThat(module.isWorkingCopy()).isTrue();
        assertThat(module.getVersionID()).isEqualTo(1L);
        assertThat(module.getTechnos()).isEqualTo(Sets.newHashSet(techno));
    }

    @Test
    public void should_return_empty_optional_if_module_is_not_found(){
        ModuleKey moduleKey = ModuleKey.withModuleName("my_module")
                .withVersion(WorkingCopy.of("the_version"))
                .build();

        Optional<Module> moduleOptional = modulesWithEvent.getModule(moduleKey);

        assertThat(moduleOptional.isPresent()).isFalse();
    }

    /**
     * Update working copy
     */
    @Test
    public void should_update_module_working_copy(){
        ModuleKey moduleKey = ModuleKey.withModuleName("my_module")
                .withVersion(WorkingCopy.of("the_version"))
                .build();

        Techno techno = new Techno("tomcat", "1", false);
        Module module = new Module(moduleKey, Sets.newHashSet(techno));
        modulesWithEvent.createWorkingCopy(module);

        Techno newTechno = new Techno("tomcat", "2", false);

        Module newModule = new Module(moduleKey, Sets.newHashSet(newTechno));
        modulesWithEvent.updateWorkingCopy(newModule);

        Module updated = modulesWithEvent.getModule(moduleKey).get();

        assertThat(updated.getTechnos()).isEqualTo(Sets.newHashSet(newTechno));
    }

    @Test
    public void should_fire_module_working_copy_updated_event() throws IOException, ClassNotFoundException {
        ModuleKey moduleKey = ModuleKey.withModuleName("my_module")
                .withVersion(WorkingCopy.of("the_version"))
                .build();

        Techno techno = new Techno("tomcat", "1", false);

        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        Techno newTechno = new Techno("tomcat", "2", false);
        modulesWithEvent.updateWorkingCopy(new Module(moduleKey, Sets.newHashSet(newTechno)));

        poolRedis.checkSavedLastEventOnStream("module-my_module-the_version-wc",
                new ModuleWorkingCopyUpdatedEvent(
                    new Module("my_module", "the_version", true, Sets.newHashSet(newTechno), 2L)
        ));
    }

    @Test(expected = MissingResourceException.class)
    public void should_not_update_module_if_not_existing_at_first(){
        ModuleKey moduleKey = ModuleKey.withModuleName("my_module")
                .withVersion(WorkingCopy.of("the_version"))
                .build();
        modulesWithEvent.updateWorkingCopy(new Module(moduleKey, Sets.newHashSet()));
    }

    @Test(expected = OutOfDateVersionException.class)
    public void should_not_update_module_if_providing_too_low_versionid(){
        ModuleKey moduleKey = ModuleKey.withModuleName("my_module")
                .withVersion(WorkingCopy.of("the_version"))
                .build();

        Techno techno = new Techno("tomcat", "1", false);

        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));
        //Update to increase version ID
        modulesWithEvent.updateWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        Techno newTechno = new Techno("tomcat", "2", false);
        ModuleKey moduleKey2 = ModuleKey.withModuleName("my_module")
                .withVersion(WorkingCopy.of("the_version"))
                .build();
        modulesWithEvent.updateWorkingCopy(new Module(moduleKey2, Sets.newHashSet(newTechno)));
    }

    @Test(expected = IncoherentVersionException.class)
    public void should_not_update_module_if_providing_to_high_versionid(){
        ModuleKey moduleKey = ModuleKey.withModuleName("my_module")
                .withVersion(WorkingCopy.of("the_version"))
                .build();

        Techno techno = new Techno("tomcat", "1", false);

        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        Techno newTechno = new Techno("tomcat", "2", false);
        ModuleKey moduleKey2 = ModuleKey.withModuleName("my_module")
                .withVersion(WorkingCopy.of("the_version"))
                .build();
        modulesWithEvent.updateWorkingCopy(new Module(moduleKey2, Sets.newHashSet(newTechno), 2L));
    }

    /**
     * Create template in working copy
     */

    @Test
    public void should_create_template_in_working_copy_if_it_does_not_already_exists() {
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("my_module", "the_version");
        TemplateData templateData = TemplateData.withTemplateName("nom du template")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        Techno techno = new Techno("tomcat", "1", false);
        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        Template result = modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);

        assertThat(result.getNamespace()).isEqualTo("modules#my_module#the_version#WORKINGCOPY");
        assertThat(result.getName()).isEqualTo("nom du template");
        assertThat(result.getFilename()).isEqualTo("filename");
        assertThat(result.getLocation()).isEqualTo("location");
        assertThat(result.getContent()).isEqualTo("content");
        //Version id should be one when just created
        assertThat(result.getVersionID()).isEqualTo(1L);
    }

    @Test
    public void should_fire_template_created_event_when_creating_template() throws IOException, ClassNotFoundException {
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("my_module", "the_version");
        TemplateData templateData = TemplateData.withTemplateName("nom du template")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();
        Techno techno = new Techno("tomcat", "1", false);
        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);

        poolRedis.checkSavedLastEventOnStream("module-my_module-the_version-wc",
                new ModuleTemplateCreatedEvent("my_module", "the_version",
                        new Template("modules#my_module#the_version#WORKINGCOPY", "nom du template", "filename",
                                "location", "content", null, 1L)));
    }

    @Test(expected = Exception.class)
    public void should_not_create_a_template_with_not_parseable_content() {
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("my_module", "the_version");
        TemplateData templateData = TemplateData.withTemplateName("nom du template")
                .withFilename("filename")
                .withLocation("location")
                .withContent("{{not closed")
                .withRights(null)
                .build();
        Techno techno = new Techno("tomcat", "1", false);
        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);
    }

    @Test(expected = DuplicateResourceException.class)
    public void should_not_create_a_template_if_it_already_exists(){
        Techno techno = new Techno("tomcat", "1", false);

        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("my_module", "the_version");
        TemplateData templateData = TemplateData.withTemplateName("nom du template")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);

        modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);
    }

    /**
     * update template in working copy
     */

    @Test
    public void should_update_template_in_working_copy_if_it_already_exists() {
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("my_module", "the_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();
        Techno techno = new Techno("tomcat", "1", false);
        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);

        TemplateData templateDataUpdate = TemplateData.withTemplateName("template1")
                .withFilename("new_filename")
                .withLocation("new_location")
                .withContent("new_content")
                .withRights(null)
                .withVersionID(1L)
                .build();

        Template template = modulesWithEvent.updateTemplateInWorkingCopy(moduleKey, templateDataUpdate);
        assertThat(template.getNamespace()).isEqualTo("modules#my_module#the_version#WORKINGCOPY");
        assertThat(template.getName()).isEqualTo("template1");
        assertThat(template.getFilename()).isEqualTo("new_filename");
        assertThat(template.getLocation()).isEqualTo("new_location");
        assertThat(template.getContent()).isEqualTo("new_content");
        assertThat(template.getVersionID()).isEqualTo(2L);
    }

    @Test
    public void should_fire_template_updated_event_when_has_updated_a_template() throws IOException, ClassNotFoundException {
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("my_module", "the_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();
        Techno techno = new Techno("tomcat", "1", false);
        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);

        TemplateData templateDataUpdate = TemplateData.withTemplateName("template1")
                .withFilename("new_filename")
                .withLocation("new_location")
                .withContent("new_content")
                .withRights(null)
                .withVersionID(1L)
                .build();

        modulesWithEvent.updateTemplateInWorkingCopy(moduleKey, templateDataUpdate);

        poolRedis.checkSavedLastEventOnStream("module-my_module-the_version-wc",
                new ModuleTemplateUpdatedEvent("my_module", "the_version",
                        new Template("modules#my_module#the_version#WORKINGCOPY", "template1", "new_filename",
                                "new_location", "new_content", null, 2L)));
    }

    @Test(expected = MissingResourceException.class)
    public void should_not_update_template_if_it_does_not_already_exists(){
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("my_module", "the_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();
        Techno techno = new Techno("tomcat", "1", false);
        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        modulesWithEvent.updateTemplateInWorkingCopy(moduleKey, templateData);
    }

    @Test(expected = Exception.class)
    public void should_not_update_a_template_with_invalid_content(){
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("my_module", "the_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();
        Techno techno = new Techno("tomcat", "1", false);
        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);

        TemplateData templateDataUpdate = TemplateData.withTemplateName("template1")
                .withFilename("new_filename")
                .withLocation("new_location")
                .withContent("{{invalid")
                .withRights(null)
                .withVersionID(1L)
                .build();

        modulesWithEvent.updateTemplateInWorkingCopy(moduleKey, templateDataUpdate);
    }

    @Test(expected = OutOfDateVersionException.class)
    public void should_not_update_template_if_provided_version_id_is_too_low(){
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("my_module", "the_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();
        Techno techno = new Techno("tomcat", "1", false);
        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);

        //Update to increase versionID
        modulesWithEvent.updateTemplateInWorkingCopy(moduleKey, templateData);

        TemplateData templateDataUpdate = TemplateData.withTemplateName("template1")
                .withFilename("new_filename")
                .withLocation("new_location")
                .withContent("new_content")
                .withRights(null)
                .withVersionID(1L)
                .build();

        modulesWithEvent.updateTemplateInWorkingCopy(moduleKey, templateDataUpdate);
    }

    @Test(expected = IncoherentVersionException.class)
    public void should_not_update_template_if_version_id_is_too_high(){
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("my_module", "the_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();
        Techno techno = new Techno("tomcat", "1", false);
        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);

        TemplateData templateDataUpdate = TemplateData.withTemplateName("template1")
                .withFilename("new_filename")
                .withLocation("new_location")
                .withContent("new_content")
                .withRights(null)
                .withVersionID(2L)
                .build();

        modulesWithEvent.updateTemplateInWorkingCopy(moduleKey, templateDataUpdate);
    }

    /**
     * Delete template in working copy
     */

    @Test
    public void should_delete_template_in_working_copy_if_it_already_exists() {
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("my_module", "the_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();
        Techno techno = new Techno("tomcat", "1", false);
        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);

        modulesWithEvent.deleteTemplateInWorkingCopy(moduleKey, "template1");

        Optional<Template> templateOptional = modulesWithEvent.getTemplate(moduleKey, "template1");

        assertThat(templateOptional.isPresent()).isFalse();
    }

    @Test
    public void should_fire_a_template_deleted_event_when_has_deleted_template() throws IOException, ClassNotFoundException {
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("my_module", "the_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();
        Techno techno = new Techno("tomcat", "1", false);
        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);

        modulesWithEvent.deleteTemplateInWorkingCopy(moduleKey, "template1");

        poolRedis.checkSavedLastEventOnStream("module-my_module-the_version-wc",
                new ModuleTemplateDeletedEvent("my_module", "the_version", "template1"));
    }

    @Test(expected = MissingResourceException.class)
    public void should_throw_an_exception_when_deleting_a_non_existing_template(){
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("my_module", "the_version");
        Techno techno = new Techno("tomcat", "1", false);
        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        modulesWithEvent.deleteTemplateInWorkingCopy(moduleKey, "template1");
    }

    /**
     * Get all templates (workingcopy/release)
     */

    @Test
    public void should_return_all_templates_in_working_copy() {
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("my_module", "the_version");
        ModuleWorkingCopyKey moduleKey2 = new ModuleWorkingCopyKey("my_other_module", "the_version");
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
        Techno techno = new Techno("tomcat", "1", false);
        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));
        modulesWithEvent.createWorkingCopy(new Module(moduleKey2, Sets.newHashSet()));

        modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);
        modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData2);
        modulesWithEvent.createTemplateInWorkingCopy(moduleKey2, templateData3);

        List<Template> templatesFromSomeModule = modulesWithEvent.getAllTemplates(moduleKey);
        List<Template> templatesFromSomeOtherModule = modulesWithEvent.getAllTemplates(moduleKey2);

        assertThat(templatesFromSomeModule.size()).isEqualTo(2);
        assertThat(templatesFromSomeOtherModule.size()).isEqualTo(1);
    }

    /**
     * Get template (In release / in working copy)
     */

    @Test
    public void should_return_template(){
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("my_module", "the_version");
        TemplateData templateData = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();
        Techno techno = new Techno("tomcat", "1", false);
        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);

        Template template = modulesWithEvent.getTemplate(moduleKey, "template1").get();

        assertThat(template.getNamespace()).isEqualTo("modules#my_module#the_version#WORKINGCOPY");
        assertThat(template.getName()).isEqualTo("template1");
        assertThat(template.getFilename()).isEqualTo("filename");
        assertThat(template.getLocation()).isEqualTo("location");
        assertThat(template.getContent()).isEqualTo("content");
        assertThat(template.getVersionID()).isEqualTo(1L);
    }

    @Test
    public void should_return_empty_option_if_getting_non_existing_template(){
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("my_module", "the_version");
        Techno techno = new Techno("tomcat", "1", false);
        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        Optional<Template> templateOptional = modulesWithEvent.getTemplate(moduleKey, "template1");

        assertThat(templateOptional.isPresent()).isFalse();
    }

    /**
     * Create working copy from
     */

    @Test
    public void create_working_copy_from_workingcopy_should_copy_templates_with_new_namespace() {
        ModuleWorkingCopyKey fromModuleKey = new ModuleWorkingCopyKey("my_module", "the_version");;
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

        Techno techno = new Techno("tomcat", "1", false);
        modulesWithEvent.createWorkingCopy(new Module(fromModuleKey, Sets.newHashSet(techno)));

        modulesWithEvent.createTemplateInWorkingCopy(fromModuleKey, templateData1);
        modulesWithEvent.createTemplateInWorkingCopy(fromModuleKey, templateData2);

        ModuleWorkingCopyKey newModuleKey = new ModuleWorkingCopyKey("new_module", "new_version");
        Module returnedModule = modulesWithEvent.createWorkingCopyFrom(newModuleKey, fromModuleKey);
        assertThat(returnedModule.getName()).isEqualTo("new_module");
        assertThat(returnedModule.getVersion()).isEqualTo("new_version");
        assertThat(returnedModule.isWorkingCopy()).isTrue();
        assertThat(returnedModule.getTechnos()).isEqualTo(Sets.newHashSet(techno));

        //Workingcopy templates should still exist
        List<Template> wcTemplates = modulesWithEvent.getAllTemplates(fromModuleKey);
        assertThat(wcTemplates.size()).isEqualTo(2);

        //New templates should have been created
        List<Template> newTemplates = modulesWithEvent.getAllTemplates(newModuleKey);

        assertThat(newTemplates.size()).isEqualTo(2);
        for(int i = 0; i<2; i++) {
            Template template = newTemplates.get(i);
            assertThat(template.getFilename()).isEqualTo("filename");
            assertThat(template.getLocation()).isEqualTo("location");
            assertThat(template.getContent()).isEqualTo("content");
            assertThat(template.getVersionID()).isEqualTo(1L);
            assertThat(template.getNamespace()).isEqualTo("modules#new_module#new_version#WORKINGCOPY");
        }

    }

    @Test
    public void create_working_copy_from_release_should_copy_templates_with_new_namespace() {
        ModuleWorkingCopyKey fromModuleKey = new ModuleWorkingCopyKey("my_module", "the_version");
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

        Techno techno = new Techno("tomcat", "1", false);
        modulesWithEvent.createWorkingCopy(new Module(fromModuleKey, Sets.newHashSet(techno)));

        modulesWithEvent.createTemplateInWorkingCopy(fromModuleKey, templateData1);
        modulesWithEvent.createTemplateInWorkingCopy(fromModuleKey, templateData2);

        modulesWithEvent.createRelease(fromModuleKey, "release_version");

        ModuleKey releaseInfo = ModuleKey.withModuleName("my_module")
                .withVersion(Release.of("release_version"))
                .build();
        ModuleWorkingCopyKey newModuleKey = new ModuleWorkingCopyKey("new_module", "new_version");
        Module returnedModule = modulesWithEvent.createWorkingCopyFrom(newModuleKey, releaseInfo);
        assertThat(returnedModule.getName()).isEqualTo("new_module");
        assertThat(returnedModule.getVersion()).isEqualTo("new_version");
        assertThat(returnedModule.isWorkingCopy()).isTrue();
        assertThat(returnedModule.getTechnos()).isEqualTo(Sets.newHashSet(techno));

        //Workingcopy templates should still exist
        List<Template> wcTemplates = modulesWithEvent.getAllTemplates(fromModuleKey);
        assertThat(wcTemplates.size()).isEqualTo(2);

        //New templates should have been created
        List<Template> newTemplates = modulesWithEvent.getAllTemplates(newModuleKey);

        assertThat(newTemplates.size()).isEqualTo(2);
        for(int i = 0; i<2; i++) {
            Template template = newTemplates.get(i);
            assertThat(template.getFilename()).isEqualTo("filename");
            assertThat(template.getLocation()).isEqualTo("location");
            assertThat(template.getContent()).isEqualTo("content");
            assertThat(template.getVersionID()).isEqualTo(1L);
            assertThat(template.getNamespace()).isEqualTo("modules#new_module#new_version#WORKINGCOPY");
        }

    }

    @Test(expected = DuplicateResourceException.class)
    public void should_throw_duplicate_ressource_exception_when_trying_to_create_existing_working_copy() {
        ModuleWorkingCopyKey fromModuleKey = new ModuleWorkingCopyKey("my_module", "the_version");

        Techno techno = new Techno("tomcat", "1", false);
        modulesWithEvent.createWorkingCopy(new Module(fromModuleKey, Sets.newHashSet(techno)));

        modulesWithEvent.createWorkingCopyFrom(fromModuleKey, fromModuleKey);
    }

    /**
     * Create release
     */

    @Test
    public void should_create_release_from_the_matching_working_copy_with_new_version() {
        ModuleWorkingCopyKey fromModuleKey = new ModuleWorkingCopyKey("my_module", "the_version");
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

        Techno techno = new Techno("tomcat", "1", false);
        modulesWithEvent.createWorkingCopy(new Module(fromModuleKey, Sets.newHashSet(techno)));

        modulesWithEvent.createTemplateInWorkingCopy(fromModuleKey, templateData1);
        modulesWithEvent.createTemplateInWorkingCopy(fromModuleKey, templateData2);

        Module returnedModule = modulesWithEvent.createRelease(fromModuleKey, "new_release_version");
        assertThat(returnedModule.getName()).isEqualTo("my_module");
        assertThat(returnedModule.getVersion()).isEqualTo("new_release_version");
        assertThat(returnedModule.isWorkingCopy()).isFalse();
        assertThat(returnedModule.getTechnos()).isEqualTo(Sets.newHashSet(techno));

        //Workingcopy templates should still exist
        List<Template> wcTemplates = modulesWithEvent.getAllTemplates(fromModuleKey);
        assertThat(wcTemplates.size()).isEqualTo(2);

        //Release templates should have been created
        ModuleKey releaseInfo = ModuleKey.withModuleName("my_module").withVersion(Release.of("new_release_version")).build();
        List<Template> releaseTemplates = modulesWithEvent.getAllTemplates(releaseInfo);

        assertThat(releaseTemplates.size()).isEqualTo(2);
        for(int i = 0; i<2; i++) {
            Template template = releaseTemplates.get(i);
            assertThat(template.getFilename()).isEqualTo("filename");
            assertThat(template.getLocation()).isEqualTo("location");
            assertThat(template.getContent()).isEqualTo("content");
            assertThat(template.getVersionID()).isEqualTo(1L);
            assertThat(template.getNamespace()).isEqualTo("modules#my_module#new_release_version#RELEASE");
        }

    }

    @Test(expected = DuplicateResourceException.class)
    public void should_throw_duplicate_ressource_exception_if_release_already_exists_when_creating_a_release() {
        ModuleWorkingCopyKey fromModuleKey = new ModuleWorkingCopyKey("my_module", "the_version");
        ModuleWorkingCopyKey fromModuleKey2 = new ModuleWorkingCopyKey("my_module", "the_version");

        assertThat(fromModuleKey).isEqualTo(fromModuleKey2);
        TemplateData templateData1 = TemplateData.withTemplateName("template1")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();
        modulesWithEvent.createWorkingCopy(new Module(fromModuleKey, Sets.newHashSet()));
        modulesWithEvent.createTemplateInWorkingCopy(fromModuleKey, templateData1);

        modulesWithEvent.createRelease(fromModuleKey, "version");
        modulesWithEvent.createRelease(fromModuleKey, "version");
    }

    @Test
    public void should_delete_module_and_all_templates_related_to_module(){
        ModuleWorkingCopyKey workingCopy = new ModuleWorkingCopyKey("my_module", "the_version");
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

        modulesWithEvent.createWorkingCopy(new Module(workingCopy, Sets.newHashSet()));
        modulesWithEvent.createTemplateInWorkingCopy(workingCopy, templateData1);
        modulesWithEvent.createTemplateInWorkingCopy(workingCopy, templateData2);

        //also create the release to test both deletions
        ModuleKey release = ModuleKey.withModuleName("my_module")
                .withVersion(Release.of("the_version"))
                .build();
        modulesWithEvent.createRelease(workingCopy, "the_version");

        assertThat(modulesWithEvent.getModule(workingCopy).isPresent()).isTrue();
        assertThat(modulesWithEvent.getAllTemplates(workingCopy).size()).isEqualTo(2);
        assertThat(modulesWithEvent.getModule(release).isPresent()).isTrue();
        assertThat(modulesWithEvent.getAllTemplates(release).size()).isEqualTo(2);

        modulesWithEvent.delete(workingCopy);
        modulesWithEvent.delete(release);

        assertThat(modulesWithEvent.getModule(workingCopy).isPresent()).isFalse();
        assertThat(modulesWithEvent.getAllTemplates(workingCopy).size()).isEqualTo(0);
        assertThat(modulesWithEvent.getModule(release).isPresent()).isFalse();
        assertThat(modulesWithEvent.getAllTemplates(release).size()).isEqualTo(0);
    }

    @Test
    public void deleting_module_should_fire_an_event() throws IOException, ClassNotFoundException {
        ModuleKey workingCopy = ModuleKey.withModuleName("my_module")
                .withVersion(WorkingCopy.of("the_version"))
                .build();
        modulesWithEvent.createWorkingCopy(new Module(workingCopy, Sets.newHashSet()));

        modulesWithEvent.delete(workingCopy);

        poolRedis.checkSavedLastEventOnStream("module-my_module-the_version-wc",
                new ModuleDeletedEvent(workingCopy.getName(), workingCopy.getVersionName(),
                        workingCopy.isWorkingCopy()));
    }

    @Test(expected = MissingResourceException.class)
    public void should_throw_exception_when_trying_to_delete_unknown_module(){
        ModuleKey workingCopy = ModuleKey.withModuleName("my_module")
                .withVersion(WorkingCopy.of("the_version"))
                .build();
        modulesWithEvent.delete(workingCopy);
    }
}

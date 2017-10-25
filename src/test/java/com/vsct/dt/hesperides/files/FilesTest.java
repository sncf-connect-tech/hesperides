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

package com.vsct.dt.hesperides.files;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.vsct.dt.hesperides.HesperidesCacheParameter;
import com.vsct.dt.hesperides.HesperidesConfiguration;
import com.vsct.dt.hesperides.applications.ApplicationsAggregate;
import com.vsct.dt.hesperides.applications.PlatformKey;
import com.vsct.dt.hesperides.applications.SnapshotRegistry;
import com.vsct.dt.hesperides.applications.SnapshotRegistryInterface;
import com.vsct.dt.hesperides.exception.runtime.MissingResourceException;
import com.vsct.dt.hesperides.resources.IterableValorisation;
import com.vsct.dt.hesperides.resources.KeyValueValorisation;
import com.vsct.dt.hesperides.resources.PermissionAwareApplicationsProxy;
import com.vsct.dt.hesperides.resources.Properties;

import com.vsct.dt.hesperides.security.UserContext;
import com.vsct.dt.hesperides.security.model.User;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.storage.RedisEventStore;
import com.vsct.dt.hesperides.storage.RetryRedisConfiguration;
import com.vsct.dt.hesperides.templating.models.HesperidesPropertiesModel;
import com.vsct.dt.hesperides.templating.modules.Module;
import com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyKey;
import com.vsct.dt.hesperides.templating.modules.ModulesAggregate;
import com.vsct.dt.hesperides.templating.modules.Techno;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateData;

import com.vsct.dt.hesperides.templating.modules.template.TemplateSlurper;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageWorkingCopyKey;
import com.vsct.dt.hesperides.templating.packages.TemplatePackagesAggregate;
import com.vsct.dt.hesperides.templating.platform.ApplicationModuleData;
import com.vsct.dt.hesperides.templating.platform.InstanceData;
import com.vsct.dt.hesperides.templating.platform.KeyValueValorisationData;
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import com.vsct.dt.hesperides.util.HesperidesCacheConfiguration;
import com.vsct.dt.hesperides.util.ManageableConnectionPoolMock;
import com.vsct.dt.hesperides.util.converter.PropertiesConverter;
import com.vsct.dt.hesperides.util.converter.impl.DefaultPropertiesConverter;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import tests.type.UnitTests;

/**
 * Created by william_montaz on 03/09/14.
 * Updated by Tidiane SIDIBE on 09/11/2016
 */
@Category(UnitTests.class)
public class FilesTest {

    private final EventBus       eventBus       = new EventBus();
    private final ManageableConnectionPoolMock poolRedis = new ManageableConnectionPoolMock();
    private final EventStore eventStore = new RedisEventStore(poolRedis, poolRedis, () -> System.currentTimeMillis());
    private ApplicationsAggregate     applications;
    private ModulesAggregate          modules;
    private TemplatePackagesAggregate templatePackages;

    private SnapshotRegistryInterface snapshotRegistryInterface = mock(SnapshotRegistry.class);

    private Files files, hiddingFiles;
    private final String comment = "Test comment";
    private final HesperidesPropertiesModel model = HesperidesPropertiesModel.empty();

    private static final PropertiesConverter PROPERTIES_CONVERTER = new DefaultPropertiesConverter();

    private TemplatePackagesAggregate templatePackagesWithEvent;
    private ApplicationsAggregate applicationsWithEvent;
    private ModulesAggregate modulesWithEvent;
    private Files filesWithEvent;

    @Before
    public void setupMocks() throws Exception {
        final RetryRedisConfiguration retryRedisConfiguration = new RetryRedisConfiguration();
        final HesperidesCacheParameter hesperidesCacheParameter = new HesperidesCacheParameter();

        final HesperidesCacheConfiguration hesperidesCacheConfiguration = new HesperidesCacheConfiguration();
        hesperidesCacheConfiguration.setRedisConfiguration(retryRedisConfiguration);
        hesperidesCacheConfiguration.setPlatformTimeline(hesperidesCacheParameter);

        final HesperidesConfiguration hesperidesConfiguration = new HesperidesConfiguration();
        hesperidesConfiguration.setCacheConfiguration(hesperidesCacheConfiguration);

        templatePackages = new TemplatePackagesAggregate(eventBus, eventStore, hesperidesConfiguration);
        applications = new ApplicationsAggregate(eventBus, eventStore, snapshotRegistryInterface, hesperidesConfiguration);
        modules = new ModulesAggregate(eventBus, eventStore, templatePackages, hesperidesConfiguration);
        files = new Files(applications, modules, templatePackages);
        hiddingFiles = new Files(new PermissionAwareApplicationsProxy(applications, new UserContext() {
            @Override
            public User getCurrentUser() {
                return new User("user", false, false);
            }

            @Override
            public void setCurrentUser(User user) {
                // no need !
            }
        }), modules, templatePackages);

        templatePackagesWithEvent = new TemplatePackagesAggregate(eventBus, eventStore, hesperidesConfiguration);
        applicationsWithEvent = new ApplicationsAggregate(eventBus, eventStore, snapshotRegistryInterface, hesperidesConfiguration);
        modulesWithEvent = new ModulesAggregate(eventBus, eventStore, templatePackagesWithEvent, hesperidesConfiguration);
        filesWithEvent = new Files(applicationsWithEvent, modulesWithEvent, templatePackagesWithEvent);

        poolRedis.reset();
    }

    @Test
    public void shouldGetFileListItemsWithAppVersionPltfmUnitAndContext() throws Exception {
        /*
        Create 2 technos with a template in each
         */
        TemplatePackageWorkingCopyKey techno1Info = new TemplatePackageWorkingCopyKey("techno1", "v1");
        TemplateData template1Data = TemplateData.withTemplateName("template_from_techno1")
                .withFilename("techno1_{{key}}")
                .withLocation("loc_techno1_{{key}}")
                .withContent("content")
                .withRights(null)
                .build();
        templatePackagesWithEvent.createTemplateInWorkingCopy(techno1Info, template1Data);

        TemplatePackageWorkingCopyKey techno2Info = new TemplatePackageWorkingCopyKey("techno2", "v1");
        TemplateData template2Data = TemplateData.withTemplateName("template_from_techno2")
                .withFilename("techno2_{{key}}")
                .withLocation("loc_techno2_{{key}}")
                .withContent("content")
                .withRights(null)
                .build();
        templatePackagesWithEvent.createTemplateInWorkingCopy(techno2Info, template2Data);

        /* Create a module and add a template specific to the module
           This template filename and location relies on a property key named key
           This property key will rely on an instance property named instance_key

           The module relies on 2 technos and thus will need to show the technos templates in the list
         */
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("the_module_name", "the_module_version");
        TemplateData template3Data = TemplateData.withTemplateName("template_from_module")
                .withFilename("module_{{key}}")
                .withLocation("loc_module_{{key}}")
                .withContent("content")
                .withRights(null)
                .build();
        Module module = new Module(moduleKey, Sets.newHashSet(new Techno("techno1", "v1", true), new Techno("techno2", "v1", true)), 1L);
        modulesWithEvent.createWorkingCopy(module);
        modulesWithEvent.createTemplateInWorkingCopy(moduleKey, template3Data);

        /*
        Create a platform "the_pltfm_name", belonging to "the_app_name"
        This platform uses the module defined above in the path "#path#1" and another one to introduce noise....
        The interesting module defines 2 instances (one for noise...), one of "the_instance_name" defines the "instance_key" as stated above
         */
        InstanceData instance1 = InstanceData.withInstanceName("the_instance_name")
                .withKeyValue(Sets.newHashSet(new KeyValueValorisationData("instance_key", "instance"))).build();
        InstanceData instance2 = InstanceData.withInstanceName("not_the_instance_name")
                .withKeyValue(Sets.newHashSet()).build();

        ApplicationModuleData applicationModule1 = ApplicationModuleData
                .withApplicationName("the_module_name")
                .withVersion("the_module_version")
                .withPath("path#1")
                .withId(1)
                .withInstances(Sets.newHashSet(instance1, instance2))
                .isWorkingcopy()
                .build();

        ApplicationModuleData applicationModule2 = ApplicationModuleData
                .withApplicationName("mod2_name")
                .withVersion("mod_version")
                .withPath("path#2")
                .withId(2)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        Set<ApplicationModuleData> appModules = Sets.newHashSet(applicationModule1, applicationModule2);
        PlatformKey platformKey = PlatformKey.withName("the_pltfm_name")
                .withApplicationName("the_app_name")
                .build();

        PlatformData.IBuilder builder = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(appModules)
                .withVersion(1L)
                .isProduction();

        applicationsWithEvent.createPlatform(builder.build());

        /*
        Add the properties for "the_module_name"
         */
        Properties properties = new Properties(Sets.newHashSet(new KeyValueValorisation("key", "prop_{{instance_key}}")), Sets.newHashSet());
        applicationsWithEvent.createOrUpdatePropertiesInPlatform(platformKey,
                "#path#1#the_module_name#the_module_version#WORKINGCOPY",
                PROPERTIES_CONVERTER.toPropertiesData(properties), 1L, comment);

        /* ACTUAL CALL */
        Set<HesperidesFile> fileSet = filesWithEvent.getLocations("the_app_name", "the_pltfm_name", "#path#1", "the_module_name",
                "the_module_version", true, "the_instance_name", false);

        assertThat(fileSet.size()).isEqualTo(3);

        /* Check only locations for now */
        List<String> fileLocations = fileSet.stream().map(HesperidesFile::getLocation).collect(Collectors.toList());

        //The location should be location from Template Object + the filename, and thoose must pass through the properties replacements
        //More tweaky replacements concerns are tested in the specific tests classes for properties
        assertThat(fileLocations.contains("loc_techno1_prop_instance/techno1_prop_instance"));
        assertThat(fileLocations.contains("loc_techno2_prop_instance/techno2_prop_instance"));
        assertThat(fileLocations.contains("loc_module_prop_instance/module_prop_instance"));

    }

    @Test
    public void shouldGetTemplateContentWithKeyValueValorisations() throws Exception {
        //We will only focus on template content. Location is handled in test above
        /* Create a module and add a template specific to the module
           This template content relies on :
            - a property named content
            - a property named property.with.dots (wich is a specific requirement not handled at first by MustacheJava
            - a property named property.with.html.escapable (to make sure content is html escaped !)

            Cases added for whitespaces ignoring
            - a property named property.with.whitespaces.at.start
            - a property named property.with.whitespaces.at.end
            - a property named property.with.whitespaces.at.everywhere
            - a property named property.with.whitespaces.and.default (witch makes use of @default on property with whitespaces)

           The property content will rely on an instance property to make sure thoose are injected
           Properties will use comment to be sure thoose are removed for generation

         */
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("the_module_name", "the_module_version");
        TemplateData templateData = TemplateData.withTemplateName("template_from_module")
                .withFilename("filename")
                .withLocation("location")
                .withContent(
                        "{{no_comment}}, {{content|comment for content}}, {{property.with.dots|some comment here}}, {{property.with.html.escapable|and comment here}}" +
                        "You know what ? {{         property.with.whitespaces.at.start|@comment \"Spaces at start\"}}, {{property.with.whitespaces.at.end   |@comment \"Spaces at end\"}}, {{  property named property.with.whitespaces.at.everywhere  | @comment \"Spaces everywhere\"}} and {{  property.with.whitespaces.and.default      | @comment \"Spaces at start and end with default value\" @default \"This is my default value!\"}}"
                )
                .withRights(null)
                .build();
        Module module = new Module(moduleKey, Sets.newHashSet(), 1L);
        modulesWithEvent.createWorkingCopy(module);
        Template createdTemplate = modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);

        /* Setup the properties, we only used PROPERTY_FILENAME and PROPERTY_LOCATION MustacheScope should be tested somewhere else*/
        Properties properties = new Properties(Sets.newHashSet(
                new KeyValueValorisation("no_comment", "OK"),
                new KeyValueValorisation("content", "the instance name is {{name}}"),
                new KeyValueValorisation("property.with.dots", "I am dotted"),
                new KeyValueValorisation("property.with.html.escapable", "I use escapable chars \"'"),
                new KeyValueValorisation("      property.with.whitespaces.at.start", "I have spaces at start"),
                new KeyValueValorisation("property.with.whitespaces.at.end      ", "I have spaces at end"),
                new KeyValueValorisation("  property named property.with.whitespaces.at.everywhere      ", "I have spaces everywhere")
        ), Sets.newHashSet());

        /*
        Create a platform "the_pltfm_name", belonging to "the_app_name"
        This platform uses the module defined above in the path "#path#1" and another one to introduce noise....
        The interesting module defines 2 instances (one for noise...), one of "the_instance_name" defines the "instance_key" as stated above
         */
        InstanceData instance1 = InstanceData.withInstanceName("the_instance_name")
                .withKeyValue(Sets.newHashSet(new KeyValueValorisationData("name", "SUPER_INSTANCE"))).build();
        InstanceData instance2 = InstanceData.withInstanceName("not_the_instance_name")
                .withKeyValue(Sets.newHashSet()).build();

        ApplicationModuleData applicationModule1 = ApplicationModuleData
                .withApplicationName("the_module_name")
                .withVersion("the_module_version")
                .withPath("path#1")
                .withId(1)
                .withInstances(Sets.newHashSet(instance1, instance2))
                .isWorkingcopy()
                .build();

        ApplicationModuleData applicationModule2 = ApplicationModuleData
                .withApplicationName("mod2_name")
                .withVersion("mod_version")
                .withPath("path#2")
                .withId(2)
                .withInstances(Sets.newHashSet(instance1, instance2))
                .isWorkingcopy()
                .build();

        Set<ApplicationModuleData> appModules = Sets.newHashSet(applicationModule1, applicationModule2);
        PlatformKey platformKey = PlatformKey.withName("the_pltfm_name")
                .withApplicationName("the_app_name")
                .build();

        PlatformData.IBuilder builder = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(appModules)
                .withVersion(1L)
                .isProduction();

        applicationsWithEvent.createPlatform(builder.build());

        /*
        Add the properties for "the_module_name"
         */
        applicationsWithEvent.createOrUpdatePropertiesInPlatform(platformKey,
                "#path#1#the_module_name#the_module_version#WORKINGCOPY",
                PROPERTIES_CONVERTER.toPropertiesData(properties), 1L, comment);


        /* ACTUAL CALL */
        String content = filesWithEvent.getFile("the_app_name", "the_pltfm_name", "#path#1", "the_module_name", "the_module_version", true,
                "the_instance_name", createdTemplate.getNamespace(), "template_from_module", model, false);

        assertThat(content).isEqualTo("OK, the instance name is SUPER_INSTANCE, I am dotted, I use escapable chars \"'You know what ? I have spaces at start, I have spaces at end, I have spaces everywhere and This is my default value!");

    }

    @Test
    public void shouldGetTemplateContentWithKeyValueValorisationsUsingGlobalModule() throws Exception {
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("the_module_name", "the_module_version");
        TemplateData templateData = TemplateData.withTemplateName("template_from_module")
                .withFilename("filename")
                .withLocation("location")
                .withContent("{{content|comment for content}}")
                .withRights(null)
                .build();
        Module module = new Module(moduleKey, Sets.newHashSet(), 1L);
        modulesWithEvent.createWorkingCopy(module);
        Template createdTemplate = modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);

        /* Setup the properties, we only used PROPERTY_FILENAME and PROPERTY_LOCATION MustacheScope should be tested somewhere else*/
        Properties properties = new Properties(Sets.newHashSet(
                new KeyValueValorisation("content", "local_property")
        ), Sets.newHashSet());

        Properties globalProperties = new Properties(Sets.newHashSet(
                new KeyValueValorisation("content", "global_property")
        ), Sets.newHashSet());

        Properties emptyGlobalProperties = Properties.empty();

        /*
        Create a platform "the_pltfm_name", belonging to "the_app_name"
        This platform uses the module defined above in the path "#path#1" and another one to introduce noise....
        The interesting module defines 2 instances (one for noise...), one of "the_instance_name" defines the "instance_key" as stated above
         */
        InstanceData instance1 = InstanceData.withInstanceName("the_instance_name")
                .withKeyValue(Sets.newHashSet(new KeyValueValorisationData("name", "SUPER_INSTANCE"))).build();
        InstanceData instance2 = InstanceData.withInstanceName("not_the_instance_name")
                .withKeyValue(Sets.newHashSet()).build();

        ApplicationModuleData applicationModule1 = ApplicationModuleData
                .withApplicationName("the_module_name")
                .withVersion("the_module_version")
                .withPath("path#1")
                .withId(1)
                .withInstances(Sets.newHashSet(instance1, instance2))
                .isWorkingcopy()
                .build();

        ApplicationModuleData applicationModule2 = ApplicationModuleData
                .withApplicationName("mod2_name")
                .withVersion("mod_version")
                .withPath("path#2")
                .withId(2)
                .withInstances(Sets.newHashSet(instance1, instance2))
                .isWorkingcopy()
                .build();

        Set<ApplicationModuleData> appModules = Sets.newHashSet(applicationModule1, applicationModule2);
        PlatformKey platformKey = PlatformKey.withName("the_pltfm_name")
                .withApplicationName("the_app_name")
                .build();

        PlatformData.IBuilder builder = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(appModules)
                .withVersion(1L)
                .isProduction();

        applicationsWithEvent.createPlatform(builder.build());

        /*
        Add the properties for "the_module_name"
         */
        applicationsWithEvent.createOrUpdatePropertiesInPlatform(platformKey,
                "#path#1#the_module_name#the_module_version#WORKINGCOPY",
                PROPERTIES_CONVERTER.toPropertiesData(properties), 1L, comment);
        applicationsWithEvent.createOrUpdatePropertiesInPlatform(platformKey, "#",
                PROPERTIES_CONVERTER.toPropertiesData(globalProperties), 2L, comment);

        /* ACTUAL CALL */
        String content = filesWithEvent.getFile("the_app_name", "the_pltfm_name", "#path#1", "the_module_name", "the_module_version", true,
                "the_instance_name", createdTemplate.getNamespace(), "template_from_module", model, false);

        assertThat(content).isEqualTo("global_property");

        /* Let's delete the global module */
        applicationsWithEvent.createOrUpdatePropertiesInPlatform(platformKey, "#",
                PROPERTIES_CONVERTER.toPropertiesData(emptyGlobalProperties), 3L, comment);

        /* ACTUAL CALL */
        String content2 = filesWithEvent.getFile("the_app_name", "the_pltfm_name", "#path#1", "the_module_name", "the_module_version", true,
                "the_instance_name", createdTemplate.getNamespace(), "template_from_module", model, false);

        assertThat(content2).isEqualTo("local_property");

    }

    @Test
    public void shouldGetTemplateContentWithIterableValorisations() throws Exception {
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("the_module_name", "the_module_version");
        TemplateData templateData = TemplateData.withTemplateName("template_from_module")
                .withFilename("filename")
                .withLocation("location")
                .withContent("{{#items}}\n" +
                        "{{name}}\n" +
                        "{{price}}\n" +
                        "{{/items}}\n" +
                        "{{#people}}\n" +
                        "{{name}}\n" +
                        "{{/people}}\n")
                .withRights(null)
                .build();
        Module module = new Module(moduleKey, Sets.newHashSet(), 1L);
        modulesWithEvent.createWorkingCopy(module);
        Template createdTemplate = modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);

        /* Setup the properties */
        IterableValorisation.IterableValorisationItem item1 = new IterableValorisation.IterableValorisationItem("car", Sets.newHashSet(new KeyValueValorisation("name", "ferrari"), new KeyValueValorisation("price", "300000")));
        IterableValorisation.IterableValorisationItem item2 = new IterableValorisation.IterableValorisationItem("motorcycle", Sets.newHashSet(new KeyValueValorisation("name", "triumph"), new KeyValueValorisation("price", "12000")));
        List<IterableValorisation.IterableValorisationItem> items = Lists.newArrayList(item1, item2);
        IterableValorisation val1 = new IterableValorisation("items", items);

        IterableValorisation.IterableValorisationItem people1 = new IterableValorisation.IterableValorisationItem("jean", Sets.newHashSet(new KeyValueValorisation("name", "jean")));
        IterableValorisation.IterableValorisationItem people2 = new IterableValorisation.IterableValorisationItem("paul", Sets.newHashSet(new KeyValueValorisation("name", "paul")));
        IterableValorisation.IterableValorisationItem people3 = new IterableValorisation.IterableValorisationItem("pierre", Sets.newHashSet(new KeyValueValorisation("name", "pierre")));
        List<IterableValorisation.IterableValorisationItem> people = Lists.newArrayList(people1, people2, people3);
        IterableValorisation val2 = new IterableValorisation("people", people);
        Properties properties = new Properties(Sets.newHashSet(), Sets.newHashSet(val1, val2));

        /* Create the platform */
        InstanceData instance1 = InstanceData.withInstanceName("the_instance_name")
                .withKeyValue(Sets.newHashSet(new KeyValueValorisationData("name", "SUPER_INSTANCE"))).build();

        ApplicationModuleData applicationModule1 = ApplicationModuleData
                .withApplicationName("the_module_name")
                .withVersion("the_module_version")
                .withPath("path#1")
                .withId(1)
                .withInstances(Sets.newHashSet(instance1))
                .isWorkingcopy()
                .build();

        Set<ApplicationModuleData> appModules = Sets.newHashSet(applicationModule1);
        PlatformKey platformKey = PlatformKey.withName("the_pltfm_name")
                .withApplicationName("the_app_name")
                .build();

        PlatformData.IBuilder builder = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(appModules)
                .withVersion(1L)
                .isProduction();

        applicationsWithEvent.createPlatform(builder.build());

        /*
        Add the properties for "the_module_name"
         */
        applicationsWithEvent.createOrUpdatePropertiesInPlatform(platformKey,
                "#path#1#the_module_name#the_module_version#WORKINGCOPY",
                PROPERTIES_CONVERTER.toPropertiesData(properties), 1L, comment);


        /* ACTUAL CALL */
        String content = filesWithEvent.getFile("the_app_name", "the_pltfm_name", "#path#1", "the_module_name", "the_module_version", true,
                "the_instance_name", createdTemplate.getNamespace(), "template_from_module", model, false);

        assertThat(content).isEqualTo("ferrari\n" +
                "300000\n" +
                "triumph\n" +
                "12000\n" +
                "jean\n" +
                "paul\n" +
                "pierre\n");

    }

    @Test
    public void should_get_file_content_with_iterable_default_values() throws Exception {
        String templateContent =
                "{{#items}}\n" +
                        "{{name}}\n" +
                        "{{price|@default \"1003030\"}}\n" +
                        "{{/items}}\n";

        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("module_name", "module_version");
        TemplateData templateData = TemplateData.withTemplateName("template_from_module")
                .withFilename("filename")
                .withLocation("location")
                .withContent(templateContent)
                .withRights(null)
                .build();
        Module module = new Module(moduleKey, Sets.newHashSet(), 1L);
        modules.createWorkingCopy(module);
        Template createdTemplate = modules.createTemplateInWorkingCopy(moduleKey, templateData);

        /* Setup the properties */
        IterableValorisation.IterableValorisationItem item1 = new IterableValorisation.IterableValorisationItem("ferrari", Sets.newHashSet(new KeyValueValorisation("name", "ferrari")));
        IterableValorisation.IterableValorisationItem item2 = new IterableValorisation.IterableValorisationItem("triumph", Sets.newHashSet(new KeyValueValorisation("name", "triumph"), new KeyValueValorisation("price", "12000")));
        List<IterableValorisation.IterableValorisationItem> items = Lists.newArrayList(item1, item2);
        IterableValorisation val1 = new IterableValorisation("items", items);

        Properties properties = new Properties(Sets.newHashSet(), Sets.newHashSet(val1));

        /* Create the platform */
        InstanceData instance1 = InstanceData.withInstanceName("instance_name")
                .withKeyValue(Sets.newHashSet(new KeyValueValorisationData("name", "SUPER_INSTANCE"))).build();

        ApplicationModuleData applicationModule1 = ApplicationModuleData
                .withApplicationName("module_name")
                .withVersion("module_version")
                .withPath("path#1")
                .withId(1)
                .withInstances(Sets.newHashSet(instance1))
                .isWorkingcopy()
                .build();

        Set<ApplicationModuleData> appModules = Sets.newHashSet(applicationModule1);
        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();

        PlatformData.IBuilder builder = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(appModules)
                .withVersion(1L)
                .isProduction();

        applications.createPlatform(builder.build());

        /*
        Add the properties for "the_module_name"
         */
        applications.createOrUpdatePropertiesInPlatform(platformKey,
                "#path#1#module_name#module_version#WORKINGCOPY",
                PROPERTIES_CONVERTER.toPropertiesData(properties), 1L, comment);

        TemplateSlurper slurper = new TemplateSlurper(templateContent);

        HesperidesPropertiesModel templateModel = slurper.generatePropertiesScope();

        /* ACTUAL CALL */
        String content = files.getFile("app_name", "pltfm_name", "#path#1", "module_name", "module_version", true, "instance_name", createdTemplate
                .getNamespace(), "template_from_module", templateModel, false);

        assertThat(content).isEqualTo(
                "ferrari\n" +
                        "1003030\n" +
                        "triumph\n" +
                        "12000\n");

    }

    @Test(expected = MissingResourceException.class)
    public void should_fail_when_trying_to_get_file_content_with_unfilled_required_fields() throws Exception {
        String templateContent =
                "{{#items}}\n" +
                        "{{name}}\n" +
                        "{{price|@required}}\n" +
                "{{/items}}\n";

        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("module_name", "module_version");
        TemplateData templateData = TemplateData.withTemplateName("template_from_module")
                .withFilename("filename")
                .withLocation("location")
                .withContent(templateContent)
                .withRights(null)
                .build();
        Module module = new Module(moduleKey, Sets.newHashSet(), 1L);
        modules.createWorkingCopy(module);
        Template createdTemplate = modules.createTemplateInWorkingCopy(moduleKey, templateData);

        /* Setup the properties */
        IterableValorisation.IterableValorisationItem item1 = new IterableValorisation.IterableValorisationItem("ferrari", Sets.newHashSet(new KeyValueValorisation("name", "ferrari")));
        List<IterableValorisation.IterableValorisationItem> items = Lists.newArrayList(item1);
        IterableValorisation val1 = new IterableValorisation("items", items);

        Properties properties = new Properties(Sets.newHashSet(), Sets.newHashSet(val1));

        /* Create the platform */
        InstanceData instance1 = InstanceData.withInstanceName("instance_name")
                .withKeyValue(Sets.newHashSet(new KeyValueValorisationData("name", "SUPER_INSTANCE"))).build();

        ApplicationModuleData applicationModule1 = ApplicationModuleData
                .withApplicationName("module_name")
                .withVersion("module_version")
                .withPath("path#1")
                .withId(1)
                .withInstances(Sets.newHashSet(instance1))
                .isWorkingcopy()
                .build();

        Set<ApplicationModuleData> appModules = Sets.newHashSet(applicationModule1);
        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();

        PlatformData.IBuilder builder = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(appModules)
                .withVersion(1L)
                .isProduction();

        applications.createPlatform(builder.build());

        /*
        Add the properties for "the_module_name"
         */
        applications.createOrUpdatePropertiesInPlatform(platformKey,
                "#path#1#module_name#module_version#WORKINGCOPY",
                PROPERTIES_CONVERTER.toPropertiesData(properties), 1L, comment);

        TemplateSlurper slurper = new TemplateSlurper(templateContent);

        HesperidesPropertiesModel templateModel = slurper.generatePropertiesScope();

        /* ACTUAL CALL */
        files.getFile("app_name", "pltfm_name", "#path#1", "module_name", "module_version", true, "instance_name", createdTemplate.getNamespace(),
                "template_from_module", templateModel, false);

        // assertion is done on the @Test() annotation !
    }

    @Test
    public void should_generate_file_with_password_fields() throws Exception {
        String templateContent =
                "{{user.password | @password}}" +
                "{{#users}}" +
                        "{{ login }}" +
                        "{{ password | @password}}" +
                "{{/users}}";

        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("module_name", "module_version");
        TemplateData templateData = TemplateData.withTemplateName("template_from_module")
                .withFilename("filename")
                .withLocation("location")
                .withContent(templateContent)
                .withRights(null)
                .build();
        Module module = new Module(moduleKey, Sets.newHashSet(), 1L);
        modules.createWorkingCopy(module);
        Template createdTemplate = modules.createTemplateInWorkingCopy(moduleKey, templateData);

        /* Setup the properties */
        IterableValorisation.IterableValorisationItem user1 = new IterableValorisation.IterableValorisationItem("tidiane", Sets.newHashSet(new KeyValueValorisation("login", "tidiane"), new KeyValueValorisation("password", "superp@ass!")));
        IterableValorisation.IterableValorisationItem user2 = new IterableValorisation.IterableValorisationItem("jean", Sets.newHashSet(new KeyValueValorisation("login", "jean"), new KeyValueValorisation("password", "superp@ass#")));
        List<IterableValorisation.IterableValorisationItem> users = Lists.newArrayList(user1, user2);
        IterableValorisation val1 = new IterableValorisation("users", users);

        Properties properties = new Properties(Sets.newHashSet(new KeyValueValorisation("user.password", "thePaSS")), Sets.newHashSet(val1));

        /* Create the platform */
        InstanceData instance1 = InstanceData.withInstanceName("instance_name")
                .withKeyValue(Sets.newHashSet(new KeyValueValorisationData("name", "SUPER_INSTANCE"))).build();

        ApplicationModuleData applicationModule1 = ApplicationModuleData
                .withApplicationName("module_name")
                .withVersion("module_version")
                .withPath("path#1")
                .withId(1)
                .withInstances(Sets.newHashSet(instance1))
                .isWorkingcopy()
                .build();

        Set<ApplicationModuleData> appModules = Sets.newHashSet(applicationModule1);
        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();

        PlatformData.IBuilder builder = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(appModules)
                .withVersion(1L)
                .isProduction();

        applications.createPlatform(builder.build());

        /*
        Add the properties for "the_module_name"
         */
        applications.createOrUpdatePropertiesInPlatform(platformKey,
                "#path#1#module_name#module_version#WORKINGCOPY",
                PROPERTIES_CONVERTER.toPropertiesData(properties), 1L, comment);

        TemplateSlurper slurper = new TemplateSlurper(templateContent);

        HesperidesPropertiesModel templateModel = slurper.generatePropertiesScope();

        /* ACTUAL CALL */
        String generatedConent = files.getFile("app_name", "pltfm_name", "#path#1", "module_name", "module_version", true, "instance_name", createdTemplate.getNamespace(),
                "template_from_module", templateModel, false);

        final String expected = "thePaSStidianesuperp@ass!jeansuperp@ass#";
        assertThat(generatedConent).isEqualTo(expected);

        /* HIDDING CALL */
        String generatedhHiddenConent = hiddingFiles.getFile("app_name", "pltfm_name", "#path#1", "module_name", "module_version", true, "instance_name", createdTemplate.getNamespace(),
                "template_from_module", templateModel, false);

        final String expectedHidden = "********tidiane********jean********";
        assertThat(generatedhHiddenConent).isEqualTo(expectedHidden);
    }

    @Test
    public void should_generate_file_with_password_and_default_fields_for_nested_iterable_properties() throws Exception {
        String templateContent =
                "{{user.password | @password}}" +
                    "{{#users}}" +
                    "{{ login }}" +
                    "{{ password | @password }}" +
                    "{{#account}}" +
                        "{{ account.login | @default \"simple\" }}" +
                        "{{ account.password | @password }}" +
                    "{{/account}}" +
                "{{/users}}";

        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("module_name", "module_version");
        TemplateData templateData = TemplateData.withTemplateName("template_from_module")
                .withFilename("filename")
                .withLocation("location")
                .withContent(templateContent)
                .withRights(null)
                .build();
        Module module = new Module(moduleKey, Sets.newHashSet(), 1L);
        modules.createWorkingCopy(module);
        Template createdTemplate = modules.createTemplateInWorkingCopy(moduleKey, templateData);

        /* Setup the properties */
        IterableValorisation.IterableValorisationItem account1 = new IterableValorisation.IterableValorisationItem("account1", Sets.newHashSet(new KeyValueValorisation("account.login", "login1"), new KeyValueValorisation("account.password", "the@ccountp@ass;)")));
        List<IterableValorisation.IterableValorisationItem> accounts = Lists.newArrayList(account1);
        IterableValorisation accounts1 = new IterableValorisation("account", accounts);
        IterableValorisation.IterableValorisationItem user1 = new IterableValorisation.IterableValorisationItem("tidiane", Sets.newHashSet(new KeyValueValorisation("login", "tidiane"), new KeyValueValorisation("password", "superp@ass!"), accounts1));

        IterableValorisation.IterableValorisationItem account2 = new IterableValorisation.IterableValorisationItem("account2", Sets.newHashSet(new KeyValueValorisation("account.password", "the@ccountp@ass2!;)")));
        List<IterableValorisation.IterableValorisationItem> _accounts = Lists.newArrayList(account2);
        IterableValorisation accounts2 = new IterableValorisation("account", _accounts);
        IterableValorisation.IterableValorisationItem user2 = new IterableValorisation.IterableValorisationItem("jean", Sets.newHashSet(new KeyValueValorisation("login", "jean"), new KeyValueValorisation("password", "superp@ass#"), accounts2));

        List<IterableValorisation.IterableValorisationItem> users = Lists.newArrayList(user1, user2);
        IterableValorisation val1 = new IterableValorisation("users", users);

        Properties properties = new Properties(Sets.newHashSet(new KeyValueValorisation("user.password", "thePaSS")), Sets.newHashSet(val1));

        /* Create the platform */
        InstanceData instance1 = InstanceData.withInstanceName("instance_name")
                .withKeyValue(Sets.newHashSet(new KeyValueValorisationData("name", "SUPER_INSTANCE"))).build();

        ApplicationModuleData applicationModule1 = ApplicationModuleData
                .withApplicationName("module_name")
                .withVersion("module_version")
                .withPath("path#1")
                .withId(1)
                .withInstances(Sets.newHashSet(instance1))
                .isWorkingcopy()
                .build();

        Set<ApplicationModuleData> appModules = Sets.newHashSet(applicationModule1);
        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();

        PlatformData.IBuilder builder = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(appModules)
                .withVersion(1L)
                .isProduction();

        applications.createPlatform(builder.build());

        /*
        Add the properties for "the_module_name"
         */
        applications.createOrUpdatePropertiesInPlatform(platformKey,
                "#path#1#module_name#module_version#WORKINGCOPY",
                PROPERTIES_CONVERTER.toPropertiesData(properties), 1L, comment);

        TemplateSlurper slurper = new TemplateSlurper(templateContent);

        HesperidesPropertiesModel templateModel = slurper.generatePropertiesScope();

        String generatedConent = files.getFile("app_name", "pltfm_name", "#path#1", "module_name", "module_version", true, "instance_name", createdTemplate.getNamespace(),
                "template_from_module", templateModel, false);

        final String expected = "thePaSStidianesuperp@ass!login1the@ccountp@ass;)jeansuperp@ass#simplethe@ccountp@ass2!;)";
        assertThat(generatedConent).isEqualTo(expected);

        // Hidding call
        String generatedhHiddenConent = hiddingFiles.getFile("app_name", "pltfm_name", "#path#1", "module_name", "module_version", true, "instance_name", createdTemplate.getNamespace(),
                "template_from_module", templateModel, false);

        final String expectedHidden = "********tidiane********login1********jean********simple********";
        assertThat(generatedhHiddenConent).isEqualTo(expectedHidden);
    }

    @Test(expected = MissingResourceException.class)
    public void should_fail_when_trying_to_get_file_content_with_not_matching_pattern_field_value() throws Exception {
        String templateContent =
                "{{#items}}\n" +
                        "{{name| @pattern \"[A-Za-z]+\"}}\n" +
                        "{{price}}\n" +
                "{{/items}}\n";

        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("module_name", "module_version");
        TemplateData templateData = TemplateData.withTemplateName("template_from_module")
                .withFilename("filename")
                .withLocation("location")
                .withContent(templateContent)
                .withRights(null)
                .build();
        Module module = new Module(moduleKey, Sets.newHashSet(), 1L);
        modules.createWorkingCopy(module);
        Template createdTemplate = modules.createTemplateInWorkingCopy(moduleKey, templateData);

        /* Setup the properties */
        IterableValorisation.IterableValorisationItem item1 = new IterableValorisation.IterableValorisationItem("ferrari", Sets.newHashSet(new KeyValueValorisation("name", "ferrari1234")));
        List<IterableValorisation.IterableValorisationItem> items = Lists.newArrayList(item1);
        IterableValorisation val1 = new IterableValorisation("items", items);

        Properties properties = new Properties(Sets.newHashSet(), Sets.newHashSet(val1));

        /* Create the platform */
        InstanceData instance1 = InstanceData.withInstanceName("instance_name")
                .withKeyValue(Sets.newHashSet(new KeyValueValorisationData("name", "SUPER_INSTANCE"))).build();

        ApplicationModuleData applicationModule1 = ApplicationModuleData
                .withApplicationName("module_name")
                .withVersion("module_version")
                .withPath("path#1")
                .withId(1)
                .withInstances(Sets.newHashSet(instance1))
                .isWorkingcopy()
                .build();

        Set<ApplicationModuleData> appModules = Sets.newHashSet(applicationModule1);
        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();

        PlatformData.IBuilder builder = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(appModules)
                .withVersion(1L)
                .isProduction();

        applications.createPlatform(builder.build());

        /*
        Add the properties for "the_module_name"
         */
        applications.createOrUpdatePropertiesInPlatform(platformKey,
                "#path#1#module_name#module_version#WORKINGCOPY",
                PROPERTIES_CONVERTER.toPropertiesData(properties), 1L, comment);

        TemplateSlurper slurper = new TemplateSlurper(templateContent);

        HesperidesPropertiesModel templateModel = slurper.generatePropertiesScope();

        /* ACTUAL CALL */
        files.getFile("app_name", "pltfm_name", "#path#1", "module_name", "module_version", true, "instance_name", createdTemplate.getNamespace(),
                "template_from_module", templateModel, false);

        // assertion is done on the @Test() annotation !
    }

    @Test(expected = MissingResourceException.class)
    public void should_fail_when_trying_to_get_file_content_with_not_matching_pattern_field_value_in_inner_iterable_properties() throws Exception {
        String templateContent =
                "{{#items}}\n" +
                        "{{name}}\n" +
                        "{{price}}\n" +
                        "{{#owners}}" +
                        "{{name | @pattern \"[A-Za-z]+\" }}" +
                        "{{/owners}}" +
                        "{{/items}}\n";

        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("module_name", "module_version");
        TemplateData templateData = TemplateData.withTemplateName("template_from_module")
                .withFilename("filename")
                .withLocation("location")
                .withContent(templateContent)
                .withRights(null)
                .build();
        Module module = new Module(moduleKey, Sets.newHashSet(), 1L);
        modules.createWorkingCopy(module);
        Template createdTemplate = modules.createTemplateInWorkingCopy(moduleKey, templateData);

        /* Setup the properties */
        IterableValorisation.IterableValorisationItem badOwner = new IterableValorisation.IterableValorisationItem("owner", Sets.newHashSet(new KeyValueValorisation("name", "223Pauline")));
        IterableValorisation.IterableValorisationItem goodOwner = new IterableValorisation.IterableValorisationItem("owner", Sets.newHashSet(new KeyValueValorisation("name", "Pauline")));
        List<IterableValorisation.IterableValorisationItem> owners = Lists.newArrayList(badOwner, goodOwner);
        IterableValorisation _owners = new IterableValorisation("owners", owners);

        IterableValorisation.IterableValorisationItem item1 = new IterableValorisation.IterableValorisationItem("ferrari", Sets.newHashSet(new KeyValueValorisation("name", "ferrari"), new KeyValueValorisation("price", "1234567890987654321"), _owners));
        List<IterableValorisation.IterableValorisationItem> items = Lists.newArrayList(item1);
        IterableValorisation val1 = new IterableValorisation("items", items);

        Properties properties = new Properties(Sets.newHashSet(), Sets.newHashSet(val1));

        /* Create the platform */
        InstanceData instance1 = InstanceData.withInstanceName("instance_name")
                .withKeyValue(Sets.newHashSet(new KeyValueValorisationData("name", "SUPER_INSTANCE"))).build();

        ApplicationModuleData applicationModule1 = ApplicationModuleData
                .withApplicationName("module_name")
                .withVersion("module_version")
                .withPath("path#1")
                .withId(1)
                .withInstances(Sets.newHashSet(instance1))
                .isWorkingcopy()
                .build();

        Set<ApplicationModuleData> appModules = Sets.newHashSet(applicationModule1);
        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();

        PlatformData.IBuilder builder = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(appModules)
                .withVersion(1L)
                .isProduction();

        applications.createPlatform(builder.build());

        /*
        Add the properties for "the_module_name"
         */
        applications.createOrUpdatePropertiesInPlatform(platformKey,
                "#path#1#module_name#module_version#WORKINGCOPY",
                PROPERTIES_CONVERTER.toPropertiesData(properties), 1L, comment);

        TemplateSlurper slurper = new TemplateSlurper(templateContent);

        HesperidesPropertiesModel templateModel = slurper.generatePropertiesScope();

        /* ACTUAL CALL */
        files.getFile("app_name", "pltfm_name", "#path#1", "module_name", "module_version", true, "instance_name", createdTemplate.getNamespace(),
                "template_from_module", templateModel, false);

        // assertion is done on the @Test() annotation !
    }

    @Test(expected = MissingResourceException.class)
    public void should_fail_when_trying_to_get_file_content_with_not_matching_unfilled_required_value_in_inner_iterable_properties() throws Exception {
        String templateContent =
                "{{#items}}\n" +
                    "{{name}}\n" +
                    "{{price}}\n" +
                    "{{#owners}}" +
                        "{{name | @required}}" +
                    "{{/owners}}" +
                "{{/items}}\n";

        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("module_name", "module_version");
        TemplateData templateData = TemplateData.withTemplateName("template_from_module")
                .withFilename("filename")
                .withLocation("location")
                .withContent(templateContent)
                .withRights(null)
                .build();
        Module module = new Module(moduleKey, Sets.newHashSet(), 1L);
        modules.createWorkingCopy(module);
        Template createdTemplate = modules.createTemplateInWorkingCopy(moduleKey, templateData);

        /* Setup the properties */
        IterableValorisation.IterableValorisationItem badOwner = new IterableValorisation.IterableValorisationItem("owner", Sets.newHashSet());
        List<IterableValorisation.IterableValorisationItem> owners = Lists.newArrayList(badOwner);
        IterableValorisation _owners = new IterableValorisation("owners", owners);

        IterableValorisation.IterableValorisationItem item1 = new IterableValorisation.IterableValorisationItem("ferrari", Sets.newHashSet(new KeyValueValorisation("name", "ferrari"), new KeyValueValorisation("price", "1234567890987654321"), _owners));
        List<IterableValorisation.IterableValorisationItem> items = Lists.newArrayList(item1);
        IterableValorisation val1 = new IterableValorisation("items", items);

        Properties properties = new Properties(Sets.newHashSet(), Sets.newHashSet(val1));

        /* Create the platform */
        InstanceData instance1 = InstanceData.withInstanceName("instance_name")
                .withKeyValue(Sets.newHashSet(new KeyValueValorisationData("name", "SUPER_INSTANCE"))).build();

        ApplicationModuleData applicationModule1 = ApplicationModuleData
                .withApplicationName("module_name")
                .withVersion("module_version")
                .withPath("path#1")
                .withId(1)
                .withInstances(Sets.newHashSet(instance1))
                .isWorkingcopy()
                .build();

        Set<ApplicationModuleData> appModules = Sets.newHashSet(applicationModule1);
        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();

        PlatformData.IBuilder builder = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(appModules)
                .withVersion(1L)
                .isProduction();

        applications.createPlatform(builder.build());

        /*
        Add the properties for "the_module_name"
         */
        applications.createOrUpdatePropertiesInPlatform(platformKey,
                "#path#1#module_name#module_version#WORKINGCOPY",
                PROPERTIES_CONVERTER.toPropertiesData(properties), 1L, comment);

        TemplateSlurper slurper = new TemplateSlurper(templateContent);

        HesperidesPropertiesModel templateModel = slurper.generatePropertiesScope();

        /* ACTUAL CALL */
        files.getFile("app_name", "pltfm_name", "#path#1", "module_name", "module_version", true, "instance_name", createdTemplate.getNamespace(),
                "template_from_module", templateModel, false);

        // assertion is done on the @Test() annotation !
    }

    @Test
    public void file_generation_should_include_platform_information_in_the_mustache_scope(){
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("the_module_name", "the_module_version");
        TemplateData templateData = TemplateData.withTemplateName("template_from_module")
                .withFilename("{{hesperides.application.name}}_file")
                .withLocation("{{hesperides.application.name}}_{{hesperides.platform.name}}_{{hesperides.application.version}}")
                .withContent("{{hesperides.application.name}} {{hesperides.platform.name}} {{hesperides.application.version}}")
                .withRights(null)
                .build();
        Module module = new Module(moduleKey, Sets.newHashSet(), 1L);
        modulesWithEvent.createWorkingCopy(module);
        Template createdTemplate = modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);

        /* Setup the properties, we use empty valorisations, since platform valorisations should be automatically provided*/
        Properties properties = new Properties(Sets.newHashSet(), Sets.newHashSet());

        /*
        Create a platform "the_pltfm_name", belonging to "the_app_name"
        This platform uses the module defined above in the path "#path#1"
         */
        InstanceData instance1 = InstanceData.withInstanceName("the_instance_name")
                .withKeyValue(Sets.newHashSet()).build();

        ApplicationModuleData applicationModule1 = ApplicationModuleData
                .withApplicationName("the_module_name")
                .withVersion("the_module_version")
                .withPath("path#1")
                .withId(1)
                .withInstances(Sets.newHashSet(instance1))
                .isWorkingcopy()
                .build();

        Set<ApplicationModuleData> appModules = Sets.newHashSet(applicationModule1);
        PlatformKey platformKey = PlatformKey.withName("the_pltfm_name")
                .withApplicationName("the_app_name")
                .build();

        PlatformData.IBuilder builder = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("version")
                .withModules(appModules)
                .withVersion(1L)
                .isProduction();

        applicationsWithEvent.createPlatform(builder.build());

        /*
        Add the properties for "the_module_name"
         */
        applicationsWithEvent.createOrUpdatePropertiesInPlatform(platformKey,
                "#path#1#the_module_name#the_module_version#WORKINGCOPY",
                PROPERTIES_CONVERTER.toPropertiesData(properties), 1L, comment);


        /* ACTUAL CALL */
        Set<HesperidesFile> filesSet = filesWithEvent.getLocations("the_app_name", "the_pltfm_name", "#path#1", "the_module_name",
                "the_module_version", true, "the_instance_name", false);
        HesperidesFile fileInfo = filesSet.iterator().next();
        assertThat(fileInfo.getFilename()).isEqualTo("the_app_name_file");
        assertThat(fileInfo.getLocation()).isEqualTo("the_app_name_the_pltfm_name_version");

        String content = filesWithEvent.getFile("the_app_name", "the_pltfm_name", "#path#1", "the_module_name", "the_module_version", true,
                "the_instance_name", createdTemplate.getNamespace(), "template_from_module", model, false);

        assertThat(content).isEqualTo("the_app_name the_pltfm_name version");
    }

    @Test
    public void file_generation_should_include_module_information_in_the_mustache_scope(){
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("the_module_name", "the_module_version");
        TemplateData templateData = TemplateData.withTemplateName("template_from_module")
                .withFilename("{{hesperides.module.name}}_file")
                .withLocation("{{hesperides.module.name}}_{{hesperides.module.version}}")
                .withContent("{{hesperides.module.name}} {{hesperides.module.version}} {{hesperides.module.path.0}} {{hesperides.module.path.1}}")
                .withRights(null)
                .build();
        Module module = new Module(moduleKey, Sets.newHashSet(), 1L);
        modulesWithEvent.createWorkingCopy(module);
        Template createdTemplate = modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);

        /* Setup the properties, we use empty valorisations, since platform valorisations should be automatically provided*/
        Properties properties = new Properties(Sets.newHashSet(), Sets.newHashSet());

        /*
        Create a platform "the_pltfm_name", belonging to "the_app_name"
         */
        InstanceData instance1 = InstanceData.withInstanceName("the_instance_name")
                .withKeyValue(Sets.newHashSet()).build();

        ApplicationModuleData applicationModule1 = ApplicationModuleData
                .withApplicationName("the_module_name")
                .withVersion("the_module_version")
                .withPath("#COMPONENT#TECHNO")
                .withId(1)
                .withInstances(Sets.newHashSet(instance1))
                .isWorkingcopy()
                .build();

        Set<ApplicationModuleData> appModules = Sets.newHashSet(applicationModule1);
        PlatformKey platformKey = PlatformKey.withName("the_pltfm_name")
                .withApplicationName("the_app_name")
                .build();

        PlatformData.IBuilder builder = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(appModules)
                .withVersion(1L)
                .isProduction();

        applicationsWithEvent.createPlatform(builder.build());

        /*
        Add the properties for "the_module_name"
         */
        applicationsWithEvent.createOrUpdatePropertiesInPlatform(platformKey,
                "#COMPONENT#TECHNO#the_module_name#the_module_version#WORKINGCOPY",
                PROPERTIES_CONVERTER.toPropertiesData(properties), 1L, comment);


        /* ACTUAL CALL */
        Set<HesperidesFile> filesSet = filesWithEvent.getLocations("the_app_name", "the_pltfm_name", "#COMPONENT#TECHNO", "the_module_name",
                "the_module_version", true, "the_instance_name", false);
        HesperidesFile fileInfo = filesSet.iterator().next();
        assertThat(fileInfo.getFilename()).isEqualTo("the_module_name_file");
        assertThat(fileInfo.getLocation()).isEqualTo("the_module_name_the_module_version");

        String content = filesWithEvent.getFile("the_app_name", "the_pltfm_name", "#COMPONENT#TECHNO", "the_module_name", "the_module_version",
                true, "the_instance_name", createdTemplate.getNamespace(), "template_from_module", model, false);

        assertThat(content).isEqualTo("the_module_name the_module_version COMPONENT TECHNO");
    }

    @Test
    public void file_generation_should_include_instance_information_in_the_mustache_scope(){
        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("the_module_name", "the_module_version");
        TemplateData templateData = TemplateData.withTemplateName("template_from_module")
                .withFilename("{{hesperides.instance.name}}_file")
                .withLocation("{{hesperides.instance.name}}_location")
                .withContent("{{hesperides.instance.name}}")
                .withRights(null)
                .build();
        Module module = new Module(moduleKey, Sets.newHashSet(), 1L);
        modulesWithEvent.createWorkingCopy(module);
        Template createdTemplate = modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);

        /* Setup the properties, we use empty valorisations, since platform valorisations should be automatically provided*/
        Properties properties = new Properties(Sets.newHashSet(), Sets.newHashSet());

        /*
        Create a platform "the_pltfm_name", belonging to "the_app_name"
         */
        InstanceData instance1 = InstanceData.withInstanceName("the_instance_name")
                .withKeyValue(Sets.newHashSet()).build();

        ApplicationModuleData applicationModule1 = ApplicationModuleData
                .withApplicationName("the_module_name")
                .withVersion("the_module_version")
                .withPath("#COMPONENT#TECHNO")
                .withId(1)
                .withInstances(Sets.newHashSet(instance1))
                .isWorkingcopy()
                .build();

        Set<ApplicationModuleData> appModules = Sets.newHashSet(applicationModule1);
        PlatformKey platformKey = PlatformKey.withName("the_pltfm_name")
                .withApplicationName("the_app_name")
                .build();

        PlatformData.IBuilder builder = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(appModules)
                .withVersion(1L)
                .isProduction();

        applicationsWithEvent.createPlatform(builder.build());

        /*
        Add the properties for "the_module_name"
         */
        applicationsWithEvent.createOrUpdatePropertiesInPlatform(platformKey,
                "#COMPONENT#TECHNO#the_module_name#the_module_version#WORKINGCOPY",
                PROPERTIES_CONVERTER.toPropertiesData(properties), 1L, comment);


        /* ACTUAL CALL */
        Set<HesperidesFile> filesSet = filesWithEvent.getLocations("the_app_name", "the_pltfm_name", "#COMPONENT#TECHNO", "the_module_name",
                "the_module_version", true, "the_instance_name", false);
        HesperidesFile fileInfo = filesSet.iterator().next();
        assertThat(fileInfo.getFilename()).isEqualTo("the_instance_name_file");
        assertThat(fileInfo.getLocation()).isEqualTo("the_instance_name_location");

        String content = filesWithEvent.getFile("the_app_name", "the_pltfm_name", "#COMPONENT#TECHNO", "the_module_name", "the_module_version",
                true, "the_instance_name", createdTemplate.getNamespace(), "template_from_module", model, false);

        assertThat(content).isEqualTo("the_instance_name");
    }

    @Test
    public void should_get_file_content_with_iterable_with_first_empty_values() throws Exception {
        String templateContent =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                "<profiles>\r\n" +
                "{{#profiles}}\r\n" +
                "  <profile>\r\n"+
                "    <user>{{user}}</user>\r\n" +
                "    <password>{{password}}</password>\r\n" +
                "    <token>{{toto.token}}</token>\r\n" +
                "  </profile>\r\n" +
                "{{/profiles}}\r\n" +
                "</profiles>";

        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("module_name", "module_version");
        TemplateData templateData = TemplateData.withTemplateName("template_from_module")
                .withFilename("filename")
                .withLocation("location")
                .withContent(templateContent)
                .withRights(null)
                .build();
        Module module = new Module(moduleKey, Sets.newHashSet(), 1L);
        modules.createWorkingCopy(module);
        Template createdTemplate = modules.createTemplateInWorkingCopy(moduleKey, templateData);

        /* Setup the properties */
        IterableValorisation.IterableValorisationItem item1 = new IterableValorisation.IterableValorisationItem("ferrari", Sets.newHashSet(
                new KeyValueValorisation("user", "marcel_in_short"),
                new KeyValueValorisation("toto.token", "144dfDGGRIs5689ds64")
        ));

        IterableValorisation.IterableValorisationItem item2 = new IterableValorisation.IterableValorisationItem("ferrari", Sets.newHashSet(
                new KeyValueValorisation("user", "maurice_in_long"),
                new KeyValueValorisation("password", "AureliaJsForEver"),
                new KeyValueValorisation("toto.token", "144dfDGGRIs5689ds64")
        ));

        List<IterableValorisation.IterableValorisationItem> items = Lists.newArrayList(item1, item2);
        IterableValorisation val1 = new IterableValorisation("profiles", items);

        Properties properties = new Properties(Sets.newHashSet(), Sets.newHashSet(val1));

        /* Create the platform */
        InstanceData instance1 = InstanceData.withInstanceName("instance_name")
                .withKeyValue(Sets.newHashSet(new KeyValueValorisationData("name", "SUPER_INSTANCE"))).build();

        ApplicationModuleData applicationModule1 = ApplicationModuleData
                .withApplicationName("module_name")
                .withVersion("module_version")
                .withPath("path#1")
                .withId(1)
                .withInstances(Sets.newHashSet(instance1))
                .isWorkingcopy()
                .build();

        Set<ApplicationModuleData> appModules = Sets.newHashSet(applicationModule1);
        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();

        PlatformData.IBuilder builder = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(appModules)
                .withVersion(1L)
                .isProduction();

        applications.createPlatform(builder.build());

        /*
        Add the properties for "the_module_name"
         */
        applications.createOrUpdatePropertiesInPlatform(platformKey,
                "#path#1#module_name#module_version#WORKINGCOPY",
                PROPERTIES_CONVERTER.toPropertiesData(properties), 1L, comment);

        TemplateSlurper slurper = new TemplateSlurper(templateContent);

        HesperidesPropertiesModel templateModel = slurper.generatePropertiesScope();

        /* ACTUAL CALL */
        String content = files.getFile("app_name", "pltfm_name", "#path#1", "module_name", "module_version", true, "instance_name", createdTemplate.getNamespace(), "template_from_module", templateModel, false);

        assertThat(content).isEqualTo(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                "<profiles>\r\n" +
                "  <profile>\r\n" +
                "    <user>marcel_in_short</user>\r\n" +
                "    <password></password>\r\n" +
                "    <token>144dfDGGRIs5689ds64</token>\r\n" +
                "  </profile>\r\n" +
                "  <profile>\r\n" +
                "    <user>maurice_in_long</user>\r\n" +
                "    <password>AureliaJsForEver</password>\r\n" +
                "    <token>144dfDGGRIs5689ds64</token>\r\n" +
                "  </profile>\r\n" +
                "</profiles>");
    }

    @Test
    public void should_get_file_content_with_iterable_with_same_variable_name() throws Exception {
        String templateContent =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "{{user}}\n" +
                "<profiles>\n" +
                "{{#profiles}}\n" +
                "  <profile>\n"+
                "    <user>{{user}}</user>\n" +
                "    <password>{{password}}</password>\n" +
                "    <token>{{toto.token}}</token>\n" +
                "  </profile>\n" +
                "{{/profiles}}\n" +
                "</profiles>";

        ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("module_name", "module_version");
        TemplateData templateData = TemplateData.withTemplateName("template_from_module")
                .withFilename("filename")
                .withLocation("location")
                .withContent(templateContent)
                .withRights(null)
                .build();
        Module module = new Module(moduleKey, Sets.newHashSet(), 1L);
        modules.createWorkingCopy(module);
        Template createdTemplate = modules.createTemplateInWorkingCopy(moduleKey, templateData);

        /* Setup the properties */
        IterableValorisation.IterableValorisationItem item1 = new IterableValorisation.IterableValorisationItem("titi", Sets.newHashSet(
                new KeyValueValorisation("user", "marcel_in_short"),
                new KeyValueValorisation("toto.token", "144dfDGGRIs5689ds64")
        ));

        IterableValorisation.IterableValorisationItem item2 = new IterableValorisation.IterableValorisationItem("rominet", Sets.newHashSet(
                new KeyValueValorisation("user", "maurice_in_long"),
                new KeyValueValorisation("password", "AureliaJsForEver"),
                new KeyValueValorisation("toto.token", "144dfDGGRIs5689ds64")
        ));

        List<IterableValorisation.IterableValorisationItem> items = Lists.newArrayList(item1, item2);
        IterableValorisation val1 = new IterableValorisation("profiles", items);

        Properties properties = new Properties(Sets.newHashSet(new KeyValueValorisation("user", "bad_user")), Sets.newHashSet(val1));

        /* Create the platform */
        InstanceData instance1 = InstanceData.withInstanceName("instance_name")
                .withKeyValue(Sets.newHashSet(new KeyValueValorisationData("name", "SUPER_INSTANCE"))).build();

        ApplicationModuleData applicationModule1 = ApplicationModuleData
                .withApplicationName("module_name")
                .withVersion("module_version")
                .withPath("path#1")
                .withId(1)
                .withInstances(Sets.newHashSet(instance1))
                .isWorkingcopy()
                .build();

        Set<ApplicationModuleData> appModules = Sets.newHashSet(applicationModule1);
        PlatformKey platformKey = PlatformKey.withName("pltfm_name")
                .withApplicationName("app_name")
                .build();

        PlatformData.IBuilder builder = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("1.0.0.0")
                .withModules(appModules)
                .withVersion(1L)
                .isProduction();

        applications.createPlatform(builder.build());

        /*
        Add the properties for "the_module_name"
         */
        applications.createOrUpdatePropertiesInPlatform(platformKey,
                "#path#1#module_name#module_version#WORKINGCOPY",
                PROPERTIES_CONVERTER.toPropertiesData(properties), 1L, comment);

        TemplateSlurper slurper = new TemplateSlurper(templateContent);

        HesperidesPropertiesModel templateModel = slurper.generatePropertiesScope();

        /* ACTUAL CALL */
        String content = files.getFile("app_name", "pltfm_name", "#path#1", "module_name", "module_version", true, "instance_name", createdTemplate.getNamespace(), "template_from_module", templateModel, false);

        assertThat(content).isEqualTo(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "bad_user\n" +
                "<profiles>\n" +
                "  <profile>\n" +
                "    <user>marcel_in_short</user>\n" +
                "    <password></password>\n" +
                "    <token>144dfDGGRIs5689ds64</token>\n" +
                "  </profile>\n" +
                "  <profile>\n" +
                "    <user>maurice_in_long</user>\n" +
                "    <password>AureliaJsForEver</password>\n" +
                "    <token>144dfDGGRIs5689ds64</token>\n" +
                "  </profile>\n" +
                "</profiles>");
    }
}

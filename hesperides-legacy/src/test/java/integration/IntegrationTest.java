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
package integration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import integration.client.ModuleClient;
import integration.client.PlatformClient;
import redis.clients.jedis.Jedis;
import tests.type.IntegrationTests;

import com.vsct.dt.hesperides.resources.ApplicationModule;
import com.vsct.dt.hesperides.resources.KeyValueValorisation;
import com.vsct.dt.hesperides.resources.Properties;
import com.vsct.dt.hesperides.resources.TemplateListItem;
import com.vsct.dt.hesperides.templating.modules.ModuleKey;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.modules.template.TemplateFileRights;
import com.vsct.dt.hesperides.templating.modules.template.TemplateRights;
import com.vsct.dt.hesperides.util.HesperidesVersion;

/**
 * Created by emeric_martineau on 10/03/2017.
 */
@Category(IntegrationTests.class)
public class IntegrationTest extends AbstractIntegrationTest {
    /**
     * Nb event to generate.
     */
    private static final int NB_EVENTS = 372;

    /**
     * Name of template in template package or module?
     */
    private static final String TEMPLATE_NAME = "title";

    /**
     * Tempalte package version.
     */
    private static final String TEMPLATE_PACKAGE_VERSION = "1.0.0";

    /**
     * Version of module.
     */
    private static final String MODULE_VERSION = "1.0.0";

    /**
     * Content of template of module.
     */
    private static final String MODULE_TEMPLATE_BASE_CONTENT = "content";

    /**
     * Name of platform.
     */
    private static final String PLATFORM_NAME = "PTF1";

    /**
     * Prefix of propoerties
     */
    private static final String PROPERTIES_PREFIX_PATH = "#TEST#INT";

    /**
     * Template package name.
     *
     * @return name
     */
    private String getTemplatePackageName() {
        return this.prefixName + "_template_name";
    }

    /**
     * Template package properties path.
     *
     * @return  properties path
     */
    private String getTemplatePackagePath() {
        return "packages#" + getTemplatePackageName() + "#" + TEMPLATE_PACKAGE_VERSION + "#WORKINGCOPY";
    }

    /**
     * Template package redis key (for data).
     *
     * @return redis key
     */
    private String getTemplatePackageRedisKey() {
        return "template_package-" + getTemplatePackageName() + "-" + TEMPLATE_PACKAGE_VERSION + "-wc";
    }

    /**
     * Template package redis key (for cache).
     *
     * @return redis key
     */
    private String getTemplatePackageRedisCacheKey() {
        return "snapshotevents-" + getTemplatePackageRedisKey();
    }

    /**
     * Name of module.
     *
     * @return name
     */
    private String getModuleName() {
        return this.prefixName + "-module_name";
    }

    /**
     * Module properties path.
     *
     * @return properties path
     */
    private String getModulePath() {
        return "modules#" + getModuleName() + "#" + MODULE_VERSION + "#WORKINGCOPY";
    }

    /**
     * Module redis key (for data).
     *
     * @return redis key
     */
    private String getModuleRedisKey() {
        return "module-" + getModuleName() + "-" + MODULE_VERSION + "-wc";
    }

    /**
     * Module redis key (for data).
     *
     * @return redis key
     */
    private String getModuleRedisCacheKey() {
        return "snapshotevents-" + getModuleRedisKey();
    }

    /**
     * Name of application.
     *
     * @return name
     */
    private String getApplicationName() {
        return this.prefixName + "-TEST_AUTO_INT";
    }

    /**
     * Platform redis key (for data).
     *
     * @return key
     */
    private String getPlatformRedisKey() {
        return  "platform-" + getApplicationName() + "-" + PLATFORM_NAME;
    }

    /**
     * Platform redis key (for cache).
     *
     * @retun key
     */
    private String getPlatformRedisCacheKey() {
        return "snapshotevents-" + getPlatformRedisKey();
    }

    @Test
    public void check_template_package_behavior_with_cache() throws Exception {
        // Create template package
        create_template_package();
        // Generate 150 to try create a snapshot
        generate_events_in_template_package(NB_EVENTS);
        // Read number of event
        read_nb_event_template_package(1 + NB_EVENTS); // 1 is create module
        // Clear cache and reload
        clear_cache_and_read_template_package();
        // Check nb snapshot in cache
        check_cache_event_template_package(3);
        // Delete template package
        delete_template_package();
    }

    @Test
    public void check_module_behavior_with_cache() throws Exception {
        // First we create a module
        create_module();
        // Now we add template (file) in this module
        add_template_in_module();
        // Generate 150 to try create a snapshot
        generate_events_in_template_in_module(NB_EVENTS);
        // Read number of event
        read_nb_event_module(1 + 1 + NB_EVENTS); // +1 for create, +1 for add template
        // Clear cache and reload
        clear_cache_and_read_module();
        // Check nb snapshot in cache
        check_cache_event_module(3);
        // Delete module
        delete_module();
    }

    @Test
    public void check_application_behavior_with_cache() throws Exception {
        // Create an application and platform
        create_platform();
        // Create a module to will be add in platform
        create_module();
        // We want a module with property
        add_template_in_module_with_property();
        // We add previous module into platform
        add_module_in_platform();
        // Generate 150 to try create a snapshot
        generate_events_in_platform(NB_EVENTS);
        // Read number of event
        read_nb_event_platform(1 + 1 + NB_EVENTS); // +1 for create, +1 for add template
        // Clear cache and reload
        clear_cache_and_read_platform();
        // Check nb snapshot in cache
        check_cache_event_platform(3);
        // Delete platform
        delete_platform();
        delete_module();
    }

    /**
     * Create a template package.
     *
     * @throws Exception
     */
    private void create_template_package() throws Exception {
        System.out.println("Create template package");

        final Template templateToCreate = createTemplate(MODULE_TEMPLATE_BASE_CONTENT);

        final Template templateCreated = this.hesClient.templatePackage().
                createWorkingcopy(getTemplatePackageName(), TEMPLATE_PACKAGE_VERSION, templateToCreate);

        checkTemplate(templateToCreate, templateCreated, 1, getTemplatePackagePath());
    }

    /**
     * Generate 150 template package update.
     */
    private void generate_events_in_template_package(final int nbEvent) {
        System.out.println(String.format("Generate %s events in template package", nbEvent));

        Template templateToCreate;
        Template templateUpdated;

        for (int index = 1; index < nbEvent + 1; index++) {
            System.out.println("Update template #" + index);

            templateToCreate = createTemplate(MODULE_TEMPLATE_BASE_CONTENT + index, index);

            templateUpdated = this.hesClient.templatePackage().
                    updateWorkingcopy(getTemplatePackageName(), TEMPLATE_PACKAGE_VERSION, templateToCreate);

            checkTemplate(templateToCreate, templateUpdated, index + 1, getTemplatePackagePath());
        }
    }

    /**
     * check number of event.
     *
     * @param nbEvent number of event asked
     */
    private void read_nb_event_template_package(final int nbEvent) {
        System.out.println(String.format("Check if found %d events", nbEvent));

        try (Jedis j = this.redisPool.getResource()) {
            final long nb = j.llen(getTemplatePackageRedisKey());

            assertThat(nb).isEqualTo(nbEvent);
        }
    }

    /**
     * Clear cache and read template package.
     */
    private void clear_cache_and_read_template_package() {
        this.hesClient.templatePackage().clearWorkingcopyCache(getTemplatePackageName(), TEMPLATE_PACKAGE_VERSION);

        final List<TemplateListItem> listTemplates = this.hesClient.templatePackage().listWorkingcopy(getTemplatePackageName(),
                TEMPLATE_PACKAGE_VERSION);

        assertThat(listTemplates.size()).isEqualTo(1);

        final TemplateListItem templateItem = listTemplates.get(0);

        assertThat(templateItem.getName()).isEqualTo(TEMPLATE_NAME);

        final Template tpl = this.hesClient.templatePackage().retreiveWorkingcopy(getTemplatePackageName(), TEMPLATE_PACKAGE_VERSION, TEMPLATE_NAME);

        assertThat(tpl.getContent()).isEqualTo(MODULE_TEMPLATE_BASE_CONTENT + NB_EVENTS);
    }

    /**
     * Check nb item in cache.
     *
     * @param nbItem
     */
    private void check_cache_event_template_package(final int nbItem) {
        System.out.println(String.format("Check if found %d item in cache", nbItem));

        try (Jedis j = this.redisCachePool.getResource()) {
            final long nb = j.llen(getTemplatePackageRedisCacheKey());

            assertThat(nb).isEqualTo(nbItem);
        }
    }

    /**
     * Delete template package.
     */
    private void delete_template_package() {
        System.out.println("Delete template package");

        this.hesClient.templatePackage().deleteWorkingcopy(getTemplatePackageName(), TEMPLATE_PACKAGE_VERSION);

        try (Jedis j = this.redisPool.getResource()) {
            j.del(getTemplatePackageRedisKey());
        }

        try (Jedis j = this.redisCachePool.getResource()) {
            j.del(getTemplatePackageRedisCacheKey());
        }

        this.hesClient.templatePackage().clearWorkingcopyCache(getTemplatePackageName(), TEMPLATE_PACKAGE_VERSION);
    }

    /**
     * Create a module.
     */
    private void create_module() {
        System.out.println("Create module");

        final ModuleClient module =  new ModuleClient(new ModuleKey(getModuleName(), new HesperidesVersion(MODULE_VERSION, true)), new HashSet<>());
        this.hesClient.module().createWorkingcopy(module);
    }

    /**
     * Add template in module
     */
    private void add_template_in_module() {
        System.out.println("Add template in module");

        final Template templateToCreate = createTemplate(MODULE_TEMPLATE_BASE_CONTENT);

        final Template templateCreated = this.hesClient.module().template().add(getModuleName(), MODULE_VERSION, templateToCreate);

        checkTemplate(templateToCreate, templateCreated, 1, getModulePath());
    }

    /**
     * Generate 150 template package update.
     */
    private void generate_events_in_template_in_module(final int nbEvent) {
        System.out.println(String.format("Generate %s events in module", nbEvent));

        Template templateToCreate;
        Template templateUpdated;

        for (int index = 1; index < nbEvent + 1; index++) {
            System.out.println("Update template # in module" + index);

            templateToCreate = createTemplate(MODULE_TEMPLATE_BASE_CONTENT + index, index);

            templateUpdated = this.hesClient.module().template()
                    .update(getModuleName(), MODULE_VERSION, templateToCreate);

            checkTemplate(templateToCreate, templateUpdated, index + 1, getModulePath());
        }
    }

    /**
     * check number of event.
     *
     * @param nbEvent number of event asked
     */
    private void read_nb_event_module(final int nbEvent) {
        System.out.println(String.format("Check if found %d events", nbEvent));

        try (Jedis j = this.redisPool.getResource()) {
            final long nb = j.llen(getModuleRedisKey());

            assertThat(nb).isEqualTo(nbEvent);
        }
    }

    /**
     * Clear cache and read module.
     */
    private void clear_cache_and_read_module() {
        this.hesClient.module().clearWorkingcopyCache(getModuleName(), MODULE_VERSION);

        final ModuleClient module = this.hesClient.module().retreiveWorkingcopy(getModuleName(), MODULE_VERSION);

        assertThat(module.getTechnos().size()).isEqualTo(0);

        final List<Template> listTemplates = this.hesClient.module().template().listWorkingcopy(getModuleName(), MODULE_VERSION);

        final Template templateItem = listTemplates.get(0);

        assertThat(templateItem.getName()).isEqualTo(TEMPLATE_NAME);

        final Template tpl = this.hesClient.module().template().retreiveWorkingcopy(getModuleName(), MODULE_VERSION, TEMPLATE_NAME);

        assertThat(tpl.getContent()).isEqualTo(MODULE_TEMPLATE_BASE_CONTENT + NB_EVENTS);
    }

    /**
     * Check nb item in cache.
     *
     * @param nbItem
     */
    private void check_cache_event_module(final int nbItem) {
        System.out.println(String.format("Check if found %d item in cache", nbItem));

        try (Jedis j = this.redisCachePool.getResource()) {
            final long nb = j.llen(getModuleRedisCacheKey());

            assertThat(nb).isEqualTo(nbItem);
        }
    }

    /**
     * Delete a module.
     */
    private void delete_module() {
        System.out.println("Delete module");

        this.hesClient.module().deleteWorkingcopy(getModuleName(), MODULE_VERSION);

        try (Jedis j = this.redisPool.getResource()) {
            j.del(getModuleRedisKey());
        }

        try (Jedis j = this.redisCachePool.getResource()) {
            j.del(getModuleRedisCacheKey());
        }

        this.hesClient.module().clearWorkingcopyCache(getModuleName(), MODULE_VERSION);
    }

    /**
     * Create platform.
     */
    private void create_platform() {
        System.out.println("Create platform");

        this.hesClient.application().platform().create(getApplicationName(), PLATFORM_NAME, MODULE_VERSION);
    }

    /**
     * Add template in module with property.
     */
    private void add_template_in_module_with_property() {
        System.out.println("Add template with property in module");

        final Template templateToCreate = createTemplate("myProp={{name_of_prop|@comment 'this is a comment' @default 'default_value' @pattern " +
                "'[a-z_]+'}}");

        final Template templateCreated = this.hesClient.module().template().add(getModuleName(), MODULE_VERSION, templateToCreate);

        checkTemplate(templateToCreate, templateCreated, 1, getModulePath());
    }

    /**
     * Add module in platform.
     */
    private void add_module_in_platform() {
        System.out.println("Add module in platform");

        final PlatformClient ptf = this.hesClient.application().platform().retreive(getApplicationName(), PLATFORM_NAME);

        final ModuleClient moduleToAdd = this.hesClient.module().retreiveWorkingcopy(getModuleName(), MODULE_VERSION);

        final Set<ApplicationModule> modules = ptf.getModules();

        modules.add(new ApplicationModule(moduleToAdd.getName(), moduleToAdd.getVersion(), moduleToAdd.isWorkingCopy(),
                PROPERTIES_PREFIX_PATH, new HashSet<>(), (int) moduleToAdd.getVersionID()));

        final PlatformClient ptfUpdate = new PlatformClient(ptf.getPlatformName(), ptf.getApplicationName(), ptf.getApplicationVersion(),
                ptf.isProduction(), modules, ptf.getVersionID());

        final PlatformClient ptfUpdated = this.hesClient.application().update(ptfUpdate);

        assertThat(ptfUpdated.getVersionID()).isEqualTo(ptfUpdate.getVersionID() + 1);
    }

    /**
     * Generate 150 event in platform.
     */
    private void generate_events_in_platform(final int nbEvent) {
        System.out.println(String.format("Generate %s events in platform", nbEvent));

        final PlatformClient ptf = this.hesClient.application().platform().retreive(getApplicationName(), PLATFORM_NAME);
        final long ptfVid = ptf.getVersionID();

        for (int index = 0; index < nbEvent; index++) {
            System.out.println("Update platform #" + index);

            // Key/Value properties
            final Set<KeyValueValorisation> keyValueProp = new HashSet<>();

            keyValueProp.add(new KeyValueValorisation("name_of_prop", "roger" + (index + 1)));

            // Properties
            final com.vsct.dt.hesperides.resources.Properties props = new Properties(keyValueProp, new HashSet<>());

            this.hesClient.application().properties().update(getApplicationName(), PLATFORM_NAME,
                    PROPERTIES_PREFIX_PATH + "#" + getModuleName() + "#" + MODULE_VERSION + "#WORKINGCOPY", ptfVid + index,
                    "This is a nice comment " + (index + 1), props);
        }
    }

    /**
     * check number of event.
     *
     * @param nbEvent number of event asked
     */
    private void read_nb_event_platform(final int nbEvent) {
        System.out.println(String.format("Check if found %d events", nbEvent));

        try (Jedis j = this.redisPool.getResource()) {
            final long nb = j.llen(getPlatformRedisKey());

            assertThat(nb).isEqualTo(nbEvent);
        }
    }

    /**
     * Clear cache and read module.
     */
    private void clear_cache_and_read_platform() {
        this.hesClient.application().clearCache(getApplicationName(), PLATFORM_NAME);

        final PlatformClient ptf = this.hesClient.application().platform().retreive(getApplicationName(), PLATFORM_NAME);

        assertThat(ptf.getModules().size()).isEqualTo(1);

        final ApplicationModule module = ptf.getModules().iterator().next();

        final Properties props = this.hesClient.application().properties().retreive(getApplicationName(), PLATFORM_NAME,
                module.getPropertiesPath());

        final KeyValueValorisation property = props.getKeyValueProperties().iterator().next();

        assertThat(property.getName()).isEqualTo("name_of_prop");

        assertThat(property.getValue()).isEqualTo("roger" + NB_EVENTS);
    }

    /**
     * Check nb item in cache.
     *
     * @param nbItem
     */
    private void check_cache_event_platform(final int nbItem) {
        System.out.println(String.format("Check if found %d item in cache", nbItem));

        try (Jedis j = this.redisCachePool.getResource()) {
            final long nb = j.llen(getPlatformRedisCacheKey());

            assertThat(nb).isEqualTo(nbItem);
        }
    }

    /**
     * Delete a module.
     */
    private void delete_platform() {
        System.out.println("Delete platform");

        this.hesClient.application().platform().delete(getApplicationName(), PLATFORM_NAME);

        try (Jedis j = this.redisPool.getResource()) {
            j.del(getPlatformRedisKey());
        }

        try (Jedis j = this.redisCachePool.getResource()) {
            j.del(getPlatformRedisCacheKey());
        }

        this.hesClient.application().clearCache(getApplicationName(), PLATFORM_NAME);
    }

    /**
     * Default template create with content.
     *
     * @param content
     *
     * @return
     */
    private static Template createTemplate(final String content) {
        return createTemplate(content, -1);
    }

    /**
     * Create a template with specified content.
     *
     * @param content the content
     * @param versionId id of tempalte
     *
     * @return
     */
    private static Template createTemplate(final String content, final int versionId) {
        TemplateFileRights userRights = new TemplateFileRights(true, false, false);
        TemplateRights templateRights = new TemplateRights(userRights, null, null);

        return new Template("", TEMPLATE_NAME, "name", "destination", content, templateRights, versionId);
    }

    /**
     * Check that template is correctly created.
     *
     * @param templateToCreate object sent to hesperides
     * @param templateCreated object receive from hesperides
     * @param versionId version id to be is in templateCreated
     * @param namespace namespace of template
     */
    private static void checkTemplate(final Template templateToCreate, final Template templateCreated, final int versionId, final String namespace) {
        assertThat(templateCreated.getNamespace()).isEqualTo(namespace);
        assertThat(templateCreated.getName()).isEqualTo(templateToCreate.getName());
        assertThat(templateCreated.getFilename()).isEqualTo(templateToCreate.getFilename());
        assertThat(templateCreated.getLocation()).isEqualTo(templateToCreate.getLocation());
        assertThat(templateCreated.getContent()).isEqualTo(templateToCreate.getContent());
        assertThat(templateCreated.getRights().getUser().isExecute()).isEqualTo(templateToCreate.getRights().getUser().isExecute());
        assertThat(templateCreated.getRights().getUser().isRead()).isEqualTo(templateToCreate.getRights().getUser().isRead());
        assertThat(templateCreated.getRights().getUser().isWrite()).isEqualTo(templateToCreate.getRights().getUser().isWrite());
        assertThat(templateCreated.getRights().getGroup()).isNull();
        assertThat(templateCreated.getRights().getOther()).isNull();
        assertThat(templateCreated.getVersionID()).isEqualTo(versionId);
    }
}

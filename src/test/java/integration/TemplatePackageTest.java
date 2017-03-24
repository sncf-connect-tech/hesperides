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

import static org.fest.assertions.api.Assertions.assertThat;
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
public class TemplatePackageTest extends AbstractIntegrationTest {
    /**
     * Template package name.
     */
    private static final String TEMPLATE_PACKAGE_NAME = "template_name";

    /**
     * Tempalte package version.
     */
    private static final String TEMPLATE_PACKAGE_VERSION = "1.0.0";

    /**
     * Template package properties path.
     */
    private static final String TEMPLATE_PACKAGE_PATH = "packages#" + TEMPLATE_PACKAGE_NAME + "#" + TEMPLATE_PACKAGE_VERSION + "#WORKINGCOPY";

    /**
     * Template package redis key (for data).
     */
    private static final String TEMPLATE_PACKAGE_REDIS_KEY = "template_package-" + TEMPLATE_PACKAGE_NAME + "-" + TEMPLATE_PACKAGE_VERSION + "-wc";

    /**
     * Template package redis key (for cache).
     */
    private static final String TEMPLATE_PACKAGE_REDIS_CACHE_KEY = "snapshotevents-template_package-" + TEMPLATE_PACKAGE_NAME + "-" +
        TEMPLATE_PACKAGE_VERSION + "-wc";

    // TODO generer un identifiant unique a placer devant les noms de template pour effacer plus facillement
    @Test
    public void check_template_package_behavior_with_cache() throws Exception {
        // Create template package
        create_template_package();
        // Generate 150 to try create a snapshot
        generate_events_in_template_package(372);
        // Read number of platform
        read_nb_event_template_package(1 + 372); // 1 is create module
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
        generate_events_in_template_in_module(372);
        // TODO créer un test qui verifie le nombre d'event
        // TODO créer un test qui supprime vide le cache mémoire et qui prend la dernière donnée
        // TODO créer un test qui vérifier les caches en base
        // Delete module
        delete_module(); // TODO clear cache in redis
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
        generate_events_in_platform(372);
        // TODO créer un test qui verifie le nombre d'event
        // TODO créer un test qui supprime vide le cache mémoire et qui prend la dernière donnée
        // TODO créer un test qui vérifier les caches en base
        // Delete platform
        delete_platform(); // TODO clear cache in redis
        delete_module();

    }

    /**
     * Create a template package.
     *
     * @throws Exception
     */
    private void create_template_package() throws Exception {
        System.out.println("Create template package");

        final Template templateToCreate = createTemplate("content");

        final Template templateCreated = this.hesClient.templatePackage().
                createWorkingcopy(TEMPLATE_PACKAGE_NAME, TEMPLATE_PACKAGE_VERSION, templateToCreate);

        checkTemplate(templateToCreate, templateCreated, 1, TEMPLATE_PACKAGE_PATH);
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

            templateToCreate = createTemplate("content" + index, index);

            templateUpdated = this.hesClient.templatePackage().
                    updateWorkingcopy(TEMPLATE_PACKAGE_NAME, TEMPLATE_PACKAGE_VERSION, templateToCreate);

            checkTemplate(templateToCreate, templateUpdated, index + 1, TEMPLATE_PACKAGE_PATH);
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
            final long nb = j.llen(TEMPLATE_PACKAGE_REDIS_KEY);

            assertThat(nb).isEqualTo(nbEvent);
        }
    }

    /**
     * Clear cache and read template package.
     */
    private void clear_cache_and_read_template_package() {
        this.hesClient.templatePackage().clearWorkingcopyCache(TEMPLATE_PACKAGE_NAME, TEMPLATE_PACKAGE_VERSION);

        final List<TemplateListItem> listTemplates = this.hesClient.templatePackage().retreiveListWorkingcopy(TEMPLATE_PACKAGE_NAME,
                TEMPLATE_PACKAGE_VERSION);

        assertThat(listTemplates.size()).isEqualTo(1);

        final TemplateListItem templateItem = listTemplates.get(0);

        assertThat(templateItem.getName()).isEqualTo("title");

        final Template tpl = this.hesClient.templatePackage().retreiveWorkingcopy(TEMPLATE_PACKAGE_NAME, TEMPLATE_PACKAGE_VERSION, "title");

        assertThat(tpl.getContent()).isEqualTo("content" + 372);
    }

    /**
     * Check nb item in cache.
     *
     * @param nbItem
     */
    private void check_cache_event_template_package(final int nbItem) {
        System.out.println(String.format("Check if found %d item in cache", nbItem));

        try (Jedis j = this.redisCachePool.getResource()) {
            final long nb = j.llen(TEMPLATE_PACKAGE_REDIS_CACHE_KEY);

            assertThat(nb).isEqualTo(nbItem);
        }
    }

    /**
     * Delete template package.
     */
    private void delete_template_package() {
        System.out.println("Delete template package");

        this.hesClient.templatePackage().deleteWorkingcopy(TEMPLATE_PACKAGE_NAME, TEMPLATE_PACKAGE_VERSION);

        try (Jedis j = this.redisPool.getResource()) {
            j.del(TEMPLATE_PACKAGE_REDIS_KEY);
        }

        try (Jedis j = this.redisCachePool.getResource()) {
            j.del(TEMPLATE_PACKAGE_REDIS_CACHE_KEY);
        }

        this.hesClient.templatePackage().clearWorkingcopyCache(TEMPLATE_PACKAGE_NAME, TEMPLATE_PACKAGE_VERSION);
    }

    /**
     * Create a module.
     */
    private void create_module() {
        System.out.println("Create module");

        final ModuleClient module =  new ModuleClient(new ModuleKey("module_name", new HesperidesVersion("1.0.0", true)), new HashSet<>());
        this.hesClient.module().createWorkingcopy(module);
    }

    /**
     * Add template in module
     */
    private void add_template_in_module() {
        System.out.println("Add template in module");

        final Template templateToCreate = createTemplate("content");

        final Template templateCreated = this.hesClient.module().template().add("module_name", "1.0.0", templateToCreate);

        checkTemplate(templateToCreate, templateCreated, 1, "modules#module_name#1.0.0#WORKINGCOPY");
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

            templateToCreate = createTemplate("content" + index, index);

            templateUpdated = this.hesClient.module().template()
                    .update("module_name", "1.0.0", templateToCreate);

            checkTemplate(templateToCreate, templateUpdated, index + 1, "modules#module_name#1.0.0#WORKINGCOPY");
        }
    }

    /**
     * Delete a module.
     */
    private void delete_module() {
        System.out.println("Delete module");

        this.hesClient.module().deleteWorkingcopy("module_name", "1.0.0");

        try (Jedis j = this.redisPool.getResource()) {
            j.del("module-module_name-1.0.0-wc");
        }

        this.hesClient.module().clearWorkingcopyCache("module_name", "1.0.0");
    }

    /**
     * Create platform.
     */
    private void create_platform() {
        System.out.println("Create platform");

        this.hesClient.application().platform().create("TEST_AUTO_INT", "PTF1", "1.0.0");
    }

    /**
     * Add template in module with property.
     */
    private void add_template_in_module_with_property() {
        System.out.println("Add template with property in module");

        final Template templateToCreate = createTemplate("myProp={{name_of_prop|@comment 'this is a comment' @default 'default_value' @pattern " +
                "'[a-z_]+'}}");

        final Template templateCreated = this.hesClient.module().template().add("module_name", "1.0.0", templateToCreate);

        checkTemplate(templateToCreate, templateCreated, 1, "modules#module_name#1.0.0#WORKINGCOPY");
    }

    /**
     * Add module in platform.
     */
    private void add_module_in_platform() {
        System.out.println("Add module in platform");

        final PlatformClient ptf = this.hesClient.application().platform().retreive("TEST_AUTO_INT", "PTF1");

        final ModuleClient moduleToAdd = this.hesClient.module().retreiveWorkingcopy("module_name", "1.0.0");

        final Set<ApplicationModule> modules = ptf.getModules();

        modules.add(new ApplicationModule(moduleToAdd.getName(), moduleToAdd.getVersion(), moduleToAdd.isWorkingCopy(),
                "#TEST#INT", new HashSet<>(), (int) moduleToAdd.getVersionID()));

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

        final PlatformClient ptf = this.hesClient.application().platform().retreive("TEST_AUTO_INT", "PTF1");
        final long ptfVid = ptf.getVersionID();

        for (int index = 0; index < nbEvent; index++) {
            System.out.println("Update platform #" + index);

            // Key/Value properties
            final Set<KeyValueValorisation> keyValueProp = new HashSet<>();

            keyValueProp.add(new KeyValueValorisation("name_of_prop", "roger" + (index + 1)));

            // Properties
            final com.vsct.dt.hesperides.resources.Properties props = new Properties(keyValueProp, new HashSet<>());

            this.hesClient.application().properties().update("TEST_AUTO_INT", "PTF1", "#TEST#INT#module_name#1.0.0#WORKINGCOPY", ptfVid + index,
                    "This is a nice comment " + (index + 1), props);
        }
    }

    /**
     * Delete a module.
     */
    private void delete_platform() {
        System.out.println("Delete platform");

        this.hesClient.application().platform().delete("TEST_AUTO_INT", "PTF1");

        try (Jedis j = this.redisPool.getResource()) {
            j.del("platform-TEST_AUTO_INT-PTF1");
        }

        this.hesClient.application().clearCache("TEST_AUTO_INT", "PTF1");
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

        return new Template("", "title", "name", "destination", content, templateRights, versionId);
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

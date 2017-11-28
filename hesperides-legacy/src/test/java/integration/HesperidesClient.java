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

import com.vsct.dt.hesperides.MainApplication;
import com.vsct.dt.hesperides.applications.PlatformKey;
import com.vsct.dt.hesperides.resources.HesperidesVersionsResource.Versions;
import com.vsct.dt.hesperides.resources.TemplateListItem;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import integration.client.ModuleClient;
import integration.client.PlatformClient;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;

/**
 * Created by emeric_martineau on 10/03/2017.
 */
public class HesperidesClient {
    /**
     * Final url for Hesperides.
     */
    private final String url;

    /**
     * Base 64 credential.
     */
    private String credential;

    private final TemplatePackage templatePackage;

    private final Module module;

    private final Application application;

    /**
     * Http client.
     */
    private Client httpClient;

    @ClassRule
    public static final DropwizardAppRule RULE =
            new DropwizardAppRule(MainApplication.class);

    public HesperidesClient(final String url) {

        // Add last '/'
        if (!url.endsWith("/")) {
            this.url = url.concat("/rest/");
        } else {
            this.url = url.concat("rest/");
        }

        this.httpClient = new JerseyClientBuilder(RULE.getEnvironment()).build("test client");

        this.templatePackage = new TemplatePackage();
        this.module = new Module();
        this.application = new Application();
        this.credential = null;
    }

    public HesperidesClient(final String url, final String username, final String password) {
        this(url);

        this.credential = "Basic " + Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes());
    }

    /**
     * Get Hesperides version.
     *
     * @return
     */
    public Versions getVersion() {
        return this.httpClient.target(url + "versions")
                .request().header(HttpHeaders.AUTHORIZATION, credential)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .get(Versions.class);
    }

    public TemplatePackage templatePackage() {
        return templatePackage;
    }

    public Module module() {
        return module;
    }

    public Application application() {
        return application;
    }

    /**
     * Sub part of template package.
     */
    public class TemplatePackage {
        /**
         * Get template list.
         *
         * @param name    name of template package
         * @param version version
         * @return
         */
        public List<TemplateListItem> listWorkingcopy(final String name, final String version) {
            return httpClient.target(url +
                    String.format("templates/packages/%s/%s/workingcopy/templates", name, version))
                    .request().header(HttpHeaders.AUTHORIZATION, credential)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .get(new GenericType<List<TemplateListItem>>() {
                    });
        }

        /**
         * Get a template in template package.
         *
         * @param name    name of template package
         * @param version version
         * @param tplName template name
         * @return
         */
        public Template retreiveWorkingcopy(final String name, final String version, final String tplName) {
            return httpClient.target(url +
                    String.format("templates/packages/%s/%s/workingcopy/templates/%s", name, version, tplName))
                    .request().header(HttpHeaders.AUTHORIZATION, credential)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .get(Template.class);
        }

        /**
         * Create a template package.
         *
         * @param name     name of template package
         * @param version  version
         * @param template template
         * @return template created (with namespace)
         */
        public Template createWorkingcopy(final String name, final String version, final Template template) {
            return httpClient.target(url +
                    String.format("templates/packages/%s/%s/workingcopy/templates", name, version))
                    .request().header(HttpHeaders.AUTHORIZATION, credential)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.json(template), Template.class);
        }

        /**
         * Update a template package.
         *
         * @param name     name of template package
         * @param version  version
         * @param template template
         * @return template created (with namespace)
         */
        public Template updateWorkingcopy(final String name, final String version, final Template template) {
            return httpClient.target(url +
                    String.format("templates/packages/%s/%s/workingcopy/templates", name, version))
                    .request().header(HttpHeaders.AUTHORIZATION, credential)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .put(Entity.json(template), Template.class);
        }

        /**
         * Delete a template package.
         *
         * @param name    name of template package
         * @param version version
         */
        public void deleteWorkingcopy(final String name, final String version) {
            httpClient.target(url +
                    String.format("templates/packages/%s/%s/workingcopy", name, version))
                    .request().header(HttpHeaders.AUTHORIZATION, credential)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .delete();
        }

        /**
         * Clear one package template.
         *
         * @param name    name of template package
         * @param version version
         */
        public void clearWorkingcopyCache(final String name, final String version) {
            httpClient.target(url + String.format("cache/template/package/%s/%s/workingcopy", name, version))
                    .request().header(HttpHeaders.AUTHORIZATION, credential)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .delete();
        }
    }

    /**
     * Sub part of module.
     */
    public class Module {
        /**
         * Sub part of module.
         */
        public class Template {
            /**
             * Add template in module.
             *
             * @param name     name of module
             * @param version  version of module
             * @param template template to be created
             * @return template create
             */
            public com.vsct.dt.hesperides.templating.modules.template.Template add(final String name, final String version,
                                                                                   final com.vsct.dt.hesperides.templating.modules.template.Template template) {
                return httpClient.target(url +
                        String.format("modules/%s/%s/workingcopy/templates", name, version))
                        .request().header(HttpHeaders.AUTHORIZATION, credential)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .post(Entity.json(template), com.vsct.dt.hesperides.templating.modules.template.Template.class);
            }

            /**
             * Update template in module.
             *
             * @param name     name of module
             * @param version  version of module
             * @param template template to be created
             * @return template create
             */
            public com.vsct.dt.hesperides.templating.modules.template.Template update(final String name, final String version,
                                                                                      final com.vsct.dt.hesperides.templating.modules.template.Template template) {
                return httpClient.target(url +
                        String.format("modules/%s/%s/workingcopy/templates", name, version))
                        .request().header(HttpHeaders.AUTHORIZATION, credential)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .put(Entity.json(template), com.vsct.dt.hesperides.templating.modules.template.Template.class);
            }

            /**
             * Get template list.
             *
             * @param name    name of template package
             * @param version version
             * @return
             */
            public List<com.vsct.dt.hesperides.templating.modules.template.Template> listWorkingcopy(final String name, final String version) {
                return httpClient.target(url +
                        String.format("modules/%s/%s/workingcopy/templates", name, version))
                        .request().header(HttpHeaders.AUTHORIZATION, credential)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(new GenericType<List<com.vsct.dt.hesperides.templating.modules.template.Template>>() {
                        });
            }

            /**
             * Get template in module.
             *
             * @param name    name of module
             * @param version version of module
             * @param tplName template name in module
             * @return
             */
            public com.vsct.dt.hesperides.templating.modules.template.Template retreiveWorkingcopy(final String name, final String version,
                                                                                                   final String tplName) {
                return httpClient.target(url +
                        String.format("modules/%s/%s/workingcopy/templates/%s", name, version, tplName))
                        .request().header(HttpHeaders.AUTHORIZATION, credential)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(com.vsct.dt.hesperides.templating.modules.template.Template.class);
            }
        }

        private final Template template = new Template();

        /**
         * Create a module.
         *
         * @param module module data
         * @return module created
         */
        public ModuleClient createWorkingcopy(final ModuleClient module) {
            return httpClient.target(url + "modules")
                    .request().header(HttpHeaders.AUTHORIZATION, credential)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.json(module), ModuleClient.class);
        }

        /**
         * Delete workingcopy module.
         *
         * @param name    name of module
         * @param version version of module
         */
        public void deleteWorkingcopy(final String name, final String version) {
            httpClient.target(url +
                    String.format("modules/%s/%s/workingcopy", name, version))
                    .request().header(HttpHeaders.AUTHORIZATION, credential)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .delete();
        }

        /**
         * Get workingcopy module.
         *
         * @param name    name of module
         * @param version version of module
         * @return
         */
        public ModuleClient retreiveWorkingcopy(final String name, final String version) {
            return httpClient.target(url +
                    String.format("modules/%s/%s/workingcopy", name, version))
                    .request().header(HttpHeaders.AUTHORIZATION, credential)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .get(ModuleClient.class);
        }

        /**
         * Return template sub part.
         *
         * @return
         */
        public Template template() {
            return this.template;
        }

        /**
         * Clear all cache.
         */
        public void clearWorkingcopyCache(final String name, final String version) {
            httpClient.target(url + String.format("cache/module/%s/%s/workingcopy", name, version))
                    .request().header(HttpHeaders.AUTHORIZATION, credential)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .delete();
        }
    }

    /**
     * Sub part of application.
     */
    public class Application {
        /**
         * Sub part of Application.
         */
        public class Properties {
            /**
             * Update properties.
             *
             * @param appName application name
             * @param ptfName platform name
             * @param path    path of properties
             * @param pftVid  platform version_id
             * @param comment comment of update
             * @param props   properties
             * @return
             */
            public com.vsct.dt.hesperides.resources.Properties update(final String appName, final String ptfName,
                                                                      final String path, final long pftVid, final String comment,
                                                                      final com.vsct.dt.hesperides.resources.Properties props) {

                return httpClient.target(url +
                        String.format("applications/%s/platforms/%s/properties", appName, ptfName))
                        .queryParam("path", path)
                        .queryParam("platform_vid", String.valueOf(pftVid))
                        .queryParam("comment", comment)
                        .request().header(HttpHeaders.AUTHORIZATION, credential)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .post(Entity.json(props), com.vsct.dt.hesperides.resources.Properties.class);
            }

            /**
             * Get properties.
             *
             * @param appName application name
             * @param ptfName platform name
             * @param path    path of properties
             * @return
             */
            public com.vsct.dt.hesperides.resources.Properties retreive(final String appName, final String ptfName, final String path) {
                return httpClient.target(url +
                        String.format("applications/%s/platforms/%s/properties", appName, ptfName))
                        .queryParam("path", path)
                        .request().header(HttpHeaders.AUTHORIZATION, credential)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(com.vsct.dt.hesperides.resources.Properties.class);
            }
        }

        /**
         * Sub part of application.
         */
        public class Platform {
            /**
             * Create a platform for an application.
             *
             * @param name       name of application
             * @param ptfName    name of platform
             * @param ptfVersion version of platform
             * @return
             */
            public PlatformClient create(final String name, final String ptfName, final String ptfVersion) {
                final PlatformClient ptf = new PlatformClient(new PlatformKey(name, ptfName), ptfVersion, false, new HashSet<>());
                return httpClient.target(url +
                        String.format("applications/%s/platforms", name))
                        .request().header(HttpHeaders.AUTHORIZATION, credential)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .post(Entity.json(ptf), PlatformClient.class);
            }

            /**
             * Get platform.
             *
             * @param name    application name
             * @param ptfName platform name
             * @return
             */
            public PlatformClient retreive(final String name, final String ptfName) {
                return httpClient.target(url +
                        String.format("applications/%s/platforms/%s", name, ptfName))
                        .request().header(HttpHeaders.AUTHORIZATION, credential)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(PlatformClient.class);
            }

            /**
             * Create a platform for an application.
             *
             * @param name    name of application
             * @param ptfName name of platform
             * @return
             */
            public void delete(final String name, final String ptfName) {
                httpClient.target(url +
                        String.format("applications/%s/platforms/%s", name, ptfName))
                        .request().header(HttpHeaders.AUTHORIZATION, credential)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .delete();
            }
        }

        private Properties properties = new Properties();

        private Platform platform = new Platform();

        /**
         * Update platform.
         *
         * @param ptf platform to update
         * @return
         */
        public PlatformClient update(final PlatformClient ptf) {
            Entity<PlatformClient> entity = Entity.json(ptf);
            return httpClient.target(url + String.format(
                    "applications/TEST_AUTO_INT/platforms?copyPropertiesForUpgradedModules=false", ptf.getApplicationName()))
                    .request().header(HttpHeaders.AUTHORIZATION, credential)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .put(entity, PlatformClient.class);

        }

        /**
         * Properties of application.
         *
         * @return
         */
        public Properties properties() {
            return this.properties;
        }

        /**
         * Platform of application.
         *
         * @return
         */
        public Platform platform() {
            return this.platform;
        }

        /**
         * Clear cache.
         *
         * @param applicationName application name
         * @param ptfName         platform name
         */
        public void clearCache(final String applicationName, final String ptfName) {
            httpClient.target(url + String.format("cache/application/%s/%s", applicationName, ptfName))
                    .request().header(HttpHeaders.AUTHORIZATION, credential)
                    .accept(MediaType.APPLICATION_JSON_TYPE)
                    .delete();
        }
    }
}

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
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import integration.client.ModuleClient;
import integration.client.PlatformClient;

import com.vsct.dt.hesperides.applications.PlatformKey;
import com.vsct.dt.hesperides.resources.HesperidesVersionsResource.Versions;
import com.vsct.dt.hesperides.templating.modules.template.Template;

/**
 * Created by emeric_martineau on 10/03/2017.
 */
public class HesperidesClient {
    /**
     * Final url for Hesperides.
     */
    private final String url;

    private final TemplatePackage templatePackage;

    private final Module module;

    private final Application application;

    /**
     * Http client.
     */
    private Client httpClient;

    public HesperidesClient(final String url) {

        // Add last '/'
        if (!url.endsWith("/")) {
            this.url = url.concat("/rest/");
        } else {
            this.url = url.concat("rest/");
        }

        this.httpClient = Client.create(new DefaultClientConfig());

        this.templatePackage = new TemplatePackage();
        this.module = new Module();
        this.application = new Application();
    }

    /**
     * Get Hesperides version.
     *
     * @return
     */
    public Versions getVersion() {
        return this.httpClient.resource(url + "versions")
                .type(MediaType.APPLICATION_JSON_TYPE)
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
         * Create a template package.
         *
         * @param name name of template package
         * @param version version
         * @param template template
         *
         * @return template created (with namespace)
         */
        public Template createWorkingcopy(final String name, final String version, final Template template) {
            return httpClient.resource(url +
                    String.format("templates/packages/%s/%s/workingcopy/templates", name, version))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(Template.class, template);
        }

        /**
         * Update a template package.
         *
         * @param name name of template package
         * @param version version
         * @param template template
         *
         * @return template created (with namespace)
         */
        public Template updateWorkingcopy(final String name, final String version, final Template template) {
            return httpClient.resource(url +
                    String.format("templates/packages/%s/%s/workingcopy/templates", name, version))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .put(Template.class, template);
        }

        /**
         * Delete a template package.
         *
         * @param name name of template package
         * @param version version
         */
        public void deleteWorkingcopy(final String name, final String version) {
            httpClient.resource(url +
                    String.format("templates/packages/%s/%s/workingcopy", name, version))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .delete();
        }

        /**
         * Clear all cache.
         */
        public void clearCache() {
            httpClient.resource(url + "cache/templates/packages")
                    .type(MediaType.APPLICATION_JSON_TYPE)
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
             * @param name name of module
             * @param version version of module
             * @param template template to be created
             *
             * @return template create
             */
            public com.vsct.dt.hesperides.templating.modules.template.Template add(final String name, final String version,
                    final com.vsct.dt.hesperides.templating.modules.template.Template template) {
                return httpClient.resource(url +
                        String.format("modules/%s/%s/workingcopy/templates", name, version))
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .post(com.vsct.dt.hesperides.templating.modules.template.Template.class, template);
            }

            /**
             * Update template in module.
             *
             * @param name name of module
             * @param version version of module
             * @param template template to be created
             *
             * @return template create
             */
            public com.vsct.dt.hesperides.templating.modules.template.Template update(final String name, final String version,
                    final com.vsct.dt.hesperides.templating.modules.template.Template template) {
                return httpClient.resource(url +
                        String.format("modules/%s/%s/workingcopy/templates", name, version))
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .put(com.vsct.dt.hesperides.templating.modules.template.Template.class, template);
            }
        }

        private final Template template = new Template();

        /**
         * Create a module.
         *
         * @param module module data
         *
         * @return module created
         */
        public ModuleClient createWorkingcopy(final ModuleClient module) {
            return httpClient.resource(url + "modules")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .post(ModuleClient.class, module);
        }

        /**
         * Delete workingcopy module.
         *
         * @param name name of module
         * @param version version of module
         */
        public void deleteWorkingcopy(final String name, final String version) {
            httpClient.resource(url +
                    String.format("modules/%s/%s/workingcopy", name, version))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .delete();
        }

        /**
         * Get workingcopy module.
         *
         * @param name name of module
         * @param version version of module
         *
         * @return
         */
        public ModuleClient retreiveWorkingcopy(final String name, final String version) {
            return httpClient.resource(url +
                    String.format("modules/%s/%s/workingcopy", name, version))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .get(ModuleClient.class);
        }

        /**
         * Return template sub part.
         * @return
         */
        public Template template() {
            return this.template;
        }

        /**
         * Clear all cache.
         */
        public void clearCache() {
            httpClient.resource(url + "cache/modules")
                    .type(MediaType.APPLICATION_JSON_TYPE)
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
            public com.vsct.dt.hesperides.resources.Properties update(final String appName, final String ptfName,
                    final String path, final long pftVid, final String comment,
                    final com.vsct.dt.hesperides.resources.Properties props) {
                //http://192.168.99.100:8080/rest/applications/TEST_AUTO_INT/platforms/PTF1/properties?path=%23TEST%23INT%23module_name%231.0.0
                // %23WORKINGCOPY&platform_vid=2&comment=This%20is%20a%20nice%20comment

                return httpClient.resource(url +
                        String.format("applications/%s/platforms/%s/properties", appName, ptfName))
                        .queryParam("path", path)
                        .queryParam("platform_vid", String.valueOf(pftVid))
                        .queryParam("comment", comment)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .post(com.vsct.dt.hesperides.resources.Properties.class, props);
            }
        }

        /**
         * Sub part of application.
         */
        public class Platform {
            /**
             * Create a platform for an application.
             *
             * @param name name of application
             * @param ptfName name of platform
             * @param ptfVersion version of platform
             * @return
             */
            public PlatformClient create(final String name, final String ptfName, final String ptfVersion) {
                final PlatformClient ptf = new PlatformClient(new PlatformKey(name, ptfName), ptfVersion, false, new HashSet<>());

                return httpClient.resource(url +
                        String.format("applications/%s/platforms", name))
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .post(PlatformClient.class, ptf);
            }

            /**
             * Get platform.
             *
             * @param name application name
             * @param ptfName platform name
             * @return
             */
            public PlatformClient retreive(final String name, final String ptfName) {
                return httpClient.resource(url +
                        String.format("applications/%s/platforms/%s", name, ptfName))
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .get(PlatformClient.class);
            }

            /**
             * Create a platform for an application.
             *
             * @param name name of application
             * @param ptfName name of platform
             *
             * @return
             */
            public void delete(final String name, final String ptfName) {
                httpClient.resource(url +
                        String.format("applications/%s/platforms/%s", name, ptfName))
                        .type(MediaType.APPLICATION_JSON_TYPE)
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
            return httpClient.resource(url + String.format(
                    "applications/TEST_AUTO_INT/platforms?copyPropertiesForUpgradedModules=false", ptf.getApplicationName()))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .put(PlatformClient.class, ptf);

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
         * Clear all cache.
         */
        public void clearCache() {
            httpClient.resource(url + "cache/applications")
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .delete();
        }

    }
}

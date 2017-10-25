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

import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.client.Client;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;

import integration.client.ModuleClient;
import integration.client.PlatformClient;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.setup.Environment;

import com.vsct.dt.hesperides.applications.PlatformKey;
import com.vsct.dt.hesperides.resources.HesperidesVersionsResource.Versions;
import com.vsct.dt.hesperides.resources.TemplateListItem;
import com.vsct.dt.hesperides.templating.modules.template.Template;

/**
 * Created by emeric_martineau on 10/03/2017.
 */
public class HesperidesClient {
    /**
     * Builder of event from JSON.
     */
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

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

    public HesperidesClient(final String url) {

        // Add last '/'
        if (!url.endsWith("/")) {
            this.url = url.concat("/rest/");
        } else {
            this.url = url.concat("rest/");
        }

        final Environment e = new Environment("HesperidesClientEnvironment", MAPPER, Validators.newValidator(),
                new MetricRegistry(), this.getClass().getClassLoader());

        this.httpClient = new JerseyClientBuilder(e).build("HesperidesClient");

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
     * Send request with authorization.
     *
     * @param target url
     *
     * @return
     */
    private Builder send(final String target) {
        return this.httpClient.target(target)
                .request()
                .header(HttpHeaders.AUTHORIZATION, credential);
    }

    /**
     * Send request with authorization.
     *
     * @param target url
     *
     * @return
     */
    private Builder send(final String target, final Map<String, String> queryparam) {
        final WebTarget webTarget = this.httpClient.target(target);

        for(Entry<String, String> query : queryparam.entrySet()) {
            webTarget.queryParam(query.getKey(), query.getValue());
        }

        return webTarget
                .request()
                .header(HttpHeaders.AUTHORIZATION, credential);
    }

    /**
     * Get Hesperides version.
     *
     * @return
     */
    public Versions getVersion() {
        return send(url + "versions").get(Versions.class);
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
         * @param name name of template package
         * @param version version
         *
         * @return
         */
        public List<TemplateListItem> listWorkingcopy(final String name, final String version) {
            return send(url + String.format("templates/packages/%s/%s/workingcopy/templates", name, version))
                    .get(new GenericType<List<TemplateListItem>>(){});
        }

        /**
         * Get a template in template package.
         *
         * @param name name of template package
         * @param version version
         * @param tplName template name
         *
         * @return
         */
        public Template retreiveWorkingcopy(final String name, final String version, final String tplName) {
            return send(url + String.format("templates/packages/%s/%s/workingcopy/templates/%s", name, version, tplName))
                    .get(Template.class);
        }

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
            return send(url + String.format("templates/packages/%s/%s/workingcopy/templates", name, version))
                    .post(Entity.json(template))
                    .readEntity(Template.class);
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
            return send(url + String.format("templates/packages/%s/%s/workingcopy/templates", name, version))
                    .put(Entity.json(template))
                    .readEntity(Template.class);
        }

        /**
         * Delete a template package.
         *
         * @param name name of template package
         * @param version version
         */
        public void deleteWorkingcopy(final String name, final String version) {
            send(url + String.format("templates/packages/%s/%s/workingcopy", name, version))
                    .delete();
        }

         /**
         * Clear one package template.
         *
         * @param name name of template package
         * @param version version
         */
        public void clearWorkingcopyCache(final String name, final String version) {
            send(url + String.format("cache/template/package/%s/%s/workingcopy", name, version))
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
                return send(url + String.format("modules/%s/%s/workingcopy/templates", name, version))
                        .post(Entity.json(template)).readEntity(com.vsct.dt.hesperides.templating.modules.template.Template.class);
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
                return send(url + String.format("modules/%s/%s/workingcopy/templates", name, version))
                        .put(Entity.json(template)).readEntity(com.vsct.dt.hesperides.templating.modules.template.Template.class);
            }

            /**
             * Get template list.
             *
             * @param name name of template package
             * @param version version
             *
             * @return
             */
            public List<com.vsct.dt.hesperides.templating.modules.template.Template> listWorkingcopy(final String name, final String version) {
                return send(url + String.format("modules/%s/%s/workingcopy/templates", name, version))
                        .get(new GenericType<List<com.vsct.dt.hesperides.templating.modules.template.Template>>(){});
            }

            /**
             * Get template in module.
             *
             * @param name name of module
             * @param version version of module
             * @param tplName template name in module
             *
             * @return
             */
            public com.vsct.dt.hesperides.templating.modules.template.Template retreiveWorkingcopy(final String name, final String version,
                    final String tplName) {
                return send(url + String.format("modules/%s/%s/workingcopy/templates/%s", name, version, tplName))
                        .get(com.vsct.dt.hesperides.templating.modules.template.Template.class);
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
            return send(url + "modules")
                    .post(Entity.json(module))
                    .readEntity(ModuleClient.class);
        }

        /**
         * Delete workingcopy module.
         *
         * @param name name of module
         * @param version version of module
         */
        public void deleteWorkingcopy(final String name, final String version) {
            send(url + String.format("modules/%s/%s/workingcopy", name, version))
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
            return send(url + String.format("modules/%s/%s/workingcopy", name, version))
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
        public void clearWorkingcopyCache(final String name, final String version) {
            send(url + String.format("cache/module/%s/%s/workingcopy", name, version))
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
             * @param path path of properties
             * @param pftVid platform version_id
             * @param comment comment of update
             * @param props properties
             *
             * @return
             */
            public com.vsct.dt.hesperides.resources.Properties update(final String appName, final String ptfName,
                    final String path, final long pftVid, final String comment,
                    final com.vsct.dt.hesperides.resources.Properties props) {

                final Map<String, String> queryParam = new HashMap<>();

                queryParam.put("path", path);
                queryParam.put("platform_vid", String.valueOf(pftVid));
                queryParam.put("comment", comment);

                return send(url + String.format("applications/%s/platforms/%s/properties", appName, ptfName), queryParam)
                        .post(Entity.json(props))
                        .readEntity(com.vsct.dt.hesperides.resources.Properties.class);
            }

            /**
             * Get properties.
             *
             * @param appName application name
             * @param ptfName platform name
             * @param path path of properties
             *
             * @return
             */
            public com.vsct.dt.hesperides.resources.Properties retreive(final String appName, final String ptfName, final String path) {
                final Map<String, String> queryParam = new HashMap<>();

                queryParam.put("path", path);

                return send(url + String.format("applications/%s/platforms/%s/properties", appName, ptfName), queryParam)
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
             * @param name name of application
             * @param ptfName name of platform
             * @param ptfVersion version of platform
             * @return
             */
            public PlatformClient create(final String name, final String ptfName, final String ptfVersion) {
                final PlatformClient ptf = new PlatformClient(new PlatformKey(name, ptfName), ptfVersion, false, new HashSet<>());

                return send(url + String.format("applications/%s/platforms", name))
                        .post(Entity.json(ptf))
                        .readEntity(PlatformClient.class);
            }

            /**
             * Get platform.
             *
             * @param name application name
             * @param ptfName platform name
             * @return
             */
            public PlatformClient retreive(final String name, final String ptfName) {
                return send(url + String.format("applications/%s/platforms/%s", name, ptfName))
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
                send(url + String.format("applications/%s/platforms/%s", name, ptfName))
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
            return send(url + String.format("applications/TEST_AUTO_INT/platforms?copyPropertiesForUpgradedModules=false", ptf.getApplicationName()))
                    .put(Entity.json(ptf))
                    .readEntity(PlatformClient.class);

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
         * @param ptfName platform name
         */
        public void clearCache(final String applicationName, final String ptfName) {
            send(url + String.format("cache/application/%s/%s", applicationName, ptfName))
                    .delete();
        }
    }
}

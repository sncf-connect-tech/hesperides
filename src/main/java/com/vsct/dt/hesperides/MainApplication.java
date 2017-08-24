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

package com.vsct.dt.hesperides;

import com.bazaarvoice.dropwizard.assets.ConfiguredAssetsBundle;
import com.codahale.metrics.JmxReporter;
import com.google.common.eventbus.EventBus;

import com.vsct.dt.hesperides.applications.Applications;
import com.vsct.dt.hesperides.applications.ApplicationsAggregate;
import com.vsct.dt.hesperides.applications.SnapshotRegistry;
import com.vsct.dt.hesperides.applications.SnapshotRegistryInterface;
import com.vsct.dt.hesperides.cache.HesperidesCacheResource;
import com.vsct.dt.hesperides.events.EventsAggregate;
import com.vsct.dt.hesperides.exception.wrapper.IllegalArgumentExceptionMapper;
import com.vsct.dt.hesperides.exception.wrapper.*;
import com.vsct.dt.hesperides.feedback.FeedbackConfiguration;
import com.vsct.dt.hesperides.feedback.FeedbacksAggregate;
import com.vsct.dt.hesperides.files.Files;
import com.vsct.dt.hesperides.healthcheck.AggregateHealthCheck;
import com.vsct.dt.hesperides.healthcheck.ElasticSearchHealthCheck;
import com.vsct.dt.hesperides.healthcheck.EventStoreHealthCheck;
import com.vsct.dt.hesperides.indexation.ElasticSearchClient;
import com.vsct.dt.hesperides.indexation.ElasticSearchIndexationExecutor;
import com.vsct.dt.hesperides.indexation.listeners.ModuleEventsIndexation;
import com.vsct.dt.hesperides.indexation.listeners.PlatformEventsIndexation;
import com.vsct.dt.hesperides.indexation.listeners.TemplateEventsIndexation;
import com.vsct.dt.hesperides.indexation.search.ApplicationSearch;
import com.vsct.dt.hesperides.indexation.search.ModuleSearch;
import com.vsct.dt.hesperides.indexation.search.TemplateSearch;
import com.vsct.dt.hesperides.resources.*;
import com.vsct.dt.hesperides.security.DisabledAuthenticator;
import com.vsct.dt.hesperides.security.ThreadLocalUserContext;
import com.vsct.dt.hesperides.security.jersey.HesperidesAuthenticator;
import com.vsct.dt.hesperides.security.jersey.NoCredentialAuthFilter;
import com.vsct.dt.hesperides.security.model.User;
import com.vsct.dt.hesperides.storage.RedisEventStore;
import com.vsct.dt.hesperides.templating.modules.ModulesAggregate;
import com.vsct.dt.hesperides.templating.packages.TemplatePackagesAggregate;
import com.vsct.dt.hesperides.templating.packages.virtual.CacheGeneratorApplicationAggregate;
import com.vsct.dt.hesperides.templating.packages.virtual.CacheGeneratorModuleAggregate;
import com.vsct.dt.hesperides.templating.packages.virtual.CacheGeneratorTemplatePackagesAggregate;
import com.vsct.dt.hesperides.util.ManageableJedisConnection;
import com.vsct.dt.hesperides.util.ManageableJedisConnectionInterface;
import com.vsct.dt.hesperides.util.converter.ApplicationConverter;
import com.vsct.dt.hesperides.util.converter.PropertiesConverter;
import com.vsct.dt.hesperides.util.converter.TimeStampedPlatformConverter;
import com.vsct.dt.hesperides.util.converter.impl.DefaultApplicationConverter;
import com.vsct.dt.hesperides.util.converter.impl.DefaultPropertiesConverter;
import com.vsct.dt.hesperides.util.converter.impl.DefaultTimeStampedPlatformConverter;
import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsScanner;
import com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider;
import com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jaxrs.listing.ResourceListingProvider;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.reader.ClassReaders;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.client.HttpClientBuilder;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.http.client.HttpClient;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class MainApplication extends Application<HesperidesConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApplication.class);

    /**
     * Replace ${xxx} by value of environment variable xxx
     *
     * @param line the string to read
     *
     * @return new string
     */
    private static String expandString(final String line) {
        final StrSubstitutor substitutor = new EnvironmentVariableSubstitutor(false);
        return substitutor.replace(line);
    }

    public static void main(final String[] args) throws Exception {
        if (args == null || args.length == 0) {
            final String[] newArgs = {"server", expandString(System.getenv("HESPERIDES_CONFIG_FILE"))};

            new MainApplication().run(newArgs);
        } else if (args.length == 2) {
            final String[] newArgs = {args[0], expandString(args[1])};

            new MainApplication().run(newArgs);
        } else {
            new MainApplication().run(args);
        }
    }

    @Override
    public String getName() {
        return "hesperides";
    }

    @Override
    public void initialize(final Bootstrap<HesperidesConfiguration> hesperidesConfigurationBootstrap) {
        // Enable variable substitution with environment variables
        hesperidesConfigurationBootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(hesperidesConfigurationBootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        hesperidesConfigurationBootstrap.addBundle(new ConfiguredAssetsBundle("/assets/", "/", "index.html"));
    }

    @Override
    public void run(final HesperidesConfiguration hesperidesConfiguration, final Environment environment) throws Exception {
        // ajoute swagger
        LOGGER.debug("Loading Swagger");

        environment.jersey().register(new ApiListingResourceJSON());
        environment.jersey().register(new ApiDeclarationProvider());
        environment.jersey().register(new ResourceListingProvider());
        ScannerFactory.setScanner(new DefaultJaxrsScanner());
        ClassReaders.setReader(new DefaultJaxrsApiReader());
        SwaggerConfig swaggerConfig = ConfigFactory.config();
        swaggerConfig.setApiVersion(hesperidesConfiguration.getApiVersion());
        swaggerConfig.setApiPath("");
        swaggerConfig.setBasePath("/rest");

        LOGGER.debug("Using Authentication Provider {}", hesperidesConfiguration.getAuthenticatorType());

        Optional<Authenticator<BasicCredentials, User>> authenticator = hesperidesConfiguration.getAuthenticator();

        final ThreadLocalUserContext userContext = new ThreadLocalUserContext();


        if (authenticator.isPresent()) {
            final HesperidesAuthenticator hesperidesAuthenticator = new HesperidesAuthenticator(authenticator.get(), userContext,
                    environment.metrics(), hesperidesConfiguration.getAuthenticationCachePolicy());

            environment.jersey().register(new AuthDynamicFeature(new BasicCredentialAuthFilter.Builder<User>()
                    .setAuthenticator(hesperidesAuthenticator)
                    .setAuthorizer(hesperidesAuthenticator)
                    .setRealm("LOGIN AD FOR HESPERIDES")
                    .buildAuthFilter()));

            // To enable Role filter
            environment.jersey().register(RolesAllowedDynamicFeature.class);
        } else {
            final DisabledAuthenticator disabledAuthenticator = new DisabledAuthenticator();

            environment.jersey().register(new AuthDynamicFeature(new NoCredentialAuthFilter.Builder<User>()
                    .setAuthenticator(disabledAuthenticator)
                    .setAuthorizer(disabledAuthenticator)
                    .setRealm("LOGIN AD FOR HESPERIDES")
                    .buildAuthFilter()));
        }

        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(User.class));

        LOGGER.debug("Creating Redis connection pool");

        /* The Event Store, which is managed to properly close the connections */
        // TODO best is abstract it in config file to allow MongoDB store or something else.
        final ManageableJedisConnectionInterface manageableJedisConnectionPool = new ManageableJedisConnection(
                hesperidesConfiguration.getRedisConfiguration());
        final RedisEventStore eventStore;


        final ManageableJedisConnectionInterface snapshotManageableJedisConnectionPool
                = new ManageableJedisConnection(
                hesperidesConfiguration.getCacheConfiguration().getRedisConfiguration());

        eventStore = new RedisEventStore(manageableJedisConnectionPool, snapshotManageableJedisConnectionPool);
        environment.lifecycle().manage(manageableJedisConnectionPool);

        LOGGER.debug("Creating Event Bus");
        final EventBus eventBus = new EventBus();

        LOGGER.debug("Creating aggregates");
        /* Create the http client */
        final HttpClient httpClient = new HttpClientBuilder(environment).using(hesperidesConfiguration.getHttpClientConfiguration()).build
                ("elasticsearch");
        environment.jersey().register(httpClient);
        /* Create the elasticsearch client */
        final ElasticSearchClient elasticSearchClient = new ElasticSearchClient(httpClient, hesperidesConfiguration.getElasticSearchConfiguration());

        /* Search Helpers */
        final ApplicationSearch applicationSearch = new ApplicationSearch(elasticSearchClient);
        final ModuleSearch moduleSearch = new ModuleSearch(elasticSearchClient);
        final TemplateSearch templateSearch = new TemplateSearch(elasticSearchClient);

        /* Registries (read part of the application) */
        final SnapshotRegistryInterface snapshotRegistryInterface = new SnapshotRegistry(manageableJedisConnectionPool.getPool());

        /* Aggregates (write part of the application: events + method to read from registries) */
        final TemplatePackagesAggregate templatePackagesAggregate = new TemplatePackagesAggregate(eventBus, eventStore,
                userContext, hesperidesConfiguration);
        environment.lifecycle().manage(templatePackagesAggregate);

        final ModulesAggregate modulesAggregate = new ModulesAggregate(eventBus, eventStore, templatePackagesAggregate,
                userContext, hesperidesConfiguration);
        environment.lifecycle().manage(modulesAggregate);

        final ApplicationsAggregate applicationsAggregate = new ApplicationsAggregate(eventBus, eventStore, snapshotRegistryInterface,
                hesperidesConfiguration, userContext);
        environment.lifecycle().manage(applicationsAggregate);

        final Applications permissionAwareApplications = new PermissionAwareApplicationsProxy(applicationsAggregate, userContext);
        /* Events aggregate */

        EventsAggregate eventsAggregate = new EventsAggregate(hesperidesConfiguration.getEventsConfiguration(), eventStore);
        environment.lifecycle().manage(eventsAggregate);

        /* Feedbacks aggregate */
        FeedbackConfiguration feedbackConfiguration = hesperidesConfiguration.getFeedbackConfiguration();

        FeedbacksAggregate feedbacksAggregate;

        if (feedbackConfiguration == null) {
            feedbacksAggregate = null;
        } else {
            feedbacksAggregate = new FeedbacksAggregate(feedbackConfiguration,
                    hesperidesConfiguration.getAssetsConfiguration(), hesperidesConfiguration.getProxyConfiguration());
            environment.lifecycle().manage(feedbacksAggregate);
        }

        /* Service to generate files */
        final Files files = new Files(permissionAwareApplications, modulesAggregate, templatePackagesAggregate);

        LOGGER.debug("Loading indexation module");
        /* The indexer */

        final ElasticSearchIndexationExecutor elasticSearchIndexationExecutor
                = new ElasticSearchIndexationExecutor(elasticSearchClient,
                hesperidesConfiguration.getElasticSearchConfiguration().getRetry(),
                hesperidesConfiguration.getElasticSearchConfiguration().getWaitBeforeRetryMs());
        /*
         * Register indexation listeners
         */
        eventBus.register(new ModuleEventsIndexation(elasticSearchIndexationExecutor));
        eventBus.register(new PlatformEventsIndexation(elasticSearchIndexationExecutor));
        eventBus.register(new TemplateEventsIndexation(elasticSearchIndexationExecutor));

        LOGGER.debug("Creating web resources");
        environment.jersey().setUrlPattern("/rest/*");

        final TimeStampedPlatformConverter timeStampedPlatformConverter = new DefaultTimeStampedPlatformConverter();
        final ApplicationConverter applicationConverter = new DefaultApplicationConverter();
        final PropertiesConverter propertiesConverter = new DefaultPropertiesConverter();

        final HesperidesTemplateResource templateResource = new HesperidesTemplateResource(templatePackagesAggregate,
                templateSearch);

        environment.jersey().register(templateResource);

        final HesperidesModuleResource moduleResource = new HesperidesModuleResource(modulesAggregate, moduleSearch);
        environment.jersey().register(moduleResource);

        final HesperidesApplicationResource applicationResource = new HesperidesApplicationResource(
                new PermissionAwareApplicationsProxy(applicationsAggregate, userContext), modulesAggregate,
                applicationSearch, timeStampedPlatformConverter, applicationConverter,
                propertiesConverter, moduleResource);

        environment.jersey().register(applicationResource);


        HesperidesStatsResource statsResource = new HesperidesStatsResource(
                new PermissionAwareApplicationsProxy(applicationsAggregate, userContext), modulesAggregate, templatePackagesAggregate);
        environment.jersey().register(statsResource);

        final HesperidesFilesResource filesResource = new HesperidesFilesResource(files, moduleResource);
        environment.jersey().register(filesResource);

        final HesperidesVersionsResource versionsResource = new HesperidesVersionsResource(hesperidesConfiguration.getBackendVersion(),
                hesperidesConfiguration.getApiVersion());
        environment.jersey().register(versionsResource);

        final HesperidesFullIndexationResource fullIndexationResource = new HesperidesFullIndexationResource(elasticSearchIndexationExecutor,
                applicationsAggregate, modulesAggregate, templatePackagesAggregate);
        environment.jersey().register(fullIndexationResource);

        // Events resource
        final HesperidesEventResource eventResource = new HesperidesEventResource(eventsAggregate);
        environment.jersey().register(eventResource);


        final CacheGeneratorTemplatePackagesAggregate cacheTemplatePackagesAggregate = new CacheGeneratorTemplatePackagesAggregate(eventStore,
                hesperidesConfiguration);
        final CacheGeneratorModuleAggregate cacheModulesAggregate = new CacheGeneratorModuleAggregate(eventStore, hesperidesConfiguration);
        final CacheGeneratorApplicationAggregate cacheApplicationsAggregate = new CacheGeneratorApplicationAggregate(eventStore, hesperidesConfiguration);

        HesperidesCacheResource hesperidesCacheResource = new HesperidesCacheResource(templatePackagesAggregate,
                modulesAggregate, applicationsAggregate, cacheTemplatePackagesAggregate, cacheModulesAggregate, cacheApplicationsAggregate);

        environment.jersey().register(hesperidesCacheResource);

        // Users resource
        final HesperidesUserResource userResource = new HesperidesUserResource();
        environment.jersey().register(userResource);

        // Feedback resource
        HesperidesFeedbackRessource feedbackResource = new HesperidesFeedbackRessource(feedbacksAggregate);
        environment.jersey().register(feedbackResource);

        LOGGER.debug("Registering exception handlers");
        /* Error handling */
        environment.jersey().register(new DefaultExceptionMapper());
        environment.jersey().register(new DuplicateResourceExceptionMapper());
        environment.jersey().register(new IncoherentVersionExceptionMapper());
        environment.jersey().register(new OutOfDateVersionExceptionMapper());
        environment.jersey().register(new MissingResourceExceptionMapper());
        environment.jersey().register(new IllegalArgumentExceptionMapper());
        environment.jersey().register(new ForbiddenOperationExceptionMapper());
        environment.jersey().register(new ForbiddenExceptionMapper());

        // ressource healthcheck
        environment.healthChecks().register("elasticsearch", new ElasticSearchHealthCheck(elasticSearchClient));
        environment.healthChecks().register("aggregate_applications", new AggregateHealthCheck(applicationsAggregate));
        environment.healthChecks().register("aggregate_modules", new AggregateHealthCheck(modulesAggregate));
        environment.healthChecks().register("aggregate_template_packages", new AggregateHealthCheck(templatePackagesAggregate));
        environment.healthChecks().register("event_store", new EventStoreHealthCheck(eventStore));

        LOGGER.debug("Loading JMX Reporter");

        /* Exposition JMX */
        // active l'export des metrics en jmx
        final JmxReporter reporter = JmxReporter.forRegistry(environment.metrics()).build();
        reporter.start();

        if (hesperidesConfiguration.getCacheConfiguration().isGenerateCaheOnStartup()) {
            LOGGER.info("Regenerate cache for template package.");
            cacheTemplatePackagesAggregate.regenerateCache();

            LOGGER.info("Regenerate cache for module.");
            cacheModulesAggregate.regenerateCache();

            LOGGER.info("Regenerate cache for application.");
            cacheApplicationsAggregate.regenerateCache();

            LOGGER.info("All cache were regenerated successfully.");
        }

        if(hesperidesConfiguration.getElasticSearchConfiguration().reindexOnStartup()) {
            /* Reset the index */
            fullIndexationResource.resetIndex();
        }
    }
}
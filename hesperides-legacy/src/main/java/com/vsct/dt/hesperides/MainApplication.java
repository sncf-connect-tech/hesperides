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
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.vsct.dt.hesperides.api.ModuleApi;
import com.vsct.dt.hesperides.api.authentication.User;
import com.vsct.dt.hesperides.applications.Applications;
import com.vsct.dt.hesperides.applications.ApplicationsAggregate;
import com.vsct.dt.hesperides.applications.SnapshotRegistry;
import com.vsct.dt.hesperides.applications.SnapshotRegistryInterface;
import com.vsct.dt.hesperides.cache.HesperidesCacheResource;
import com.vsct.dt.hesperides.domain.modules.ModuleSearchRepository;
import com.vsct.dt.hesperides.events.EventsAggregate;
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
import com.vsct.dt.hesperides.infrastructure.elasticsearch.ElasticSearchConfiguration;
import com.vsct.dt.hesperides.infrastructure.elasticsearch.modules.ElasticSearchModuleSearchRepository;
import com.vsct.dt.hesperides.infrastructure.redis.RedisConfiguration;
import com.vsct.dt.hesperides.resources.*;
import com.vsct.dt.hesperides.security.BasicAuthProviderWithUserContextHolder;
import com.vsct.dt.hesperides.security.CorrectedCachingAuthenticator;
import com.vsct.dt.hesperides.security.DisabledAuthProvider;
import com.vsct.dt.hesperides.security.ThreadLocalUserContext;
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
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.http.client.HttpClient;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;
import javax.ws.rs.container.ContainerRequestFilter;
import java.util.Optional;

public final class MainApplication extends Application<HesperidesConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainApplication.class);

    public static void main(final String[] args) throws Exception {
        new MainApplication().run(args);
    }

    @Override
    public String getName() {
        return "hesperides";
    }

    @Override
    public void initialize(final Bootstrap<HesperidesConfiguration> hesperidesConfigurationBootstrap) {
        hesperidesConfigurationBootstrap.addBundle(new ConfiguredAssetsBundle("/assets/", "/", "index.html"));
    }

    private Injector createInjector(final HesperidesConfiguration hesperidesConfiguration) {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(RedisConfiguration.class).toInstance(hesperidesConfiguration.getRedisConfiguration());
                bind(ElasticSearchConfiguration.class).toInstance(hesperidesConfiguration.getElasticSearchConfiguration());
                bind(ModuleSearchRepository.class).to(ElasticSearchModuleSearchRepository.class);
                bind(MustacheFactory.class).to(DefaultMustacheFactory.class);
            }
        });
    }

    @Override
    public void run(final HesperidesConfiguration hesperidesConfiguration, final Environment environment) throws Exception {
        Injector injector = createInjector(hesperidesConfiguration);

        // API
        environment.jersey().register(injector.getInstance(ModuleApi.class));


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

        // Authentication
        LOGGER.debug("Using Authentication Provider {}", hesperidesConfiguration.getAuthenticatorType());
        Optional<Authenticator<BasicCredentials, User>> authenticator = hesperidesConfiguration.getAuthenticator();
        ContainerRequestFilter authProvider;
        ThreadLocalUserContext userContext = new ThreadLocalUserContext(environment.jersey());
        if (authenticator.isPresent()) {
            authProvider = new BasicAuthProviderWithUserContextHolder(
                    new CorrectedCachingAuthenticator(
                            environment.metrics(),
                            authenticator.get(),
                            hesperidesConfiguration.getAuthenticationCachePolicy()
                    ),
                    "LOGIN AD POUR HESPERIDES",
                    userContext,
                    hesperidesConfiguration.useDefaultUserWhenAuthentFails());

            environment.jersey().register(new AuthDynamicFeature(
                    new BasicCredentialAuthFilter.Builder<User>()
                            .setAuthenticator(authenticator.get())
                            .setRealm("LOGIN AD POUR HESPERIDES")
                            .buildAuthFilter()));
//            environment.jersey().register(RolesAllowedDynamicFeature.class);
            //If you want to use @Auth to inject a custom Principal type into your resource
            environment.jersey().register(new AuthValueFactoryProvider.Binder(User.class));
        } else {
            authProvider = new DisabledAuthProvider();
        }
        environment.jersey().register(authProvider);


        // CORS configuration if conf exist
        if (hesperidesConfiguration.getCorsConfiguration() != null &&
                hesperidesConfiguration.getCorsConfiguration().getHeaders() != null &&
                hesperidesConfiguration.getCorsConfiguration().getMethods() != null &&
                hesperidesConfiguration.getCorsConfiguration().getOrigins() != null) {
            enableCorsHeaders(hesperidesConfiguration, environment);
        }

        LOGGER.debug("Creating Redis connection pool");

        /* The Event Store, which is managed to properly close the connections */
        // TODO best is abstract it in config file to allow MongoDB store or something else.
        final ManageableJedisConnectionInterface manageableJedisConnectionPool = new ManageableJedisConnection(
                hesperidesConfiguration.getRedisConfiguration());
        final RedisEventStore eventStore;


        final ManageableJedisConnectionInterface snapshotManageableJedisConnectionPool
                = new ManageableJedisConnection(
                hesperidesConfiguration.getCacheConfiguration().getRedisConfiguration());

        eventStore = new RedisEventStore(manageableJedisConnectionPool, snapshotManageableJedisConnectionPool, () -> System.currentTimeMillis());
        environment.lifecycle().manage(manageableJedisConnectionPool);

        LOGGER.debug("Creating Event Bus");
        EventBus eventBus = new EventBus();

        LOGGER.debug("Creating aggregates");
        /* Create the http client */
        HttpClient httpClient = new HttpClientBuilder(environment).using(hesperidesConfiguration.getHttpClientConfiguration()).build("elasticsearch");
//        environment.jersey().getResourceConfig().getRootResourceSingletons().add(httpClient);
        /* Create the elasticsearch client */
        ElasticSearchClient elasticSearchClient = new ElasticSearchClient(httpClient, hesperidesConfiguration.getElasticSearchConfiguration());

        /* Search Helpers */
        ApplicationSearch applicationSearch = new ApplicationSearch(elasticSearchClient);
        ModuleSearch moduleSearch = new ModuleSearch(elasticSearchClient);
        TemplateSearch templateSearch = new TemplateSearch(elasticSearchClient);

        /* Registries (read part of the application) */
        SnapshotRegistryInterface snapshotRegistryInterface = new SnapshotRegistry(manageableJedisConnectionPool.getPool());

        /* Aggregates (write part of the application: events + method to read from registries) */
        TemplatePackagesAggregate templatePackagesAggregate = new TemplatePackagesAggregate(eventBus, eventStore,
                userContext, hesperidesConfiguration);
        environment.lifecycle().manage(templatePackagesAggregate);

        ModulesAggregate modulesAggregate = new ModulesAggregate(eventBus, eventStore, templatePackagesAggregate,
                userContext, hesperidesConfiguration);
        environment.lifecycle().manage(modulesAggregate);

        ApplicationsAggregate applicationsAggregate = new ApplicationsAggregate(eventBus, eventStore, snapshotRegistryInterface,
                hesperidesConfiguration, userContext);
        environment.lifecycle().manage(applicationsAggregate);

        Applications permissionAwareApplications = new PermissionAwareApplicationsProxy(applicationsAggregate, userContext);
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
        Files files = new Files(permissionAwareApplications, modulesAggregate, templatePackagesAggregate);

        LOGGER.debug("Loading indexation module");
        /* The indexer */

        ElasticSearchIndexationExecutor elasticSearchIndexationExecutor
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

        TimeStampedPlatformConverter timeStampedPlatformConverter = new DefaultTimeStampedPlatformConverter();
        ApplicationConverter applicationConverter = new DefaultApplicationConverter();
        PropertiesConverter propertiesConverter = new DefaultPropertiesConverter();

        HesperidesTemplateResource templateResource = new HesperidesTemplateResource(templatePackagesAggregate,
                templateSearch);

        environment.jersey().register(templateResource);

        HesperidesModuleResource moduleResource = new HesperidesModuleResource(modulesAggregate, moduleSearch);
        environment.jersey().register(moduleResource);

        HesperidesApplicationResource applicationResource = new HesperidesApplicationResource(
                new PermissionAwareApplicationsProxy(applicationsAggregate, userContext), modulesAggregate,
                applicationSearch, timeStampedPlatformConverter, applicationConverter,
                propertiesConverter, moduleResource);

        environment.jersey().register(applicationResource);

        HesperidesStatsResource statsResource = new HesperidesStatsResource(
                new PermissionAwareApplicationsProxy(applicationsAggregate, userContext), modulesAggregate, templatePackagesAggregate);
        environment.jersey().register(statsResource);

        HesperidesFilesResource filesResource = new HesperidesFilesResource(files, moduleResource);
        environment.jersey().register(filesResource);

        HesperidesVersionsResource versionsResource = new HesperidesVersionsResource(hesperidesConfiguration.getBackendVersion(), hesperidesConfiguration.getApiVersion());
        environment.jersey().register(versionsResource);

        HesperidesFullIndexationResource fullIndexationResource = new HesperidesFullIndexationResource(elasticSearchIndexationExecutor, applicationsAggregate, modulesAggregate, templatePackagesAggregate);
        environment.jersey().register(fullIndexationResource);

        // Events resource
        HesperidesEventResource eventResource = new HesperidesEventResource(eventsAggregate);
        environment.jersey().register(eventResource);

        final CacheGeneratorTemplatePackagesAggregate cacheTemplatePackagesAggregate = new CacheGeneratorTemplatePackagesAggregate(eventStore,
                hesperidesConfiguration);
        final CacheGeneratorModuleAggregate cacheModulesAggregate = new CacheGeneratorModuleAggregate(eventStore, hesperidesConfiguration);
        final CacheGeneratorApplicationAggregate cacheApplicationsAggregate = new CacheGeneratorApplicationAggregate(eventStore, hesperidesConfiguration);

        HesperidesCacheResource hesperidesCacheResource = new HesperidesCacheResource(templatePackagesAggregate,
                modulesAggregate, applicationsAggregate, cacheTemplatePackagesAggregate, cacheModulesAggregate, cacheApplicationsAggregate);

        environment.jersey().register(hesperidesCacheResource);

        // Users resource
        HesperidesUserResource userResource = new HesperidesUserResource();
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

        // ressource healthcheck
        environment.healthChecks().register("elasticsearch", new ElasticSearchHealthCheck(elasticSearchClient));
        environment.healthChecks().register("aggregate_applications", new AggregateHealthCheck(applicationsAggregate));
        environment.healthChecks().register("aggregate_modules", new AggregateHealthCheck(modulesAggregate));
        environment.healthChecks().register("aggregate_template_packages", new AggregateHealthCheck(templatePackagesAggregate));
        environment.healthChecks().register("event_store", new EventStoreHealthCheck(eventStore));

        LOGGER.debug("Loading JMX Reporter");

        /* Exposition JMX */
        // active l'export des metrics en jmx
        JmxReporter reporter = JmxReporter.forRegistry(environment.metrics()).build();
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

        if (hesperidesConfiguration.getElasticSearchConfiguration().reindexOnStartup()) {
            /* Reset the index */
            fullIndexationResource.resetIndex();
        }
    }


    private void enableCorsHeaders(HesperidesConfiguration hesperidesConfiguration, Environment env) {
        final FilterRegistration.Dynamic cors = env.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS parameters
        //cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        //cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "Authorization,X-Requested-With,Content-Type,Accept,Origin");
        //cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "OPTIONS,GET,PUT,POST,DELETE,HEAD");

        cors.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, hesperidesConfiguration.getCorsConfiguration().getOrigins());
        cors.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, hesperidesConfiguration.getCorsConfiguration().getHeaders());
        cors.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, hesperidesConfiguration.getCorsConfiguration().getMethods());

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }
}
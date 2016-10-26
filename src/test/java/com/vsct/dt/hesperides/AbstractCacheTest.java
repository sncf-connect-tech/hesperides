package com.vsct.dt.hesperides;

import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;
import com.vsct.dt.hesperides.applications.ApplicationsAggregate;
import com.vsct.dt.hesperides.applications.PlatformKey;
import com.vsct.dt.hesperides.applications.SnapshotRegistry;
import com.vsct.dt.hesperides.storage.EventStore;
import com.vsct.dt.hesperides.storage.RedisEventStore;
import com.vsct.dt.hesperides.storage.RetryRedisConfiguration;
import com.vsct.dt.hesperides.templating.modules.Module;
import com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyKey;
import com.vsct.dt.hesperides.templating.modules.ModulesAggregate;
import com.vsct.dt.hesperides.templating.modules.Techno;
import com.vsct.dt.hesperides.templating.modules.template.TemplateData;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageWorkingCopyKey;
import com.vsct.dt.hesperides.templating.packages.TemplatePackagesAggregate;
import com.vsct.dt.hesperides.templating.platform.ApplicationModuleData;
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import com.vsct.dt.hesperides.util.HesperidesCacheConfiguration;
import com.vsct.dt.hesperides.util.ManageableConnectionPoolMock;
import org.junit.Before;

/**
 * Created by emeric_martineau on 10/06/2016.
 */
public abstract class AbstractCacheTest {
    protected static final int NB_EVENT_BEFORE_STORE = 5;

    protected final EventBus eventBus       = new EventBus();
    protected final ManageableConnectionPoolMock poolRedis = new ManageableConnectionPoolMock();
    protected final EventStore eventStore = new RedisEventStore(poolRedis, poolRedis);
    protected TemplatePackagesAggregate templatePackagesWithEvent;
    protected ModulesAggregate modulesWithEvent;
    protected ApplicationsAggregate applicationsWithEvent;

    @Before
    public void setUp() throws Exception {
        final RetryRedisConfiguration retryRedisConfiguration = new RetryRedisConfiguration();
        final HesperidesCacheParameter hesperidesCacheParameter = new HesperidesCacheParameter();

        final HesperidesCacheConfiguration hesperidesCacheConfiguration = new HesperidesCacheConfiguration();
        hesperidesCacheConfiguration.setRedisConfiguration(retryRedisConfiguration);
        hesperidesCacheConfiguration.setPlatformTimeline(hesperidesCacheParameter);
        hesperidesCacheConfiguration.setTemplatePackage(hesperidesCacheParameter);
        hesperidesCacheConfiguration.setNbEventBeforePersiste(NB_EVENT_BEFORE_STORE);

        final HesperidesConfiguration hesperidesConfiguration = new HesperidesConfiguration();
        hesperidesConfiguration.setCacheConfiguration(hesperidesCacheConfiguration);

        templatePackagesWithEvent = new TemplatePackagesAggregate(eventBus, eventStore, hesperidesConfiguration);
        modulesWithEvent = new ModulesAggregate(eventBus, eventStore, templatePackagesWithEvent, hesperidesConfiguration);
        applicationsWithEvent = new ApplicationsAggregate(eventBus, eventStore,
                new SnapshotRegistry(poolRedis.getPool()), hesperidesConfiguration);
        poolRedis.reset();
    }

    protected TemplatePackageWorkingCopyKey generateTemplatePackage(final int max) {
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");

        return generateTemplatePackage(packageInfo, max);
    }

    protected TemplatePackageWorkingCopyKey generateTemplatePackage(final TemplatePackageWorkingCopyKey packageInfo,
                                                                    final int max) {
        TemplateData templateData = TemplateData.withTemplateName("nom du template")
                .withFilename("filename")
                .withLocation("location")
                .withContent("content")
                .withRights(null)
                .build();

        templatePackagesWithEvent.createTemplateInWorkingCopy(packageInfo, templateData);

        for (int index = 1; index < max; index++ ) {
            templateData = TemplateData.withTemplateName("nom du template")
                    .withFilename("filename" + index)
                    .withLocation("location")
                    .withContent("content")
                    .withRights(null)
                    .withVersionID(index)
                    .build();

            templatePackagesWithEvent.updateTemplateInWorkingCopy(packageInfo, templateData);
        }

        return packageInfo;
    }

    protected void generateModule(final ModuleWorkingCopyKey moduleKey, final int max) {
        Techno techno = new Techno("tomcat", "1", false);

        modulesWithEvent.createWorkingCopy(new Module(moduleKey, Sets.newHashSet(techno)));

        for (int index = 1; index < max; index++ ) {
            TemplateData templateData = TemplateData.withTemplateName("nom du template" + index)
                    .withFilename("filename")
                    .withLocation("location")
                    .withContent("content")
                    .withRights(null)
                    .build();

            modulesWithEvent.createTemplateInWorkingCopy(moduleKey, templateData);
        }
    }

    protected ModuleWorkingCopyKey generateModule(final int max) {
        final ModuleWorkingCopyKey moduleKey = new ModuleWorkingCopyKey("my_module", "the_version");

        generateModule(moduleKey, max);

        return moduleKey;
    }

    protected PlatformKey generateApplication(final int max) {
        final PlatformKey platformKey = PlatformKey.withName("a_pltfm")
                .withApplicationName("an_app")
                .build();

        generateApplication(platformKey, max);

        return platformKey;
    }

    protected void generateApplication(final PlatformKey platformKey, final int max) {
        ApplicationModuleData module1 = ApplicationModuleData
                .withApplicationName("module1")
                .withVersion("version")
                .withPath("#WDI#WAS")
                .withId(0)
                .withInstances(Sets.newHashSet())
                .isWorkingcopy()
                .build();

        PlatformData.IBuilder builder1 = PlatformData.withPlatformName(platformKey.getName())
                .withApplicationName(platformKey.getApplicationName())
                .withApplicationVersion("app_version")
                .withModules(Sets.newHashSet(module1))
                .withVersion(1L)
                .isProduction();

        applicationsWithEvent.createPlatform(builder1.build());

        for (long index = 1; index < max; index++) {
            builder1 = PlatformData.withPlatformName(platformKey.getName())
                    .withApplicationName(platformKey.getApplicationName())
                    .withApplicationVersion("app_version")
                    .withModules(Sets.newHashSet(module1))
                    .withVersion(index)
                    .isProduction();

            applicationsWithEvent.updatePlatform(builder1.build(), false);
        }
    }
}

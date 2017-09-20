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

package com.vsct.dt.hesperides.cache;

import com.google.common.cache.LoadingCache;
import com.vsct.dt.hesperides.AbstractCacheTest;
import com.vsct.dt.hesperides.HesperidesConfiguration;
import com.vsct.dt.hesperides.applications.PlatformKey;
import com.vsct.dt.hesperides.resources.ApplicationModule;
import com.vsct.dt.hesperides.resources.Instance;
import com.vsct.dt.hesperides.resources.Platform;
import com.vsct.dt.hesperides.storage.EventTimeProvider;
import com.vsct.dt.hesperides.storage.HesperidesSnapshotItem;
import com.vsct.dt.hesperides.templating.modules.Module;
import com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyKey;
import com.vsct.dt.hesperides.templating.modules.event.ModuleContainer;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageRegistry;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageWorkingCopyKey;
import com.vsct.dt.hesperides.templating.packages.event.TemplatePackageContainer;
import com.vsct.dt.hesperides.templating.packages.virtual.CacheGeneratorModuleAggregate;
import com.vsct.dt.hesperides.templating.packages.virtual.CacheGeneratorTemplatePackagesAggregate;
import com.vsct.dt.hesperides.templating.platform.ApplicationModuleData;
import com.vsct.dt.hesperides.templating.platform.InstanceData;
import com.vsct.dt.hesperides.templating.platform.KeyValueValorisationData;
import com.vsct.dt.hesperides.templating.platform.PlatformData;
import com.vsct.dt.hesperides.templating.platform.PropertiesData;
import com.vsct.dt.hesperides.util.HesperidesCacheConfiguration;
import com.vsct.dt.hesperides.util.converter.impl.DefaultPlatformConverter;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;

import tests.type.UnitTests;

/**
 * Created by emeric_martineau on 07/06/2016.
 */
@Category(UnitTests.class)
public class CacheTest extends AbstractCacheTest {
    @Test
    public void should_create_template_in_working_copy_without_snapshot() {
        TemplatePackageWorkingCopyKey packageInfo = generateTemplatePackage(NB_EVENT_BEFORE_STORE);

        final String redisKey = String.format("%s-%s",
                templatePackagesWithEvent.getStreamPrefix(), packageInfo.getEntityName());

        final Optional<HesperidesSnapshotItem> hesperidesSnapshotItem = eventStore.findLastSnapshot(redisKey);

        assertThat(hesperidesSnapshotItem.isPresent()).isTrue();

        final HesperidesSnapshotItem snapshot = hesperidesSnapshotItem.get();

        assertThat(snapshot.getNbEvents()).isEqualTo(5);

        final TemplatePackageContainer container = (TemplatePackageContainer) snapshot.getSnapshot();

        final Template tempalte = container.getTemplate("nom du template");

        assertThat(tempalte).isNotNull();
        assertThat(tempalte.getFilename()).isEqualTo("filename4");
        assertThat(tempalte.getLocation()).isEqualTo("location");
        assertThat(tempalte.getContent()).isEqualTo("content");
    }

    @Test
    public void should_invalidate_cache_for_template() throws NoSuchFieldException, IllegalAccessException {
        TemplatePackageWorkingCopyKey packageInfo = new TemplatePackageWorkingCopyKey("some_package", "package_version");

        should_create_template_in_working_copy_without_snapshot();

        Optional<Template> template = this.templatePackagesWithEvent.getTemplate(packageInfo, "nom du template");

        assertThat(template.isPresent()).isTrue();

        this.templatePackagesWithEvent.removeFromCache(packageInfo.getName(), packageInfo.getVersion().getVersionName(),
                packageInfo.isWorkingCopy());

        Field privateStringField = this.templatePackagesWithEvent.getClass().
                getDeclaredField("templateRegistry");

        privateStringField.setAccessible(true);

        final TemplatePackageRegistry templateRegistry
                = (TemplatePackageRegistry) privateStringField.get(this.templatePackagesWithEvent);

        privateStringField = templateRegistry.getClass().getDeclaredField("cache");

        privateStringField.setAccessible(true);

        final LoadingCache<String, TemplatePackageContainer> cache
                = (LoadingCache<String, TemplatePackageContainer>) privateStringField.get(templateRegistry);

        final TemplatePackageContainer tpc = cache.getIfPresent(packageInfo.getNamespace());

        assertThat(tpc).isNull();
    }

    @Test
    public void should_create_template_in_working_copy_with_snapshot() {
        final int max = NB_EVENT_BEFORE_STORE + 1;

        TemplatePackageWorkingCopyKey packageInfo = generateTemplatePackage(max);

        final String redisKey = String.format("%s-%s",
                templatePackagesWithEvent.getStreamPrefix(), packageInfo.getEntityName());

        final Optional<HesperidesSnapshotItem> hesperidesSnapshotItem = eventStore.findLastSnapshot(redisKey);

        assertThat(hesperidesSnapshotItem.isPresent()).isTrue();

        final HesperidesSnapshotItem snapshot = hesperidesSnapshotItem.get();

        assertThat(snapshot.getNbEvents()).isEqualTo(5);

        final TemplatePackageContainer container = (TemplatePackageContainer) snapshot.getSnapshot();

        final Template tempalte = container.getTemplate("nom du template");

        assertThat(tempalte).isNotNull();
        assertThat(tempalte.getFilename()).isEqualTo("filename4");
        assertThat(tempalte.getLocation()).isEqualTo("location");
        assertThat(tempalte.getContent()).isEqualTo("content");
    }

    @Test
    public void should_create_module_in_working_copy_without_snapshot() {
        ModuleWorkingCopyKey moduleKey = generateModule(NB_EVENT_BEFORE_STORE);

        final String redisKey = String.format("%s-%s",
                modulesWithEvent.getStreamPrefix(), moduleKey.getEntityName());

        final Optional<HesperidesSnapshotItem> hesperidesSnapshotItem = eventStore.findLastSnapshot(redisKey);

        assertThat(hesperidesSnapshotItem.isPresent()).isTrue();

        final HesperidesSnapshotItem snapshot = hesperidesSnapshotItem.get();

        assertThat(snapshot.getNbEvents()).isEqualTo(5);

        final ModuleContainer container = (ModuleContainer) snapshot.getSnapshot();

        assertThat(container.getModule().getName()).isEqualTo("my_module");

        assertThat(container.loadAllTemplate().size()).isEqualTo(4);
    }

    @Test
    public void should_create_module_in_working_copy_with_snapshot() {
        ModuleWorkingCopyKey moduleKey = generateModule(NB_EVENT_BEFORE_STORE + 1);

        final String redisKey = String.format("%s-%s",
                modulesWithEvent.getStreamPrefix(), moduleKey.getEntityName());

        final Optional<HesperidesSnapshotItem> hesperidesSnapshotItem = eventStore.findLastSnapshot(redisKey);

        assertThat(hesperidesSnapshotItem.isPresent()).isTrue();

        final HesperidesSnapshotItem snapshot = hesperidesSnapshotItem.get();

        assertThat(snapshot.getNbEvents()).isEqualTo(5);

        final ModuleContainer container = (ModuleContainer) snapshot.getSnapshot();

        assertThat(container.getModule().getName()).isEqualTo("my_module");

        assertThat(container.loadAllTemplate().size()).isEqualTo(4);
    }


    @Test
    public void should_create_plaform_without_snapshot() {
        PlatformKey platformKey = generateApplication(NB_EVENT_BEFORE_STORE);

        final String redisKey = String.format("%s-%s",
                applicationsWithEvent.getStreamPrefix(), platformKey.getEntityName());

        final Optional<HesperidesSnapshotItem> hesperidesSnapshotItem = eventStore.findLastSnapshot(redisKey);

        assertThat(hesperidesSnapshotItem.isPresent()).isTrue();

        final HesperidesSnapshotItem snapshot = hesperidesSnapshotItem.get();

        assertThat(snapshot).isNotNull();

        assertThat(snapshot.getNbEvents()).isEqualTo(5);
    }

    @Test
    public void should_create_plaform_with_snapshot() {
        PlatformKey platformKey = generateApplication(NB_EVENT_BEFORE_STORE + 1);

        final String redisKey = String.format("%s-%s",
                applicationsWithEvent.getStreamPrefix(), platformKey.getEntityName());

        final Optional<HesperidesSnapshotItem> hesperidesSnapshotItem = eventStore.findLastSnapshot(redisKey);

        assertThat(hesperidesSnapshotItem.isPresent()).isTrue();

        final HesperidesSnapshotItem snapshot = hesperidesSnapshotItem.get();

        assertThat(snapshot).isNotNull();

        assertThat(snapshot.getNbEvents()).isEqualTo(5);
    }


    @Test
    public void should_regenerate_cache_template_package() {
        final HesperidesCacheConfiguration hesperidesCacheConfiguration = new HesperidesCacheConfiguration();
        hesperidesCacheConfiguration.setNbEventBeforePersiste(NB_EVENT_BEFORE_STORE);

        final HesperidesConfiguration hesperidesConfiguration = new HesperidesConfiguration();
        hesperidesConfiguration.setCacheConfiguration(hesperidesCacheConfiguration);

        // Insert data
        final TemplatePackageWorkingCopyKey packageInfo = generateTemplatePackage(101);

        new CacheGeneratorTemplatePackagesAggregate(this.eventStore, hesperidesConfiguration).regenerateCache();

        final String redisKey = String.format("%s-%s",
                templatePackagesWithEvent.getStreamPrefix(), packageInfo.getEntityName());

        final String streamName = String.format("snapshotevents-%s", redisKey);

        final List<String> cacheList = poolRedis.getPool().getResource().lrange(streamName, 0, 101);

        assertThat(cacheList.size()).isEqualTo(20);

        final Optional<HesperidesSnapshotItem> hesperidesSnapshotItem = eventStore.findLastSnapshot(redisKey);

        assertThat(hesperidesSnapshotItem.isPresent()).isTrue();

        final HesperidesSnapshotItem snapshot = hesperidesSnapshotItem.get();

        assertThat(snapshot.getNbEvents()).isEqualTo(100);
    }

    @Test
    public void should_regenerate_cache_module() {
        final HesperidesCacheConfiguration hesperidesCacheConfiguration = new HesperidesCacheConfiguration();
        hesperidesCacheConfiguration.setNbEventBeforePersiste(NB_EVENT_BEFORE_STORE);

        final HesperidesConfiguration hesperidesConfiguration = new HesperidesConfiguration();
        hesperidesConfiguration.setCacheConfiguration(hesperidesCacheConfiguration);

        // Insert data
        final ModuleWorkingCopyKey moduleKey = generateModule(101);

        new CacheGeneratorModuleAggregate(this.eventStore, hesperidesConfiguration).regenerateCache();

        final String redisKey = String.format("%s-%s",
                modulesWithEvent.getStreamPrefix(), moduleKey.getEntityName());

        final String streamName = String.format("snapshotevents-%s", redisKey);

        final List<String> cacheList = poolRedis.getPool().getResource().lrange(streamName, 0, 101);

        assertThat(cacheList.size()).isEqualTo(20);

        final Optional<HesperidesSnapshotItem> hesperidesSnapshotItem = eventStore.findLastSnapshot(redisKey);

        assertThat(hesperidesSnapshotItem.isPresent()).isTrue();

        final HesperidesSnapshotItem snapshot = hesperidesSnapshotItem.get();

        assertThat(snapshot.getNbEvents()).isEqualTo(100);
    }

    @Test
    public void should_get_platform_with_timestamp() throws InterruptedException {
        // First, create Module
        final ModuleWorkingCopyKey moduleKey = generateModule(1);

        final Module module = modulesWithEvent.getModule(moduleKey).get();

        // Then create Application/Platform
        final PlatformKey platformKey = generateApplication(1);

        // Get Platform for version_id and update
        PlatformData ptf = applicationsWithEvent.getPlatform(platformKey).get();

        final Set<InstanceData> instances = new HashSet<>();

        final InstanceData instance = InstanceData.withInstanceName("TOTO_TITI").withKeyValue(new HashSet<>()).build();

        // Create instance and module in platform
        instances.add(instance);

        final ApplicationModuleData moduleData = ApplicationModuleData
                .withApplicationName(moduleKey.getName())
                .withVersion(moduleKey.getVersion().getVersionName())
                .withPath("#WAS")
                .withId((int) module.getVersionID())
                .withInstances(instances)
                .isWorkingcopy()
                .build();

        final Set<ApplicationModuleData> modules = new HashSet<>();
        modules.add(moduleData);

        // Platform need to be convert to update (PlatformData is immutable and Modules set is immutable)
        PlatformData newPtf = PlatformData
                .withPlatformName(ptf.getPlatformName())
                .withApplicationName(ptf.getApplicationName())
                .withApplicationVersion( ptf.getApplicationVersion())
                .withModules(modules)
                .withVersion(ptf.getVersionID())
                .build();

        Thread.sleep(200);

        applicationsWithEvent.updatePlatform(newPtf, false);

        // Add properties
        ptf = applicationsWithEvent.getPlatform(platformKey).get();

        final PropertiesData propertiesOld = createPropertiesData("myProp", "1");

        applicationsWithEvent.createOrUpdatePropertiesInPlatform(platformKey, "#WAS", propertiesOld, ptf.getVersionID(), "comment 1");

        // Keep timestamp
        final long timestamp = this.timeProvider.currentTimestamp() - 1;

        // New properties
        ptf = applicationsWithEvent.getPlatform(platformKey).get();

        final PropertiesData propertiesNew = createPropertiesData("myProp1", "1", "myProp2", "2");

        applicationsWithEvent.createOrUpdatePropertiesInPlatform(platformKey, "#WAS", propertiesNew, ptf.getVersionID(), "comment 2");

        // Get properties with previous timestamp and check number of propperties
        final PropertiesData prop = applicationsWithEvent.getProperties(platformKey, "#WAS", timestamp);

        assertThat(prop.getKeyValueProperties().size()).isEqualTo(1);

        final KeyValueValorisationData keyValue = prop.getKeyValueProperties().iterator().next();
        assertThat(keyValue.getName()).isEqualTo("myProp");
        assertThat(keyValue.getValue()).isEqualTo("1");
    }

    /**
     * Create properties.
     *
     * @param keyValue "key" => "value"
     *
     * @return
     */
    private static PropertiesData createPropertiesData(final String... keyValue) {
        final Set<KeyValueValorisationData> keyValueProperties = new HashSet<>();

        for (int index = 0; index < keyValue.length; index += 2) {
            keyValueProperties.add(new KeyValueValorisationData(keyValue[index], keyValue[index + 1]));
        }


        return new PropertiesData(keyValueProperties, new HashSet<>());
    }

    @Test
    public void should_get_properties_with_timestamp() throws InterruptedException {
        // First, create Module
        final ModuleWorkingCopyKey moduleKey = generateModule(1);

        final Module module = modulesWithEvent.getModule(moduleKey).get();

        // Then create Application/Platform
        final PlatformKey platformKey = generateApplication(1);

        // Get Platform for version_id and update
        PlatformData ptf = applicationsWithEvent.getPlatform(platformKey).get();

        final Set<InstanceData> instances = new HashSet<>();

        final InstanceData instance = InstanceData.withInstanceName("TOTO_TITI").withKeyValue(new HashSet<>()).build();

        // Create instance and module in platform
        instances.add(instance);

        final ApplicationModuleData moduleData = ApplicationModuleData
                .withApplicationName(moduleKey.getName())
                .withVersion(moduleKey.getVersion().getVersionName())
                .withPath("#WAS")
                .withId((int) module.getVersionID())
                .withInstances(instances)
                .isWorkingcopy()
                .build();

        final Set<ApplicationModuleData> modules = new HashSet<>();
        modules.add(moduleData);

        // Platform need to be convert to update (PlatformData is immutable and Modules set is immutable)
        PlatformData newPtf = PlatformData
                .withPlatformName(ptf.getPlatformName())
                .withApplicationName(ptf.getApplicationName())
                .withApplicationVersion( ptf.getApplicationVersion())
                .withModules(modules)
                .withVersion(ptf.getVersionID())
                .build();

        applicationsWithEvent.updatePlatform(newPtf, false);

        final Map<Integer, Long> indexWithTimeStamp = new HashMap<>();

        for (int index = 0; index < 26; index++) {
            indexWithTimeStamp.put(index, updateProperties(platformKey, index));
        }

        indexWithTimeStamp.forEach((index, timestamp) -> {
            final PropertiesData prop = applicationsWithEvent.getProperties(platformKey, "#WAS", timestamp);

            final KeyValueValorisationData keyValue = prop.getKeyValueProperties().iterator().next();

            assertThat(keyValue.getName()).isEqualTo("myProp");
            assertThat(keyValue.getValue()).isEqualTo(String.valueOf(index));
        });
    }

    /**
     * Update properties and return timestamp
     *
     * @param platformKey platform key
     * @param index index of propoerties
     *
     * @return timestamp of update
     *
     * @throws InterruptedException
     */
    private long updateProperties(final PlatformKey platformKey, final int index) throws InterruptedException {
        final PlatformData ptf;// Add properties
        ptf = applicationsWithEvent.getPlatform(platformKey).get();

        final PropertiesData propertiesOld = createPropertiesData("myProp", String.valueOf(index));

        applicationsWithEvent.createOrUpdatePropertiesInPlatform(platformKey, "#WAS", propertiesOld, ptf.getVersionID(), "comment 1");

        // When get timestamp (call timestamp() method), timestamp is increase by one.
        // Therefore currentTimestamp() method return next timestamp.
        // We need decrease by one to get timestamp of event.
        return this.timeProvider.currentTimestamp() - 1;
    }
}

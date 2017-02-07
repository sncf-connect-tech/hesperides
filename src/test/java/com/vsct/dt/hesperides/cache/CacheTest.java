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
import com.vsct.dt.hesperides.storage.HesperidesSnapshotItem;
import com.vsct.dt.hesperides.templating.modules.ModuleWorkingCopyKey;
import com.vsct.dt.hesperides.templating.modules.event.ModuleContainer;
import com.vsct.dt.hesperides.templating.modules.template.Template;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageRegistry;
import com.vsct.dt.hesperides.templating.packages.TemplatePackageWorkingCopyKey;
import com.vsct.dt.hesperides.templating.packages.event.TemplatePackageContainer;
import com.vsct.dt.hesperides.templating.packages.virtual.CacheGeneratorModuleAggregate;
import com.vsct.dt.hesperides.templating.packages.virtual.CacheGeneratorTemplatePackagesAggregate;
import com.vsct.dt.hesperides.util.HesperidesCacheConfiguration;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by emeric_martineau on 07/06/2016.
 */
public class CacheTest extends AbstractCacheTest {
    @Test
    public void should_create_template_in_working_copy_without_snapshot() {
        TemplatePackageWorkingCopyKey packageInfo = generateTemplatePackage(NB_EVENT_BEFORE_STORE);

        final String redisKey = String.format("%s-%s",
                templatePackagesWithEvent.getStreamPrefix(), packageInfo.getEntityName());

        final HesperidesSnapshotItem snapshot = eventStore.findLastSnapshot(redisKey);

        assertThat(snapshot).isNotNull();

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

        final HesperidesSnapshotItem snapshot = eventStore.findLastSnapshot(redisKey);

        assertThat(snapshot).isNotNull();

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

        final HesperidesSnapshotItem snapshot = eventStore.findLastSnapshot(redisKey);

        assertThat(snapshot).isNotNull();

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

        final HesperidesSnapshotItem snapshot = eventStore.findLastSnapshot(redisKey);

        assertThat(snapshot).isNotNull();

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

        final HesperidesSnapshotItem snapshot = eventStore.findLastSnapshot(redisKey);

        assertThat(snapshot).isNotNull();

        assertThat(snapshot.getNbEvents()).isEqualTo(5);
    }

    @Test
    public void should_create_plaform_with_snapshot() {
        PlatformKey platformKey = generateApplication(NB_EVENT_BEFORE_STORE + 1);

        final String redisKey = String.format("%s-%s",
                applicationsWithEvent.getStreamPrefix(), platformKey.getEntityName());

        final HesperidesSnapshotItem snapshot = eventStore.findLastSnapshot(redisKey);

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

        final HesperidesSnapshotItem snapshot = eventStore.findLastSnapshot(redisKey);

        assertThat(snapshot).isNotNull();

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

        final HesperidesSnapshotItem snapshot = eventStore.findLastSnapshot(redisKey);

        assertThat(snapshot).isNotNull();

        assertThat(snapshot.getNbEvents()).isEqualTo(100);
    }
}
